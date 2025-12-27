package com.bank.dispatch_consumer.domain.service;

import com.bank.dispatch_consumer.domain.entity.CardReplacementEntity;
import com.bank.dispatch_consumer.domain.mapper.EntityMapper;
import com.bank.dispatch_consumer.domain.repo.CardReplacementRepository;
import com.bank.dispatch_consumer.domain.repo.SnapshotCacheRepository;
import com.bank.dispatch_consumer.infrastructure.kafka.DltProducer;
import com.bank.dispatch_consumer.infrastructure.kafka.EventMessage;
import com.bank.dispatch_consumer.infrastructure.kafka.KafkaRxConsumer;
import com.bank.events.CardReplacementEvent;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessingServiceTest {

    @Mock private KafkaRxConsumer consumer;
    @Mock private EntityMapper mapper;
    @Mock private CardReplacementRepository repo;
    @Mock private SnapshotCacheRepository cacheRepo;
    @Mock private DltProducer dltProducer;

    // Mocks auxiliares para el mensaje de Kafka
    @Mock private ReceiverOffset offset;

    @InjectMocks
    private ProcessingService processingService;

    @Test
    void processMessage_ShouldSaveEntity_WhenFlowIsSuccessful() {
        // GIVEN
        String requestId = "req-123";
        CardReplacementEvent event = CardReplacementEvent.newBuilder()
                .setEventId("evt-1")
                .setRequestId(requestId)
                .setCustomerId("cust-1")
                .setCardPANMasked("****")
                .setReasonCode("LOST")
                .setPriority("HIGH")
                .setBranchCode("BR-1")
                .setDeliveryAddress("Address")
                .setRequestedAt(Instant.now())
                .setAttemptNumber(1)
                .setCorrelationId("cor-1")
                .setStatus("NEW")
                .build();

        // Simulamos un mensaje que envuelve el evento Avro
        // Nota: rawKafkaValue puede ser cualquier objeto, usamos un String simple
        EventMessage<CardReplacementEvent> message = new EventMessage<>(event, offset, "raw-avro-bytes");

        CardReplacementEntity entity = CardReplacementEntity.builder()
                .requestId(requestId)
                .status("NEW")
                .build();

        // 1. El consumidor devuelve un FLUX que emite 1 mensaje inmediatamente
        when(consumer.consume()).thenReturn(Flux.just(message));

        // 2. Cache devuelve vacío (no hay snapshot previo)
        when(cacheRepo.getSnapshotJson(requestId)).thenReturn(Maybe.empty());

        // 3. Mapper convierte evento a entidad
        when(mapper.toEntity(event)).thenReturn(entity);

        // 4. Repo guarda exitosamente
        when(repo.save(entity)).thenReturn(Single.just(entity));

        // WHEN
        processingService.init(); // Esto dispara la suscripción y procesa el Flux simulado

        // THEN
        verify(repo).save(entity);       // Se guardó en Mongo
        verify(offset).acknowledge();    // Se hizo commit del offset en Kafka
        verify(dltProducer, never()).send(anyString(), anyString()); // No hubo error
    }

    @Test
    void processMessage_ShouldSendToDLT_WhenErrorOccurs() {
        // GIVEN - Escenario de error
        String requestId = "req-error";
        CardReplacementEvent event = CardReplacementEvent.newBuilder()
                .setEventId("evt-1")
                .setRequestId(requestId)
                // Llenar datos mínimos obligatorios para Avro
                .setCustomerId("c")
                .setCardPANMasked("*")
                .setReasonCode("r")
                .setPriority("p")
                .setBranchCode("b")
                .setDeliveryAddress("d")
                .setRequestedAt(Instant.now())
                .setAttemptNumber(1)
                .setCorrelationId("corr")
                .setStatus("s")
                .build();

        EventMessage<CardReplacementEvent> message = new EventMessage<>(event, offset, "raw-data");
        CardReplacementEntity entity = CardReplacementEntity.builder().requestId(requestId).build();

        // 1. Flux emite mensaje
        when(consumer.consume()).thenReturn(Flux.just(message));

        // 2. Cache vacío
        when(cacheRepo.getSnapshotJson(requestId)).thenReturn(Maybe.empty());

        // 3. Mapper ok
        when(mapper.toEntity(event)).thenReturn(entity);

        // 4. ERROR al guardar en Mongo
        when(repo.save(entity)).thenReturn(Single.error(new RuntimeException("DB Down")));

        // 5. El DLT responde exitosamente (Mono.empty)
        when(dltProducer.send(eq(requestId), anyString())).thenReturn(Mono.empty());

        // WHEN
        processingService.init();

        // THEN
        verify(repo).save(entity);
        // Verificar que se envió al DLT
        verify(dltProducer).send(eq(requestId), contains("DB Down"));
        // Verificar que, a pesar del error, se hizo ack para no bloquear la cola (según tu lógica actual)
        verify(offset).acknowledge();
    }

    @Test
    void processMessage_ShouldIgnoreNullPayload() {
        // GIVEN - Mensaje con payload NULL
        EventMessage<CardReplacementEvent> message = new EventMessage<>(null, offset, "bad-data");

        when(consumer.consume()).thenReturn(Flux.just(message));

        // WHEN
        processingService.init();

        // THEN
        verify(repo, never()).save(any()); // Nunca intenta guardar
        verify(offset).acknowledge();      // Pero sí hace ack para descartarlo
    }


    @Test
    void processMessage_ShouldLogSnapshot_WhenCacheReturnsValue() {
        // GIVEN
        String requestId = "req-snapshot";
        CardReplacementEvent event = CardReplacementEvent.newBuilder()
                .setEventId("evt-1")
                .setRequestId(requestId)
                // ... setear campos obligatorios mínimos ...
                .setCustomerId("c").setCardPANMasked("*").setReasonCode("r")
                .setPriority("p").setBranchCode("b").setDeliveryAddress("d")
                .setRequestedAt(Instant.now())
                .setAttemptNumber(1).setCorrelationId("c").setStatus("s")
                .build();

        EventMessage<CardReplacementEvent> message = new EventMessage<>(event, offset, "raw");
        CardReplacementEntity entity = CardReplacementEntity.builder().requestId(requestId).build();

        // 1. Consumer entrega mensaje
        when(consumer.consume()).thenReturn(Flux.just(message));

        // 2. AQUÍ ESTÁ EL CAMBIO: El Cache devuelve un valor (Maybe.just)
        // Esto hará que el código entre al .map() que tenías en rojo
        when(cacheRepo.getSnapshotJson(requestId)).thenReturn(Maybe.just("{\"audit\": \"found\"}"));

        // 3. El resto sigue igual
        when(mapper.toEntity(event)).thenReturn(entity);
        when(repo.save(entity)).thenReturn(Single.just(entity));

        // WHEN
        processingService.init();

        // THEN
        verify(repo).save(entity);      // El flujo continuó (gracias al ignoreElement)
        verify(offset).acknowledge();

        // Verificamos que se llamó al cache (aunque esto ya lo sabemos si pasó el test)
        verify(cacheRepo).getSnapshotJson(requestId);
    }
}
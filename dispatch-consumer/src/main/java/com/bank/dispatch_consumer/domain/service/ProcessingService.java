package com.bank.dispatch_consumer.domain.service;

import com.bank.dispatch_consumer.domain.mapper.EntityMapper;
import com.bank.events.CardReplacementEvent;
import com.bank.dispatch_consumer.domain.repo.CardReplacementRepository;
import com.bank.dispatch_consumer.domain.repo.SnapshotCacheRepository;
import com.bank.dispatch_consumer.infrastructure.kafka.DltProducer;
import com.bank.dispatch_consumer.infrastructure.kafka.EventMessage;
import com.bank.dispatch_consumer.infrastructure.kafka.KafkaRxConsumer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Slf4j
public class ProcessingService {

    private final KafkaRxConsumer consumer;
    private final EntityMapper mapper;
    private final CardReplacementRepository repo;
    private final SnapshotCacheRepository cacheRepo;
    private final DltProducer dltProducer;

    @PostConstruct
    public void init() {
        consumer.consume()
                .subscribe(this::processMessage);
    }

    private void processMessage(EventMessage<CardReplacementEvent> msg) {
        if (msg.getPayload() == null) {
            msg.ack(); // Ignoramos mensajes mal formados pero hacemos ack para avanzar
            return;
        }

        CardReplacementEvent event = msg.getPayload();

        // Flujo simulado basado en descripción del PDF:
        // 1. Validar/Enriquecer con Redis
        cacheRepo.getSnapshotJson(event.getRequestId())
                .map(json -> {
                    log.info("Snapshot encontrado para enrichment: {}", json);
                    return json; // Aquí iría lógica real de merge
                })
                .ignoreElement() // Completamos ya sea que exista o no
                .toSingleDefault(true) // Continuamos
                .flatMap(ignored -> {
                    // 2. Map to Entity
                    var entity = mapper.toEntity(event);
                    entity.setReceivedAt(System.currentTimeMillis());
                    entity.setRawPayload(msg.getRawKafkaValue().toString());

                    // 3. Save Mongo
                    return repo.save(entity);
                })
                .doOnSuccess(saved -> {
                    log.info("Evento guardado OK: {}", saved.getRequestId());
                    msg.ack();
                })
                .doOnError(err -> {
                    log.error("Error procesando evento", err);
                    // 4. DLT en caso de error
                    dltProducer.send(event.getRequestId(), err.getMessage()).subscribe();
                    msg.ack(); // Ack para no bloquear, ya se mandó a DLT
                })
                .subscribe(
                        success -> {},
                        error -> log.debug("Error flujo finalizado controladamente: {}", error.getMessage())
                );
    }
}
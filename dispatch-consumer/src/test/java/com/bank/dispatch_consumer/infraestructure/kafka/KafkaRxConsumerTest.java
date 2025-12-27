package com.bank.dispatch_consumer.infraestructure.kafka;

import com.bank.dispatch_consumer.config.KafkaTopicsProperties;
import com.bank.dispatch_consumer.domain.mapper.EntityMapper;
import com.bank.dispatch_consumer.infrastructure.kafka.EventMessage;
import com.bank.dispatch_consumer.infrastructure.kafka.KafkaRxConsumer;
import com.bank.events.CardReplacementEvent;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaRxConsumerTest {

    @Mock private ReceiverOptions<String, Object> receiverOptions;
    @Mock private KafkaTopicsProperties topics;
    @Mock private EntityMapper mapper;

    @InjectMocks
    private KafkaRxConsumer consumer;

    // CORRECCIÓN: Renombramos 'record' a 'receiverRecord' para evitar conflicto con palabra reservada
    @Mock private ReceiverRecord<String, Object> receiverRecord;

    @Mock private ReceiverOffset offset;
    @Mock private GenericRecord genericRecord;

    @Test
    void mapRecord_ShouldReturnEvent_WhenValueIsGenericRecord() {
        // GIVEN
        CardReplacementEvent expectedEvent = CardReplacementEvent.newBuilder()
                .setEventId("1")
                .setRequestId("req")
                .setCustomerId("c")
                .setCardPANMasked("*")
                .setReasonCode("r")
                .setPriority("p")
                .setBranchCode("b")
                .setDeliveryAddress("d")
                .setRequestedAt(Instant.EPOCH)
                .setAttemptNumber(1)
                .setCorrelationId("c")
                .setStatus("s")
                .build();

        // Actualizamos las referencias a la nueva variable 'receiverRecord'
        when(receiverRecord.value()).thenReturn(genericRecord);
        when(receiverRecord.receiverOffset()).thenReturn(offset);

        // Simulamos que el mapper funciona bien
        when(mapper.toEvent(genericRecord)).thenReturn(expectedEvent);

        // WHEN
        EventMessage<CardReplacementEvent> result = consumer.mapRecord(receiverRecord);

        // THEN
        assertNotNull(result.getPayload());
        assertEquals("req", result.getPayload().getRequestId());
        assertEquals(offset, result.getOffset());
    }

    @Test
    void mapRecord_ShouldReturnNullPayload_WhenValueIsNotGenericRecord() {
        // GIVEN - Kafka entrega un String (basura), no un Avro
        // Actualizamos referencias
        when(receiverRecord.value()).thenReturn("Texto invalido");
        when(receiverRecord.receiverOffset()).thenReturn(offset);

        // WHEN
        EventMessage<CardReplacementEvent> result = consumer.mapRecord(receiverRecord);

        // THEN
        assertNull(result.getPayload());
        assertEquals("Texto invalido", result.getRawKafkaValue());
    }

    @Test
    void mapRecord_ShouldHandleException_WhenMapperFails() {
        // GIVEN
        // Actualizamos referencias
        when(receiverRecord.value()).thenReturn(genericRecord);
        when(receiverRecord.receiverOffset()).thenReturn(offset);

        // Simulamos error en el mapper
        when(mapper.toEvent(genericRecord)).thenThrow(new RuntimeException("Map Error"));

        // WHEN
        EventMessage<CardReplacementEvent> result = consumer.mapRecord(receiverRecord);

        // THEN
        assertNull(result.getPayload());
    }
}
package com.bank.dispatch_consumer.domain.mapper;

import com.bank.dispatch_consumer.domain.entity.CardReplacementEntity;
import com.bank.events.CardReplacementEvent;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityMapperTest {

    // Instanciamos la interfaz anónimamente para probar sus métodos 'default'
    private final EntityMapper mapper = new EntityMapper() {};

    @Mock
    private GenericRecord genericRecord;

    @Test
    void toEvent_ShouldMapGenericRecordToEvent_WhenDataIsValid() {
        // GIVEN
        long timeMillis = 1672531200000L; // 2023-01-01

        when(genericRecord.get("eventId")).thenReturn("evt-123");
        when(genericRecord.get("requestId")).thenReturn("req-001");
        when(genericRecord.get("customerId")).thenReturn("cust-555");
        when(genericRecord.get("cardPANMasked")).thenReturn("1234********5678");
        when(genericRecord.get("reasonCode")).thenReturn("LOST");
        when(genericRecord.get("priority")).thenReturn("HIGH");
        when(genericRecord.get("branchCode")).thenReturn("BR-01");
        when(genericRecord.get("deliveryAddress")).thenReturn("Av. Siempre Viva 123");
        when(genericRecord.get("requestedAt")).thenReturn(timeMillis); // Avro entrega Long
        when(genericRecord.get("attemptNumber")).thenReturn(1);       // Avro entrega Integer
        when(genericRecord.get("correlationId")).thenReturn("corr-999");
        when(genericRecord.get("status")).thenReturn("PENDING");

        // WHEN
        CardReplacementEvent result = mapper.toEvent(genericRecord);

        // THEN
        assertNotNull(result);
        assertEquals("evt-123", result.getEventId());
        assertEquals("req-001", result.getRequestId());
        // Verificamos que se haya convertido el Long a Instant correctamente
        assertEquals(Instant.ofEpochMilli(timeMillis), result.getRequestedAt());
        assertEquals(1, result.getAttemptNumber());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void toEvent_ShouldReturnNull_WhenInputIsNull() {
        assertNull(mapper.toEvent(null));
    }

    @Test
    void toEntity_ShouldMapEventToEntity_WhenDataIsValid() {
        // GIVEN
        Instant now = Instant.now();

        // CORRECCIÓN: Usamos el Builder de Avro (newBuilder) en lugar de Lombok (.builder)
        // Nota: Avro requiere setear todos los campos que no tengan valor "default" en el esquema.
        CardReplacementEvent event = CardReplacementEvent.newBuilder()
                .setEventId("evt-001")
                .setRequestId("req-001")
                .setCustomerId("cust-555")
                .setCardPANMasked("1234********5678")
                .setReasonCode("STOLEN")
                .setPriority("NORMAL")
                .setBranchCode("BR-01")
                .setDeliveryAddress("Av. Siempre Viva 123")
                .setRequestedAt(now) // El setter de Avro suele aceptar Instant si logicalTypes está activo
                .setAttemptNumber(2)
                .setCorrelationId("corr-999")
                .setStatus("PROCESSED")
                .build();

        // WHEN
        CardReplacementEntity result = mapper.toEntity(event);

        // THEN
        assertNotNull(result);
        assertEquals("req-001", result.getRequestId());
        assertEquals("cust-555", result.getCustomerId());
        assertEquals(now.toEpochMilli(), result.getRequestedAt()); // Verifica conversión Instant -> Long (Mongo)
        assertEquals(2, result.getAttemptNumber());
        assertEquals("PROCESSED", result.getStatus());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenInputIsNull() {
        assertNull(mapper.toEntity(null));
    }
}
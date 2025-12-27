package com.bank.card_ops_producer.domain.mapper;



import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.events.CardReplacementEvent;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import static org.junit.jupiter.api.Assertions.*;


class EventMapperTest {

    private final EventMapper mapper = new EventMapper();

    @Test
    void toEvent_ShouldMapAllFieldsCorrectly() {
        // ARRANGE: Preparamos un DTO con datos de prueba
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        // Usamos el Builder para evitar problemas de constructores
        CardReplacementRequestDto dto = CardReplacementRequestDto.builder()
                .requestId("req-123")
                .customerId("cust-999")
                .cardPANMasked("1234********5678")
                .reasonCode("LOST")
                .priority("HIGH")
                .branchCode("BR-001")
                .deliveryAddress("Av. Test 123")
                .requestedAt(now)
                .correlationId("corr-777")
                .status("PENDING")
                .build();

        int attemptNumber = 1;

        // ACT: Ejecutamos el mapper
        CardReplacementEvent event = mapper.toEvent(dto, attemptNumber);

        // ASSERT: Verificamos campo por campo
        assertNotNull(event.getEventId()); // El ID se genera random, solo validamos que no sea nulo
        assertEquals("req-123", event.getRequestId());
        assertEquals("cust-999", event.getCustomerId());
        assertEquals("1234********5678", event.getCardPANMasked());
        assertEquals("LOST", event.getReasonCode());
        assertEquals("HIGH", event.getPriority());
        assertEquals("BR-001", event.getBranchCode());
        assertEquals("Av. Test 123", event.getDeliveryAddress());

        // Avro guarda Instant como long (milisegundos) o Instant directo según config
        // Aquí verificamos que sea igual al objeto Instant que pasamos
        assertEquals(now, event.getRequestedAt());

        assertEquals(1, event.getAttemptNumber());
        assertEquals("corr-777", event.getCorrelationId());
        assertEquals("PENDING", event.getStatus());
    }
}
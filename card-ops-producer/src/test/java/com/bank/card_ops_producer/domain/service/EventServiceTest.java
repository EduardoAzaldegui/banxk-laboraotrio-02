package com.bank.card_ops_producer.domain.service;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.mapper.EventMapper;
import com.bank.card_ops_producer.domain.policy.AttemptPolicy;
import com.bank.card_ops_producer.domain.port.EventPublisher;
import com.bank.events.CardReplacementEvent;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.time.Instant; // <--- Importar esto
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private AttemptPolicy policy;
    @Mock private EventMapper mapper;
    @Mock private EventPublisher<Object> publisher;

    private EventService eventService;

    @BeforeEach
    void setup() {
        eventService = new EventService(policy, mapper, publisher, "topic-test");
    }

    @Test
    void process_ShouldOrchestrateFlowCorrectly() {
        // ARRANGE
        CardReplacementRequestDto dto = CardReplacementRequestDto.builder()
                .requestId("req-123")
                .build();

        CardReplacementEvent mockEvent = CardReplacementEvent.newBuilder()
                .setEventId("event-abc")
                .setRequestId("req-123")
                .setCustomerId("c")
                .setCardPANMasked("p")
                .setReasonCode("r")
                .setPriority("p")
                .setBranchCode("b")
                .setDeliveryAddress("d")

                // CORRECCIÓN AQUÍ: Usamos un Instant en lugar de un long
                .setRequestedAt(Instant.ofEpochMilli(123L))

                .setAttemptNumber(1)
                .setCorrelationId("c")
                .setStatus("s")
                .build();

        SendResult<String, Object> mockSendResult = mock(SendResult.class);

        // Simulamos la cadena de llamadas reactivas:
        when(policy.resolveAttempt(dto)).thenReturn(Single.just(1));
        when(mapper.toEvent(dto, 1)).thenReturn(mockEvent);
        when(publisher.publish("topic-test", "req-123", mockEvent))
                .thenReturn(Single.just(mockSendResult));

        // ACT
        TestObserver<String> observer = eventService.process(dto).test();

        // ASSERT
        observer.assertComplete();
        observer.assertValue("event-abc");
    }
}
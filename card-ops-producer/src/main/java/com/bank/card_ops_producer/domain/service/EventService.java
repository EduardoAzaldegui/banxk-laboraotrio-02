package com.bank.card_ops_producer.domain.service;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.mapper.EventMapper;
import com.bank.card_ops_producer.domain.policy.AttemptPolicy;
import com.bank.card_ops_producer.domain.port.EventPublisher;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final AttemptPolicy policy;
    private final EventMapper mapper;
    private final EventPublisher<Object> publisher;
    private final String topic;

    public EventService(AttemptPolicy policy,
                        EventMapper mapper,
                        EventPublisher<Object> publisher,
                        @Value("${kafka.topic}") String topic) {
        this.policy = policy;
        this.mapper = mapper;
        this.publisher = publisher;
        this.topic = topic;
    }

    public Single<String> process(CardReplacementRequestDto dto) {
        return policy.resolveAttempt(dto)
                .map(attempt -> mapper.toEvent(dto, attempt))
                .flatMap(event -> publisher.publish(topic, event.getRequestId(), event)
                        .map(result -> event.getEventId())
                );
    }
}
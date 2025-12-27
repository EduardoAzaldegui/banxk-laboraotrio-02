package com.bank.dispatch_consumer.infrastructure.kafka;

import com.bank.dispatch_consumer.config.KafkaTopicsProperties;
import com.bank.dispatch_consumer.domain.mapper.EntityMapper;
import com.bank.events.CardReplacementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.core.publisher.Flux;
import java.util.Collections;


@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaRxConsumer {
    private final ReceiverOptions<String, Object> receiverOptions;
    private final KafkaTopicsProperties topics;
    private final EntityMapper mapper;

    public Flux<EventMessage<CardReplacementEvent>> consume() {
        return KafkaReceiver.create(receiverOptions.subscription(Collections.singleton(topics.getMain())))
                .receive()
                .map(this::mapRecord);
    }

    public EventMessage<CardReplacementEvent> mapRecord(ReceiverRecord<String, Object> rec){
        Object value = rec.value();
        CardReplacementEvent ev = null;
        try {
            if (value instanceof GenericRecord gr) {
                ev = mapper.toEvent(gr);
            } else {
                log.warn("Valor no Avro ({}), se ignora", value == null ? "null" : value.getClass());
            }
        } catch (Exception e) {
            log.error("Error mapeando Avro -> Event", e);
        }
        return new EventMessage<>(ev, rec.receiverOffset(), value);
    }
}
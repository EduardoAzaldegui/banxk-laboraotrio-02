package com.bank.dispatch_consumer.domain.mapper;

import com.bank.dispatch_consumer.domain.entity.CardReplacementEntity;
import com.bank.events.CardReplacementEvent;
import org.apache.avro.generic.GenericRecord;
import org.mapstruct.Mapper;
import java.time.Instant;



@Mapper(componentModel = "spring")
public interface EntityMapper {

    default CardReplacementEvent toEvent(GenericRecord gr) {
        if (gr == null) return null;
        CardReplacementEvent e = new CardReplacementEvent();
        e.setEventId(toStringOrNull(gr.get("eventId")));
        e.setRequestId(toStringOrNull(gr.get("requestId")));
        e.setCustomerId(toStringOrNull(gr.get("customerId")));
        e.setCardPANMasked(toStringOrNull(gr.get("cardPANMasked")));
        e.setReasonCode(toStringOrNull(gr.get("reasonCode")));
        e.setPriority(toStringOrNull(gr.get("priority")));
        e.setBranchCode(toStringOrNull(gr.get("branchCode")));
        e.setDeliveryAddress(toStringOrNull(gr.get("deliveryAddress")));

        Object ts = gr.get("requestedAt");
        if (ts instanceof Long l) e.setRequestedAt(Instant.ofEpochMilli(l));

        Object at = gr.get("attemptNumber");
        if (at instanceof Integer i) e.setAttemptNumber(i);

        e.setCorrelationId(toStringOrNull(gr.get("correlationId")));
        e.setStatus(toStringOrNull(gr.get("status")));
        return e;
    }

    default CardReplacementEntity toEntity(CardReplacementEvent ev) {
        if (ev == null) return null;
        CardReplacementEntity en = new CardReplacementEntity();
        en.setRequestId(ev.getRequestId());
        en.setCustomerId(ev.getCustomerId());
        // Nota: cardPANMasked y reasonCode no están en la entidad según el PDF, pero se mapean en la lógica si fuera necesario.
        en.setBranchCode(ev.getBranchCode());
        en.setDeliveryAddress(ev.getDeliveryAddress());
        en.setRequestedAt(ev.getRequestedAt() != null ? ev.getRequestedAt().toEpochMilli() : 0L);
        en.setAttemptNumber(ev.getAttemptNumber());
        en.setCorrelationId(ev.getCorrelationId());
        en.setStatus(ev.getStatus());
        return en;
    }

    // Helper para evitar casteos inseguros directos en el código generado
    private String toStringOrNull(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}
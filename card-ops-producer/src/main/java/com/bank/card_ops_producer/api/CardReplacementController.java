package com.bank.card_ops_producer.api;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.service.EventService;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/card-replacements")
@RequiredArgsConstructor
public class CardReplacementController {

    private final EventService service;

    @PostMapping
    public Single<ResponseEntity<String>> create(@Valid @RequestBody CardReplacementRequestDto dto) {
        return service.process(dto)
                .map(id -> ResponseEntity.accepted().body("Event Processed. ID: " + id));
    }
}
package com.bank.dispatch_consumer.infrastructure.mongo;

import com.bank.dispatch_consumer.domain.entity.CardReplacementEntity;
import com.bank.dispatch_consumer.domain.repo.CardReplacementRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class CardReplacementRepositoryMongoAdapter implements CardReplacementRepository {
    private final SpringCardReplacementReactiveRepo reactiveRepo;

    @Override
    public Maybe<CardReplacementEntity> findByRequestId(String requestId) {
        // Simulando RxJava3Adapter.monoToMaybe usando el bridge estándar o lógica reactor
        return Maybe.fromPublisher(reactiveRepo.findByRequestId(requestId));
    }

    @Override
    public Single<Boolean> existsByRequestId(String requestId) {
        return Single.fromPublisher(reactiveRepo.existsByRequestId(requestId));
    }

    @Override
    public Single<CardReplacementEntity> save(CardReplacementEntity entity) {
        return Single.fromPublisher(reactiveRepo.save(entity));
    }
}
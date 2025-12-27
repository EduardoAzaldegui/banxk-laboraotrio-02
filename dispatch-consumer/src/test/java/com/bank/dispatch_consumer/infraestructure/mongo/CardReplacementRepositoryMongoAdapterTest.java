package com.bank.dispatch_consumer.infraestructure.mongo;
import com.bank.dispatch_consumer.domain.entity.CardReplacementEntity;
import com.bank.dispatch_consumer.infrastructure.mongo.CardReplacementRepositoryMongoAdapter;
import com.bank.dispatch_consumer.infrastructure.mongo.SpringCardReplacementReactiveRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardReplacementRepositoryMongoAdapterTest {

    @Mock
    private SpringCardReplacementReactiveRepo reactiveRepo;

    @InjectMocks
    private CardReplacementRepositoryMongoAdapter adapter;

    @Test
    void findByRequestId_ShouldReturnMaybeWithEntity_WhenExists() {
        // GIVEN
        String id = "req-1";
        CardReplacementEntity entity = CardReplacementEntity.builder().requestId(id).build();

        // Simulamos que el repo de Spring devuelve un Mono lleno
        when(reactiveRepo.findByRequestId(id)).thenReturn(Mono.just(entity));

        // WHEN & THEN (Usamos .test() de RxJava para verificar)
        adapter.findByRequestId(id)
                .test()
                .assertValue(e -> e.getRequestId().equals(id)) // Verifica que sea la entidad correcta
                .assertComplete(); // Verifica que termine bien
    }

    @Test
    void findByRequestId_ShouldReturnEmptyMaybe_WhenNotExists() {
        // GIVEN
        String id = "req-empty";
        // Simulamos Mono vacío
        when(reactiveRepo.findByRequestId(id)).thenReturn(Mono.empty());

        // WHEN & THEN
        adapter.findByRequestId(id)
                .test()
                .assertNoValues() // Maybe vacío no emite valores
                .assertComplete();
    }

    @Test
    void existsByRequestId_ShouldReturnSingleBoolean() {
        // GIVEN
        String id = "req-1";
        when(reactiveRepo.existsByRequestId(id)).thenReturn(Mono.just(true));

        // WHEN & THEN
        adapter.existsByRequestId(id)
                .test()
                .assertValue(true) // Verifica el booleano
                .assertComplete();
    }

    @Test
    void save_ShouldReturnSingleEntity() {
        // GIVEN
        CardReplacementEntity entity = CardReplacementEntity.builder().requestId("new").build();
        when(reactiveRepo.save(entity)).thenReturn(Mono.just(entity));

        // WHEN & THEN
        adapter.save(entity)
                .test()
                .assertValue(e -> e.getRequestId().equals("new"))
                .assertComplete();
    }
}
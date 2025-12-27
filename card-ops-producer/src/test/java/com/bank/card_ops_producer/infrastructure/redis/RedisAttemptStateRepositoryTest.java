package com.bank.card_ops_producer.infrastructure.redis;

import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisAttemptStateRepositoryTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    // Necesitamos mockear también las "operaciones de valor" (opsForValue)
    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    @InjectMocks
    private RedisAttemptStateRepository repository;

    @Test
    void existsByRequestId_WhenExists_ShouldReturnTrue() {
        // ARRANGE
        // Simulamos que Redis devuelve un Mono.just(true)
        when(redisTemplate.hasKey("card:req:123")).thenReturn(Mono.just(true));

        // ACT
        TestObserver<Boolean> observer = repository.existsByRequestId("123").test();

        // ASSERT
        observer.assertComplete();
        observer.assertValue(true);
    }

    @Test
    void saveFirstAttempt_ShouldSaveValueOne() {
        // ARRANGE
        // Primero configuramos que redisTemplate.opsForValue() devuelva nuestro mock valueOps
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        // Luego simulamos el set exitoso
        when(valueOps.set("card:req:123", "1")).thenReturn(Mono.just(true));

        // ACT
        TestObserver<Boolean> observer = repository.saveFirstAttempt("123").test();

        // ASSERT
        observer.assertComplete();
        observer.assertValue(true);
    }

    @Test
    void saveEventsSnapshot_ShouldSaveJsonWithTTL() {
        // ARRANGE
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        // Simulamos set con TTL
        when(valueOps.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.just(true));

        // ACT
        TestObserver<Boolean> observer = repository
                .saveEventSnapshot("123", "{json}", Duration.ofMinutes(10))
                .test();

        // ASSERT
        observer.assertComplete();
        observer.assertValue(true);
    }
}
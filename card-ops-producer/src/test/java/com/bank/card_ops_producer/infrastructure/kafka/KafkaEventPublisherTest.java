package com.bank.card_ops_producer.infrastructure.kafka;

import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaEventPublisher publisher;

    @Test
    void publish_WhenKafkaSendsSuccessfully_ShouldEmitSuccess() {
        // ARRANGE
        String topic = "topic-test";
        String key = "key-123";
        Object value = "some-data";
        SendResult<String, Object> mockResult = mock(SendResult.class);

        // Simulamos un Future que se completa exitosamente de inmediato
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mockResult);

        when(kafkaTemplate.send(topic, key, value)).thenReturn(future);

        // ACT
        TestObserver<SendResult<String, Object>> observer = publisher.publish(topic, key, value).test();

        // ASSERT
        observer.assertComplete();      // El Single terminó bien
        observer.assertNoErrors();      // No hubo error
        observer.assertValue(mockResult); // El valor retornado es el SendResult
    }

    @Test
    void publish_WhenKafkaFails_ShouldEmitError() {
        // ARRANGE
        String topic = "topic-fail";
        String key = "key-error";
        Object value = "bad-data";
        RuntimeException kafkaException = new RuntimeException("Kafka is down!");

        // Simulamos un Future que falla
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(kafkaException);

        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        // ACT
        TestObserver<SendResult<String, Object>> observer = publisher.publish(topic, key, value).test();

        // ASSERT
        observer.assertError(kafkaException); // Verifica que el Single explotó con nuestra excepción
        observer.assertError(ex -> "Kafka is down!".equals(ex.getMessage()));
    }
}
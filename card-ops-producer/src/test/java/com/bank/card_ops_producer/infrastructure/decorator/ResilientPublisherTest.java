package com.bank.card_ops_producer.infrastructure.decorator;

import com.bank.card_ops_producer.domain.port.EventPublisher;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilientPublisherTest {

    // Mockeamos la interfaz que está siendo decorada (el KafkaEventPublisher real)
    @Mock
    private EventPublisher<Object> delegate;

    // Inyectamos el mock dentro de nuestra clase ResilientPublisher
    @InjectMocks
    private ResilientPublisher publisher;

    @Test
    void publish_ShouldDelegateToInnerPublisher() {
        // ARRANGE
        String topic = "test-topic";
        String key = "key-1";
        Object value = "payload";

        // Simulamos un resultado exitoso (no necesitamos instanciar SendResult real, un mock basta)
        SendResult<String, Object> mockResult = mock(SendResult.class);

        // Cuando llamen al delegado, retorna éxito
        when(delegate.publish(topic, key, value)).thenReturn(Single.just(mockResult));

        // ACT
        TestObserver<SendResult<String, Object>> observer = publisher.publish(topic, key, value).test();

        // ASSERT
        observer.assertComplete();      // Terminó bien
        observer.assertValue(mockResult); // Retornó lo que dijo el delegado

        // VERIFICACIÓN CLAVE: Aseguramos que ResilientPublisher llamó al método del delegado
        verify(delegate).publish(topic, key, value);
    }
}
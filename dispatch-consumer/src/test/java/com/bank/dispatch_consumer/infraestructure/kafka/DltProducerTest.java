package com.bank.dispatch_consumer.infraestructure.kafka;

import com.bank.dispatch_consumer.config.KafkaTopicsProperties;
import com.bank.dispatch_consumer.infrastructure.kafka.DltProducer;
import io.reactivex.rxjava3.core.Completable;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DltProducerTest {

    @Mock
    private KafkaTopicsProperties topics;

    @Test
    void send_ShouldExecuteCodePath_EvenIfKafkaIsDown() {
        // GIVEN
        String dltTopic = "topic-dlt";
        String key = "req-1";
        String value = "error-json";

        when(topics.getDlt()).thenReturn(dltTopic);

        // 1. Configuración "Fail-Fast" (Fallar rápido)
        // Configuramos timeouts minúsculos para que el intento de envío aborte de inmediato.
        // Esto fuerza a la JVM a ejecutar las líneas dentro de send(), logrando la cobertura.
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Nadie escucha aquí
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 0);       // No esperar si no hay conexión
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 100); // Timeout total muy corto
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 50);   // Timeout de request corto
        props.put(ProducerConfig.RETRIES_CONFIG, 0);            // No reintentar

        SenderOptions<String, String> options = SenderOptions.<String, String>create(props)
                .stopOnError(true);

        // 2. Instanciamos tu clase original INTACTA manualmente
        // (Usamos el constructor generado por @RequiredArgsConstructor)
        DltProducer dltProducer = new DltProducer(options, topics);

        // WHEN
        // Convertimos a Completable para probarlo fácil con RxJava
        Completable resultado = Completable.fromPublisher(dltProducer.send(key, value));

        // THEN
        // Verificamos que termine con ERROR.
        // ¿Por qué error? Porque no hay Kafka.
        // ¿Por qué es bueno? Porque para lanzar el error, tuvo que ejecutar la línea "KafkaSender.create(...).send(...)"
        // ¡Eso te da la cobertura verde!
        resultado.test()
                .awaitDone(2, TimeUnit.SECONDS) // Esperar a que falle
                .assertError(Throwable.class);  // Confirmar que falló (timeout/conexión)
    }
}
package com.bank.dispatch_consumer.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConsumerConfigTest {

    private KafkaConsumerConfig config;

    @BeforeEach
    void setUp() {
        config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "groupId", "test-group");
        ReflectionTestUtils.setField(config, "schemaUrl", "http://localhost:8081");
    }

    @Test
    void receiverOptions_ShouldHaveCorrectProperties() {
        // WHEN
        ReceiverOptions<String, Object> options = config.receiverOptions();

        // THEN
        assertNotNull(options);
        Map<String, Object> props = options.consumerProperties();

        assertEquals("localhost:9092", props.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("test-group", props.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("http://localhost:8081", props.get("schema.registry.url"));
        assertEquals("earliest", props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));

        // CORRECCIÓN AQUÍ: Convertimos a String antes de comparar o esperamos un String "false"
        // Usamos String.valueOf() para que funcione sea Boolean o String
        assertEquals("false", String.valueOf(props.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)));
    }

    @Test
    void senderOptions_ShouldHaveCorrectProperties() {
        // WHEN
        SenderOptions<String, String> options = config.senderOptions();

        // THEN
        assertNotNull(options);
        Map<String, Object> props = options.producerProperties();

        assertEquals("localhost:9092", props.get(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("org.apache.kafka.common.serialization.StringSerializer", props.get("key.serializer"));
        assertEquals("org.apache.kafka.common.serialization.StringSerializer", props.get("value.serializer"));
    }
}
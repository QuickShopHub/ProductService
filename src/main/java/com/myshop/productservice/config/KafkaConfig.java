package com.myshop.productservice.config;


import com.myshop.productservice.dto.CommentIdDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic newTopicUpdate(){
        log.info("updateElastic topic created");
        return new NewTopic("updateElastic", 1, (short)1);
    }

    @Bean
    public NewTopic newTopicDelete(){
        log.info("deleteElastic topic created");
        return new NewTopic("deleteElastic", 1, (short)1);
    }




    // Общие настройки
    private Map<String, Object> baseConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "updateConsumer_v2");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Отключаем headers
        return props;
    }


    @Bean
    public ConsumerFactory<String, CommentIdDTO> productConsumerFactory() {
        Map<String, Object> props = baseConsumerConfigs();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.myshop.productservice.dto.CommentIdDTO");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CommentIdDTO> kafkaListenerContainerFactoryUpdate() {
        ConcurrentKafkaListenerContainerFactory<String, CommentIdDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productConsumerFactory());

        // Более надёжная обработка ошибок
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (consumerRecord, e) -> {
                    log.error("Error processing product update: {}", consumerRecord.value(), e);
                },
                new FixedBackOff(1000L, 2)
        );
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

}

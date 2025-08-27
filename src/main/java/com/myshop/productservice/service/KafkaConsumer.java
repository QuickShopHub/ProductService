package com.myshop.productservice.service;

import com.myshop.productservice.dto.CommentIdDTO;
import com.myshop.productservice.dto.ProductIdDTO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Slf4j
@Service
public class KafkaConsumer {

    private final KafkaProducer kafkaProducer;

    public KafkaConsumer(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @KafkaListener(topics = "updateCountComments", containerFactory = "kafkaListenerContainerFactoryUpdate")
    public void consumeUpdate(CommentIdDTO commentIdDTO) {
        if (commentIdDTO == null) {
            log.warn("commentIdDTO = null");
            return;
        }
        log.info("Received message from topic `updateCountComments`: {}", commentIdDTO.getId());
        UUID id = commentIdDTO.getId();
        kafkaProducer.updateCommCount(id);
    }

    @KafkaListener(topics = "updateCountSold", containerFactory = "kafkaListenerContainerFactoryUpdateSold")
    public void updateSold(ProductIdDTO productIdDTO) {
        if(productIdDTO == null){
            log.warn("commentIdDTO = null");
            return;
        }

        kafkaProducer.updateSold(productIdDTO.getId());
    }

}

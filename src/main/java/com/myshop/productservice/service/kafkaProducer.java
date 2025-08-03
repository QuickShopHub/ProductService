package com.myshop.productservice.service;


import com.myshop.productservice.dto.searchService.ProductForSearch;
import com.myshop.productservice.repository.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class kafkaProducer {

    private final KafkaTemplate<String, ProductForSearch> kafkaTemplateUpdate;


    private final KafkaTemplate<String, List<UUID>> kafkaTemplateDelete;

    public kafkaProducer(KafkaTemplate<String, ProductForSearch> kafkaTemplate, KafkaTemplate<String, List<UUID>> kafkaTemplateDelete) {
        this.kafkaTemplateUpdate = kafkaTemplate;
        this.kafkaTemplateDelete = kafkaTemplateDelete;
    }


    public ProductForSearch getProductForSearchFromProduct(Product product) {
        ProductForSearch newProductForSearch = new ProductForSearch();

        newProductForSearch.setId(product.getId());
        newProductForSearch.setName(product.getName());
        newProductForSearch.setPrice(product.getPrice());
        newProductForSearch.setDescription(product.getDescription());
        newProductForSearch.setQuantitySold(product.getQuantitySold());
        newProductForSearch.setRating(product.getRating());
        newProductForSearch.setArticle(product.getArticle());

        return newProductForSearch;
    }

    public void sendUpdate(ProductForSearch productForSearch) {
        log.info("send to topic `updateElastic` productForSearch: {}", productForSearch);
        kafkaTemplateUpdate.send("updateElastic", productForSearch);
    }


    public void sendDelete(List<UUID> ids) {
        log.info("send to topic `deleteElastic` {} ids to delete", ids.size());
        kafkaTemplateDelete.send("deleteElastic", ids);
    }

}

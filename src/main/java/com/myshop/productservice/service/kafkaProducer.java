package com.myshop.productservice.service;


import com.myshop.productservice.dto.searchService.DeleteDTO;
import com.myshop.productservice.dto.searchService.ProductForSearch;
import com.myshop.productservice.repository.Avatar;
import com.myshop.productservice.repository.AvatarRepository;
import com.myshop.productservice.repository.Product;
import com.myshop.productservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class kafkaProducer {

    private final KafkaTemplate<String, ProductForSearch> kafkaTemplateUpdate;

    private final AvatarRepository avatarRepository;


    private final KafkaTemplate<String, DeleteDTO> kafkaTemplateDelete;
    private final ProductRepository productRepository;

    public kafkaProducer(KafkaTemplate<String, ProductForSearch> kafkaTemplate, AvatarRepository avatarRepository, KafkaTemplate<String, DeleteDTO> kafkaTemplateDelete, ProductRepository productRepository) {
        this.kafkaTemplateUpdate = kafkaTemplate;
        this.avatarRepository = avatarRepository;
        this.kafkaTemplateDelete = kafkaTemplateDelete;
        this.productRepository = productRepository;
    }


    public ProductForSearch getProductForSearchFromProduct(Product product) {
        ProductForSearch newProductForSearch = new ProductForSearch();

        newProductForSearch.setId(product.getId());
        newProductForSearch.setName(product.getName());
        newProductForSearch.setPrice(product.getPrice());
        newProductForSearch.setDescription(product.getDescription());
        newProductForSearch.setQuantitySold(product.getQuantitySold() != null ? product.getQuantitySold() : 0);
        newProductForSearch.setRating(product.getRating() != null ? product.getRating() : null);
        newProductForSearch.setArticle(product.getArticle());

        Avatar avatar = avatarRepository.findByProductId(product.getId());
        newProductForSearch.setUrl(avatar.getUrl());

        newProductForSearch.setCountComments(productRepository.getCountCommentsByProductId(product.getId()));



        return newProductForSearch;
    }

    public void sendUpdate(ProductForSearch productForSearch) {
        log.info("send to topic `updateElastic` productForSearch: {}", productForSearch);
        kafkaTemplateUpdate.send("updateElastic", productForSearch);
    }


    public void sendDelete(List<UUID> ids) {
        log.info("send to topic `deleteElastic` {}  to delete", ids);

        DeleteDTO deleteDTO = new DeleteDTO();
        deleteDTO.setIds(ids);
        kafkaTemplateDelete.send("deleteElastic", deleteDTO);
    }

}

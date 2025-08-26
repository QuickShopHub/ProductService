package com.myshop.productservice.service;


import com.myshop.productservice.repository.Product;
import com.myshop.productservice.repository.ProductRepository;
import com.myshop.productservice.repository.UpdateRatingEntity;
import com.myshop.productservice.repository.UpdateRatingRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class RatingService {
    private static final String REDIS_KEY_PREFIX = "product:";
    private final UpdateRatingRepository updateRatingRepository;
    private final ProductRepository productRepository;
    private final KafkaProducer kafkaProducer;
    private final RedisTemplate<String, Product> redisTemplate;

    public RatingService(UpdateRatingRepository updateRatingRepository, ProductRepository productRepository, KafkaProducer kafkaProducer, RedisTemplate<String, Product> redisTemplate) {
        this.updateRatingRepository = updateRatingRepository;
        this.productRepository = productRepository;
        this.kafkaProducer = kafkaProducer;
        this.redisTemplate = redisTemplate;
    }

    public ResponseEntity<UpdateRatingEntity> setRating(UpdateRatingEntity updateRatingEntity){

        Optional<UpdateRatingEntity> optionalUpdateRatingEntity = updateRatingRepository.findByProductIdAndUserId(updateRatingEntity.getProductId(), updateRatingEntity.getUserId());
        Optional<Product> productOptional = productRepository.findById(updateRatingEntity.getProductId());
        if(productOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        if(optionalUpdateRatingEntity.isPresent()){
            optionalUpdateRatingEntity.get().setGrade(updateRatingEntity.getGrade());
            updateRatingRepository.save(optionalUpdateRatingEntity.get());
            updateProductRating(updateRatingEntity.getProductId(), productOptional.get());
            return ResponseEntity.ok(optionalUpdateRatingEntity.get());
        }
        else{
            updateRatingEntity.setId(UUID.randomUUID());
            updateRatingRepository.save(updateRatingEntity);
            updateProductRating(updateRatingEntity.getProductId(), productOptional.get());
            return  ResponseEntity.ok(updateRatingEntity);
        }
    }

    private void updateProductRating(UUID id, Product product){
        long count = updateRatingRepository.getCountRatingByProductId(id);
        long sum = updateRatingRepository.getSumRatingByProductId(id);
        if(count > 0){
            product.setRating(BigDecimal.valueOf((double) sum / count));
        }
        else{
            product.setRating(BigDecimal.ZERO);
        }
        productRepository.save(product);
        redisTemplate.delete(REDIS_KEY_PREFIX+product.getId());

        kafkaProducer.sendUpdate(kafkaProducer.getProductForSearchFromProduct(product));
    }

    public ResponseEntity<UpdateRatingEntity> getRating(UUID product_id, UUID user_id){
        Integer grade = updateRatingRepository.getGradeByProductIdAndUserId(product_id, user_id);
        UpdateRatingEntity  updateRatingEntity = new UpdateRatingEntity();
        if(grade == null){
            updateRatingEntity.setGrade(0);
        }
        else{
            updateRatingEntity.setGrade(grade);
        }
        return ResponseEntity.ok(updateRatingEntity);
    }
}

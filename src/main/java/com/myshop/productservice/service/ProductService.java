package com.myshop.productservice.service;

import com.myshop.productservice.dto.UpdateAvatar;
import com.myshop.productservice.dto.UpdateRating;
import com.myshop.productservice.repository.Avatar;
import com.myshop.productservice.repository.AvatarRepository;
import com.myshop.productservice.repository.Product;
import com.myshop.productservice.repository.ProductRepository;
import com.myshop.productservice.dto.ProductUpdatePrice;
import com.myshop.productservice.dto.searchService.ProductForSearch;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;


@Slf4j
@Service
public class ProductService {

    private static final String REDIS_KEY_PREFIX = "product:";

    private final kafkaProducer  kafkaProducer;

    private final RedisTemplate<String, Product> redisTemplate;

    private final ProductRepository productRepository;
    private final AvatarRepository avatarRepository;

    @Autowired
    public ProductService(kafkaProducer kafkaProducer, RedisTemplate<String, Product> redisTemplate, ProductRepository productRepository, AvatarRepository avatarRepository) {
        this.kafkaProducer = kafkaProducer;
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
        this.avatarRepository = avatarRepository;
    }

    public List<Product> getProductsById(List<UUID> ids) {

        if(ids == null || ids.isEmpty()){
            throw new IllegalArgumentException("Ids is null");
        }

        List<Product> result = new ArrayList<>();
        List<UUID> idsToFetchFromDb = new ArrayList<>();

        // Шаг 1: Получаем из Redis
        for (UUID id : ids) {
            Product product = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + id);
            if (product != null) {
                log.info("НАШЛИ В РЭДИС");
                result.add(product);
            } else {
                idsToFetchFromDb.add(id);
            }
        }


        List<Product> productsFromDb = productRepository.findAllById(idsToFetchFromDb);

        for (Product product : productsFromDb) {
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + product.getId(), product, Duration.ofMinutes(1));
            result.add(product);
        }


        //сортировка по id как пришли на сервер
        List<Product> onSend = new ArrayList<>();
        for(Product product : result) {
            for (Product value : result) {
                if (product.getId().equals(value.getId())) {
                    onSend.add(value);
                }
            }
        }
        return onSend;
    }


    public Product addProduct(Product product) {


        Optional<Product> temp = productRepository.findByArticle(product.getArticle());

        if(temp.isPresent()) {
            throw new IllegalArgumentException("Product with the same article already exists. Id: " + product.getId());
        }

        product.setId(UUID.randomUUID());
        product.setCreatedAt(LocalDate.now());

        if (product.getAvatar() != null) {
            product.getAvatar().setProduct(product);
        }


        ProductForSearch productForSearch = ProductForSearch.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .article(product.getArticle())
                .quantitySold(product.getQuantitySold() == null ? 0 : product.getQuantitySold())
                .rating(product.getRating() == null ? BigDecimal.ZERO : product.getRating())
                .build();
        kafkaProducer.sendUpdate(productForSearch);

        return productRepository.save(product);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }



    public Product updateAll(Product product) {
        Optional<Product> temp = productRepository.findById(product.getId());

        if(temp.isEmpty()) {
            throw new IllegalArgumentException("Product with id: " + product.getId() + "is not found");
        }

        Optional<Product> checkArticle = productRepository.findByArticle(product.getArticle());
        if(checkArticle.isPresent() && !checkArticle.get().getId().equals(product.getId())) {
            throw new IllegalArgumentException("Product with the same article already exists. Id: " + checkArticle.get().getId());
        }

        redisTemplate.delete(REDIS_KEY_PREFIX+product.getId());

        Product newProduct = temp.get();

        //Меняем поля
        if(product.getAvatar() != null) {
            newProduct.getAvatar().setUrl(product.getAvatar().getUrl());
        }
        if(product.getName() != null) {
            newProduct.setName(product.getName());
        }
        if(product.getPrice() != null) {
            newProduct.setPrice(product.getPrice());
        }
        if(product.getDescription() != null) {
            newProduct.setDescription(product.getDescription());
        }
        if(product.getArticle() != null) {
            newProduct.setArticle(product.getArticle());
        }
        if(product.getQuantity() != null){
            newProduct.setQuantity(product.getQuantity());
        }
        if(product.getRating() != null) {
            newProduct.setRating(product.getRating());
        }
        if(product.getActive() != null){
            newProduct.setActive(product.getActive());
        }
        if(product.getCreatedAt() != null) {
            newProduct.setCreatedAt(product.getCreatedAt());
        }
        if(product.getIdVendor() != null) {
            newProduct.setIdVendor(product.getIdVendor());
        }
        if(product.getCountGrades() != null) {
            newProduct.setCountGrades(product.getCountGrades());
        }
        if(product.getQuantitySold() != null){
            newProduct.setQuantitySold(product.getQuantitySold());
        }

        kafkaProducer.sendUpdate(kafkaProducer.getProductForSearchFromProduct(newProduct));

        return productRepository.save(newProduct);
    }




    public Product updatePrice(ProductUpdatePrice productUpdatePrice) {
        UUID id = productUpdatePrice.getId();
        Optional<Product> temp = productRepository.findById(id);
        if(temp.isEmpty()) {
            throw new IllegalArgumentException("Product with id: " + productUpdatePrice.getId() + "is not found");
        }
        Product update = temp.get();
        update.setPrice(productUpdatePrice.getPrice());

        redisTemplate.delete(REDIS_KEY_PREFIX+productUpdatePrice.getId());

        kafkaProducer.sendUpdate(kafkaProducer.getProductForSearchFromProduct(update));

        return productRepository.save(update);
    }


    public long deleteProducts(List<UUID> ids) {
        long count = 0;

        for (UUID id : ids) {
            Optional<Product> temp = productRepository.findById(id);
            if(temp.isPresent()) {
                count++;
            }
            redisTemplate.delete(REDIS_KEY_PREFIX+id);
        }

        productRepository.deleteAllById(ids);
        kafkaProducer.sendDelete(ids);


        return count;
    }

    public Product updateRatingValue(UpdateRating updateRating) {
        UUID id = updateRating.getIdProduct();
        Optional<Product> temp = productRepository.findById(id);
        if(temp.isEmpty()) {
            throw new IllegalArgumentException("Product with id: " + updateRating.getIdProduct() + "is not found");
        }

        Product update = temp.get();

        BigDecimal rating = update.getRating();
        long countGrades =  update.getCountGrades();

        BigDecimal longAsBigDecimal = BigDecimal.valueOf(countGrades);

        if (rating.compareTo(BigDecimal.ZERO) != 0) {
            rating = rating.multiply(longAsBigDecimal);
            rating = rating.add(updateRating.getGrade());
        }
        else{
            rating = updateRating.getGrade();
        }

        countGrades+=1;

        rating = rating.divide(BigDecimal.valueOf(countGrades), 1, RoundingMode.HALF_UP);

        update.setRating(rating);
        update.setCountGrades(countGrades);

        redisTemplate.delete(REDIS_KEY_PREFIX+updateRating.getIdProduct());

        kafkaProducer.sendUpdate(kafkaProducer.getProductForSearchFromProduct(update));

        return productRepository.save(update);
    }

    //-------------------АВАТАРКИ и ФОТКИ----------------------------------------
    @Transactional
    public UpdateAvatar updateAvatar(UpdateAvatar updateAvatar) {
        if(avatarRepository.setUrlByProductId(updateAvatar.getAvatarUrl(), updateAvatar.getId()) == 1){
            return updateAvatar;
        }
        throw new IllegalArgumentException("Product with id: " + updateAvatar.getId() + " not found");
    }


    public List<String> getAvatar(List<UUID> ids){
        if(ids.isEmpty()){
            return List.of();
        }

        List<Avatar> temp = avatarRepository.getUrlsByProductIds(ids);

        List<String> ans = new ArrayList<>();

        for(UUID id : ids){
            for(Avatar avatar : temp){
                if(avatar.getProduct().getId().equals(id)){
                    ans.add(avatar.getUrl());
                }
            }
        }

        return ans;
    }

    //todo добавить реализацию фоток

}

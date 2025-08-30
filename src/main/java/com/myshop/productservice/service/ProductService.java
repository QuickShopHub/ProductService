package com.myshop.productservice.service;

import com.myshop.productservice.dto.*;
import com.myshop.productservice.filter.JwtAuthFilter;
import com.myshop.productservice.repository.Product;
import com.myshop.productservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;


@Slf4j
@Service
public class ProductService {

    private static final String REDIS_KEY_PREFIX = "product:";

    private final KafkaProducer kafkaProducer;

    private final RedisTemplate<String, Product> redisTemplate;

    private final ProductRepository productRepository;

    private final PhotoService photoService;

    private final JwtAuthFilter jwtAuthFilter;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    public ProductService(KafkaProducer kafkaProducer, RedisTemplate<String, Product> redisTemplate, ProductRepository productRepository, PhotoService photoService, JwtAuthFilter jwtAuthFilter) {
        this.kafkaProducer = kafkaProducer;
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
        this.photoService = photoService;
        this.jwtAuthFilter = jwtAuthFilter;
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

    public static String generateRandomDigits(int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // Генерируем случайную цифру от 0 до 9
            int digit = RANDOM.nextInt(10);
            result.append(digit);
        }
        return result.toString();
    }

    @Transactional
    public ResponseEntity<Product> addProduct(NewProduct newProduct) {

        Product product = newProduct.getProduct();
        if(product.getArticle() == null){
            product.setArticle(generateRandomDigits(15));
        }

        Optional<Product> temp = productRepository.findByArticle(product.getArticle());

        if(temp.isPresent()) {
            product.setMessage("Article is occupied");
            return ResponseEntity.badRequest().body(product);
        }

        product.setId(UUID.randomUUID());
        product.setCreatedAt(LocalDate.now());
        productRepository.save(product);
        if(newProduct.getPhotos() != null || newProduct.getPhotos().isEmpty()) {

            photoService.photoForNewProduct(newProduct.getPhotos(), product);
        }

        UpdateAvatar updateAvatar = new  UpdateAvatar();

        updateAvatar.setAvatar(newProduct.getAvatar());
        updateAvatar.getAvatar().setProduct(product);
        updateAvatar.getAvatar().setId(UUID.randomUUID());

        if(newProduct.getAvatar().getUrl() != null) {
            photoService.setAvatar(updateAvatar);
        }
        kafkaProducer.sendUpdate(kafkaProducer.getProductForSearchFromProduct(product));

        return ResponseEntity.ok(product);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }



    public ResponseEntity<Product> updateAll(Product product) {

        if(!jwtAuthFilter.tryDo(product.getIdVendor())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


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

        return ResponseEntity.ok(productRepository.save(newProduct));
    }




    public ResponseEntity<Product> updatePrice(ProductUpdatePrice productUpdatePrice) {
        UUID id = productUpdatePrice.getId();
        Optional<Product> temp = productRepository.findById(id);
        if(temp.isEmpty()) {
            throw new IllegalArgumentException("Product with id: " + productUpdatePrice.getId() + "is not found");
        }
        Product update = temp.get();

        if(!jwtAuthFilter.tryDo(update.getIdVendor())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        update.setPrice(productUpdatePrice.getPrice());

        redisTemplate.delete(REDIS_KEY_PREFIX+productUpdatePrice.getId());

        kafkaProducer.sendUpdate(kafkaProducer.getProductForSearchFromProduct(update));

        return ResponseEntity.ok(productRepository.save(update));
    }


    public List<Product> deleteProducts(List<UUID> ids) {

        List<Product> deletedProduct = new ArrayList<>();
        for (UUID id : ids) {
            Optional<Product> temp = productRepository.findById(id);
            if(temp.isPresent() && jwtAuthFilter.tryDo(temp.get().getIdVendor())) {
                redisTemplate.delete(REDIS_KEY_PREFIX+id);
                deletedProduct.add(temp.get());
            }
        }
        productRepository.deleteAll(deletedProduct);
        kafkaProducer.sendDelete(ids);
        return deletedProduct;
    }


    @Scheduled(cron = "@daily")
    public void updateCountComments() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            productRepository.setCommentsCount(productRepository.getCountCommentsByProductId(product.getId()));
        }
    }


    public ResponseEntity<List<Product>> getProductsFromJsonList(ProductsIdList productsId){

        List<Product> productList = productRepository.findAllById(productsId.getIds());

        List<Product> result =  new ArrayList<>();

        for(UUID productId : productsId.getIds()){
            for(Product product : productList){
                if(productId.equals(product.getId())){
                    result.add(product);
                }
            }
        }
        return ResponseEntity.ok(result);
    }
}

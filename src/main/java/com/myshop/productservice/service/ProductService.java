package com.myshop.productservice.service;

import com.myshop.productservice.repository.Product;
import com.myshop.productservice.repository.ProductRepository;
import com.myshop.productservice.dto.ProductUpdatePrice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.*;


@Slf4j
@Service
public class ProductService {


    private final ProductRepository productRepository;


    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getProductsById(List<UUID> ids) {

        if(ids == null || ids.isEmpty()){
            throw new IllegalArgumentException("Ids is null");
        }

        List<Product> list = productRepository.findAllById(ids);

        List<Product> onSend = new ArrayList<>();

        for(Product product : list) {
            for (Product value : list) {
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
        return productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product updateAll(Product product) {
        UUID id = product.getId();
        Optional<Product> temp = productRepository.findById(id);
        if(temp.isEmpty()) {
            throw new IllegalArgumentException("Product with id: " + product.getId() + "is not found");
        }
        return productRepository.save(product);
    }

    public Product updatePrice(ProductUpdatePrice productUpdatePrice) {
        UUID id = productUpdatePrice.getId();
        Optional<Product> temp = productRepository.findById(id);
        if(temp.isEmpty()) {
            throw new IllegalArgumentException("Product with id: " + productUpdatePrice.getId() + "is not found");
        }
        Product update = temp.get();
        update.setPrice(productUpdatePrice.getPrice());
        return productRepository.save(update);
    }

    public long deleteProducts(List<UUID> ids) {
        long count = 0;

        for (UUID id : ids) {
            Optional<Product> temp = productRepository.findById(id);
            if(temp.isPresent()) {
                count++;
            }
        }
        productRepository.deleteAllById(ids);
        return count;
    }
}

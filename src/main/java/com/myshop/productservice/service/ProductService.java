package com.myshop.productservice.service;

import com.myshop.productservice.repository.Product;
import com.myshop.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {


    private final ProductRepository productRepository;


    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProductById(UUID id) {
        Optional<Product> temp = productRepository.findById(id);
        if(temp.isPresent()) {
            return temp.get();
        }
        else{
            throw new IllegalArgumentException("Product not found. Id: " + id);
        }
    }

}

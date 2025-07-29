package com.myshop.productservice.controller;


import com.myshop.productservice.repository.Product;
import com.myshop.productservice.service.ProductService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping(path = "/getProduct")
    public Product getProductById(@RequestParam(name = "id") UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping(path = "/getProducts")
    public List<Product> getProductsById(@RequestParam(name = "id") List<UUID> id) {
        return productService.getProductsById(id);
    }

    @PostMapping(path = "/addProduct")
    public Product addProduct(@Valid @RequestBody Product product) {
        return productService.addProduct(product);
    }

    @GetMapping(path = "/getAll")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

}

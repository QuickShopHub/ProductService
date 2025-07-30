package com.myshop.productservice.controller;


import com.myshop.productservice.repository.Product;
import com.myshop.productservice.dto.ProductUpdatePrice;
import com.myshop.productservice.service.ProductService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public Product getProductById(@Valid @RequestParam(name = "id") UUID id) {

        return productService.getProductById(id);
    }

    @GetMapping(path = "/getProducts")
    public List<Product> getProductsById(@Valid @RequestParam(name = "id") List<UUID> id) {
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

    @PutMapping(path = "/updateProduct")
    public Product updateProduct(@Valid @RequestBody Product product) {

        return productService.updateAll(product);
    }

    @PatchMapping(path = "/updatePrice")
    public Product updatePrice(@Valid @RequestBody ProductUpdatePrice product) {
        return productService.updatePrice(product);
    }

    @DeleteMapping(path = "/deleteProducts")
    public ResponseEntity<String>  deleteProducts(@Valid @RequestParam(name = "ids") List<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body("Список ID пуст");
        }

        return ResponseEntity.ok("Удалено: " + productService.deleteProducts(ids) + " записей");
    }

    @DeleteMapping(path = "/deleteProduct")
    public ResponseEntity<String>  deleteProduct(@Valid @RequestParam(name = "id") UUID id) {

        if (id == null) {
            return ResponseEntity.badRequest().body("Поле ID пусто");
        }
        if(productService.deleteProduct(id) == 1){
            return ResponseEntity.ok("Запись: " + id + " удалена");
        }
        return ResponseEntity.badRequest().body("Ошибка при удалении");
    }

}

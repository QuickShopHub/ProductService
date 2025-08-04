package com.myshop.productservice.controller;


import com.myshop.productservice.dto.UpdateAvatar;
import com.myshop.productservice.dto.UpdateRating;
import com.myshop.productservice.repository.Avatar;
import com.myshop.productservice.repository.Product;
import com.myshop.productservice.dto.ProductUpdatePrice;
import com.myshop.productservice.service.ProductService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping()
    public List<Product> getProductsById(@Valid @RequestParam(name = "id") List<UUID> id) {
        return productService.getProductsById(id);
    }

    @PostMapping()
    public Product addProduct(@Valid @RequestBody Product product) {
        return productService.addProduct(product);
    }

    @GetMapping(path = "/allProducts")
    public Page<Product> getAllProducts(
            @RequestParam(defaultValue = "0", name = "page", required = false) int page,
            @RequestParam(defaultValue = "1", name = "size", required = false) int size) {
        return productService.getAllProducts(PageRequest.of(page, size));
    }

    @PutMapping()
    public Product updateProduct(@Valid @RequestBody Product product) {

        return productService.updateAll(product);
    }

    @PatchMapping(path = "/price")
    public Product updatePrice(@Valid @RequestBody ProductUpdatePrice product) {
        return productService.updatePrice(product);
    }

    @DeleteMapping()
    public ResponseEntity<String>  deleteProducts(@Valid @RequestParam(name = "id") List<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body("Список ID пуст");
        }

        return ResponseEntity.ok("Удалено: " + productService.deleteProducts(ids) + " записей");
    }

    @PatchMapping(path = "/rating")
    public Product updateRating(@Valid @RequestBody UpdateRating updateRating) {
        return productService.updateRatingValue(updateRating);
    }

    //-------------------АВАТАРКИ и ФОТКИ----------------------------------------

    @PutMapping(path = "/avatar")
    public UpdateAvatar updateAvatar(@Valid @RequestBody UpdateAvatar updateAvatar) {
        return productService.updateAvatar(updateAvatar);
    }

    @GetMapping(path = "/avatar")
    public List<String> getAvatar(@RequestParam(name = "id") List<UUID> ids) {
        log.info("Ids length" + ids.size());
        return productService.getAvatar(ids);
    }

}

package com.myshop.productservice.controller;


import com.myshop.productservice.dto.*;
import com.myshop.productservice.repository.Photos;
import com.myshop.productservice.repository.Product;
import com.myshop.productservice.repository.ProductRepository;
import com.myshop.productservice.repository.UpdateRatingEntity;
import com.myshop.productservice.service.PhotoService;
import com.myshop.productservice.service.ProductService;

import com.myshop.productservice.service.RatingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/products")
public class ProductController {

    private final ProductService productService;

    private final PhotoService photoService;

    private final RatingService ratingService;


    public ProductController(ProductService productService, PhotoService photoService, RatingService ratingService) {
        this.productService = productService;
        this.photoService = photoService;
        this.ratingService = ratingService;
    }


    @GetMapping(path = "/id")
    public List<Product> getProductsById(@Valid @RequestParam(name = "id") List<UUID> id) {
        return productService.getProductsById(id);
    }

    @PostMapping(path = "/elements")
    public ResponseEntity<List<Product>> getProductsFromJson(@RequestBody @Valid ProductsIdList productsId) {
        return productService.getProductsFromJsonList(productsId);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping()
    public Product addProduct(@RequestBody NewProduct newProduct) {
        return productService.addProduct(newProduct);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/allProducts")
    public ResponseEntity<PagedModel<EntityModel<Product>>> getAllProducts(
            @RequestParam(defaultValue = "0", name = "page", required = false) int page,
            @RequestParam(defaultValue = "1", name = "size", required = false) int size,
            PagedResourcesAssembler<Product> pagedResourcesAssembler) {
        Page<Product> productPage = productService.getAllProducts(PageRequest.of(page, size));
        PagedModel<EntityModel<Product>> pagedModel = pagedResourcesAssembler.toModel(productPage);
        return ResponseEntity.ok(pagedModel);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping()
    public ResponseEntity<Product> updateProduct(@Valid @RequestBody Product product) {
        return productService.updateAll(product);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PatchMapping(path = "/price")
    public ResponseEntity<Product> updatePrice(@Valid @RequestBody ProductUpdatePrice product) {
        return productService.updatePrice(product);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping()
    public ResponseEntity<List<Product>>  deleteProducts(@Valid @RequestParam(name = "id") List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok( productService.deleteProducts(ids));
    }

    //-------------------АВАТАРКИ и ФОТКИ----------------------------------------

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping(path = "/avatar")
    public UpdateAvatar updateAvatar(@Valid @RequestBody UpdateAvatar updateAvatar) {
        return photoService.setAvatar(updateAvatar);
    }


    @PostMapping(path = "/avatar_ids")
    public ResponseEntity<Map<String, List<String>>> getAvatar(@RequestBody Map<String, List<UUID>> data) {
        return photoService.getAvatar(data.get("ids"));
    }

    @GetMapping(path = "/photo")
    public List<Photos>  getPhotos(@RequestParam(name = "id") UUID id) {
        return photoService.getPhotos(id);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping(path = "/photo/{id}")
    public ResponseEntity<String> deletePhotos(@PathVariable UUID id) {
        return photoService.deletePhoto(id);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping(path = "/photo")
    public ResponseEntity<String> addPhotos(@RequestBody NewPhotos newPhotos) {
        return photoService.addPhotos(newPhotos);
    }


    //=====================================Рейтинг==========================================================

    @PreAuthorize("hasRole('USER')")
    @PostMapping(path = "/rating")
    public ResponseEntity<UpdateRatingEntity> setRating(@RequestBody UpdateRatingEntity updateRatingEntity){
        return ratingService.setRating(updateRatingEntity);
    }

    @GetMapping(path = "/rating")
    public ResponseEntity<UpdateRatingEntity> getRating(@RequestParam UUID product_id, @RequestParam UUID user_id){
        return ratingService.getRating(product_id, user_id);
    }


}

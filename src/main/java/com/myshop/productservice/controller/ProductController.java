package com.myshop.productservice.controller;


import com.myshop.productservice.dto.UpdateAvatar;
import com.myshop.productservice.dto.UpdateRating;
import com.myshop.productservice.repository.Product;
import com.myshop.productservice.dto.ProductUpdatePrice;
import com.myshop.productservice.service.AvatarService;
import com.myshop.productservice.service.ProductService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/products")
public class ProductController {

    private final ProductService productService;

    private final AvatarService avatarService;


    public ProductController(ProductService productService, AvatarService avatarService) {
        this.productService = productService;
        this.avatarService = avatarService;
    }



    @GetMapping(path = "/id")
    public List<Product> getProductsById(@Valid @RequestParam(name = "id") List<UUID> id) {
        return productService.getProductsById(id);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping()
    public Product addProduct(@Valid @RequestBody Product product) {
        return productService.addProduct(product);
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

    @PreAuthorize("hasRole('USER')")
    @PutMapping()
    public Product updateProduct(@Valid @RequestBody Product product) {

        return productService.updateAll(product);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping(path = "/price")
    public Product updatePrice(@Valid @RequestBody ProductUpdatePrice product) {
        return productService.updatePrice(product);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping()
    public ResponseEntity<String>  deleteProducts(@Valid @RequestParam(name = "id") List<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body("Список ID пуст");
        }

        return ResponseEntity.ok("Удалено: " + productService.deleteProducts(ids) + " записей");
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping(path = "/rating")
    public Product updateRating(@Valid @RequestBody UpdateRating updateRating) {
        return productService.updateRatingValue(updateRating);
    }

    //-------------------АВАТАРКИ и ФОТКИ----------------------------------------

    @PreAuthorize("hasRole('USER')")
    @PutMapping(path = "/avatar")
    public UpdateAvatar updateAvatar(@Valid @RequestBody UpdateAvatar updateAvatar) {
        return avatarService.updateAvatar(updateAvatar);
    }


    @GetMapping(path = "/avatar_id")
    public List<String> getAvatar(@RequestParam(name = "id") List<UUID> ids) {
        log.info("Ids length" + ids.size());
        return avatarService.getAvatar(ids);
    }

}

package com.myshop.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;



import java.util.Optional;
import java.util.UUID;


public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(value = "SELECT * FROM products WHERE article = :article", nativeQuery = true)
    Optional<Product> findByArticle(String article);
}

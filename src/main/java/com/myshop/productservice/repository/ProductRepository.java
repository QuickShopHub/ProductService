package com.myshop.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;



import java.util.Optional;
import java.util.UUID;


public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(value = "SELECT * FROM products WHERE article = :article", nativeQuery = true)
    Optional<Product> findByArticle(String article);

    @Query(value = "SELECT COUNT(*) FROM comments WHERE product_id=:id",  nativeQuery = true)
    long getCountCommentsByProductId(UUID id);

    @Modifying
    @Query(value = "INSERT INTO products(count_comments) VALUES(:count)",   nativeQuery = true)
    void setCommentsCount(long count);

    @Query(value = "SELECT COUNT(*) FROM buy_product WHERE product_id=:id",   nativeQuery = true)
    Long countSoldBiProductId(UUID id);
}

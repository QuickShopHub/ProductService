package com.myshop.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UpdateRatingRepository extends JpaRepository<UpdateRatingEntity, UUID> {


    @Query(value = "SELECT * FROM rating WHERE product_id=:productId AND user_id=:userId",  nativeQuery = true)
    Optional<UpdateRatingEntity> findByProductIdAndUserId(UUID productId, UUID userId);

    @Query(value = "SELECT SUM(grade) FROM rating WHERE product_id=:productId",   nativeQuery = true)
    long getSumRatingByProductId(UUID productId);

    @Query(value = "SELECT COUNT(*) FROM rating WHERE product_id=:productId",   nativeQuery = true)
    long getCountRatingByProductId(UUID productId);

    @Query(value = "SELECT grade FROM rating WHERE product_id=:productId AND user_id=:userId",  nativeQuery = true)
    Integer getGradeByProductIdAndUserId(UUID productId,  UUID userId);

}

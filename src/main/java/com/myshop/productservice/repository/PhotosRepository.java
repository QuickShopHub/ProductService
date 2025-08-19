package com.myshop.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PhotosRepository extends JpaRepository<Photos, UUID> {

    @Query(value = "SELECT * FROM photos WHERE product_id=:id",  nativeQuery = true)
    List<Photos> findAllByProductId(UUID id);

}

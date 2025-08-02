package com.myshop.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.util.UUID;

public interface AvatarRepository extends JpaRepository<Avatar, UUID> {

    @Modifying
    @Query(value = "UPDATE avatar SET url = :url WHERE productid = :id", nativeQuery = true)
    int setUrlByProductId(String url, UUID id);

}

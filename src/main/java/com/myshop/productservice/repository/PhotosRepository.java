package com.myshop.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PhotosRepository extends JpaRepository<Photos, UUID> {
}

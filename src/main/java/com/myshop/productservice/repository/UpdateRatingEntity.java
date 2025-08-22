package com.myshop.productservice.repository;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "rating")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRatingEntity {

    @Id
    UUID id;

    @Column(name = "product_id")
    UUID productId;

    @Column(name = "user_id")
    UUID userId;

    int grade;

}

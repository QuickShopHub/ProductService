package com.myshop.productservice.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "photos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Photos {

    @Id
    private UUID id;

    private String url;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}

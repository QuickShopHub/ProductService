package com.myshop.productservice.repository;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.util.UUID;

@RedisHash
@Entity
@Table(name = "avatar")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Avatar {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    private String url;

    @OneToOne
    @JoinColumn(name = "productid")
    private Product product;
}

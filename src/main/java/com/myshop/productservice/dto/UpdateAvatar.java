package com.myshop.productservice.dto;

import lombok.Data;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Data
@Repository
public class UpdateAvatar {
    private UUID id;
    private String avatarUrl;
}

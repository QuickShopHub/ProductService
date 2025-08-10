package com.myshop.productservice.dto;

import com.myshop.productservice.repository.Avatar;
import lombok.Data;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Data
@Repository
public class UpdateAvatar {
    private UUID id;
    private Avatar avatar;
}

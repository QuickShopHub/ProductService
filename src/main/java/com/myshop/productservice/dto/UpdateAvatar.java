package com.myshop.productservice.dto;

import com.myshop.productservice.repository.Avatar;
import lombok.Data;


import java.util.UUID;

@Data
public class UpdateAvatar {
    private UUID id;
    private Avatar avatar;
}

package com.myshop.productservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateRating {

    @NotNull()
    private UUID idProduct;
    @NotNull()
    private BigDecimal grade;

}

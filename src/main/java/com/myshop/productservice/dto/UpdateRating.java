package com.myshop.productservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Repository
public class UpdateRating {

    @NotNull()
    private UUID idProduct;
    @NotNull()
    private BigDecimal grade;

}

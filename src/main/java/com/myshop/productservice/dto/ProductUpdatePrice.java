package com.myshop.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Repository
public class ProductUpdatePrice {

    @NotNull(message = "Id должен быть указан")
    private UUID id;

    @DecimalMin(value = "0.0", message = "Цена не может быть отрицательной")
    @Digits(integer = 10, fraction = 2, message = "Цена должна иметь максимум 2 знака после запятой")
    private BigDecimal price;

}

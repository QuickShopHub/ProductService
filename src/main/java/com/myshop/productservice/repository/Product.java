package com.myshop.productservice.repository;



import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;



import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.UUID;

@RedisHash
@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product{

    @Id
    private UUID id;

    @NotBlank(message = "Имя обязательно")
    private String name;

    @Size(max = 1000, message = "Максимум 1000символов")
    @NotBlank(message = "Размер долен быть указан")
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Цена не может быть отрицательной")
    @Digits(integer = 10, fraction = 2, message = "Цена должна иметь максимум 2 знака после запятой")
    private BigDecimal price;

    @NotBlank(message = "Артикул должен быть указан")
    private String article;

    @DecimalMin(value = "0.0", inclusive = true, message = "Количество не может быть отрицательным")
    @NotNull(message = "Количество должно быть указано")
    private Integer quantity;

    @NotNull(message = "Статус должен быть указан")
    private Boolean active;

    @Column(name = "createdat")
    private LocalDate createdAt;

    @Column(name = "idvendor")
    private UUID idVendor;

    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "countgrades")
    private Long countGrades = 0L;

    @Column(name = "quantitysold")
    private Long quantitySold = 0L;

}

package com.myshop.productservice.repository;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
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
public class Product {

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
    private int quantity;

    @NotNull(message = "Статус должен быть указан")
    private boolean active;

    @Column(name = "createdat")
    private LocalDate createdAt;

    //todo @NotNull(message = "Id продовца должно быть указано")
    @Column(name = "idvendor")
    private UUID idVendor;

    private BigDecimal rating;

    @Column(name = "countgrades")
    private long countGrades;

    @Column(name = "quantitysold")
    private long quantitySold;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "product", orphanRemoval = true)
    private Avatar avatar;
}

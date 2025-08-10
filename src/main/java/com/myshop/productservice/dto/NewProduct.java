package com.myshop.productservice.dto;

import com.myshop.productservice.repository.Avatar;
import com.myshop.productservice.repository.Photos;
import com.myshop.productservice.repository.Product;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewProduct {

    @Valid
    private Product product;

    private List<Photos> photos;

    private Avatar avatar;
}

package com.myshop.productservice.dto.searchService;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class DeleteDTO {
    private List<UUID> ids;
}

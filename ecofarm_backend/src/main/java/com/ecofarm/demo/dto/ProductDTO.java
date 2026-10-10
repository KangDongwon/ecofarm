package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {

    private Long productID;
    private String productName;
    private String description;
}

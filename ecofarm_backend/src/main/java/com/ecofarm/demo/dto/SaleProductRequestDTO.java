package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SaleProductRequestDTO {
    private String sellerEmail;
    private Long productID;
    private LocalDate availableUntilDate;
    private Integer quantity;
    private Double price;
    private String youTubeLink;
}

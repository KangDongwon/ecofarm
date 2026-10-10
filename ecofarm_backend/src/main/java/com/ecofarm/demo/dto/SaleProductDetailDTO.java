package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SaleProductDetailDTO {
    private Long saleProductID;
    private String sellerName;
    private String productName;
    private Double price;
    private Integer quantity;
    private LocalDate availableUntilDate;
    private String youTubeLink;
    private String consumerName;
}




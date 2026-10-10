package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SaleProductDTO {

    private Long saleProductID;
    private Long sellerID;
    private Long productID;
    private Double price;
    private Integer quantity;
    private LocalDate createdDate;
    private LocalDate availableUntilDate;
    private String approvalStatus;
    private LocalDate approvalDate;
    private String youTubeLink;
}

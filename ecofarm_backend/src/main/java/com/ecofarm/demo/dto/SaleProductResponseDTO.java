package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SaleProductResponseDTO {
    private Long saleProductID;
    private String apartmentName;
    private Long sellerID;
    private String sellerName;
    private String productName;
    private Double price;
    private Integer quantity;
    private LocalDate createdDate;
    private LocalDate availableUntilDate;
    private LocalDate approvalDate;
    private String approvalStatus;
    private String youTubeLink;

}

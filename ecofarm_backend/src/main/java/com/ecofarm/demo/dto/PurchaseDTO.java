package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PurchaseDTO {

    private String consumerEmail;
    private Long saleProductID;
    private Integer purchaseQuantity;
    private Double totalPrice;
}
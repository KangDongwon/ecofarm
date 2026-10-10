package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class OrderResponseDTO {
    private Long orderID;
    private String apartmentName;
    private String productName;
    private String sellerName;
    private Long sellerID;
    private String consumerName;
    private Long consumerID;
    private LocalDate orderDate;
    private Integer quantity;
    private Double totalPrice;
    private String status;

    // 생성자, getters, setters
}

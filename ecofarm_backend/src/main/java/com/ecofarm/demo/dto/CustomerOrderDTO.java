package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CustomerOrderDTO {

    private Long orderID;
    private Long consumerID;
    private Long saleProductID;
    private LocalDate orderDate;
    private Integer quantity;
    private Double totalPrice;
    private String status;
}

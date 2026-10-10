package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReviewResponseDTO {

    private Long reviewID;
    private String productName;
    private LocalDate orderDate;
    private String sellerName;
    private Long consumerID;
    private String consumerName;
    private Long saleProductID;
    private Integer rating;
    private String comment;
    private LocalDate reviewDate;
}
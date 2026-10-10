package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ReviewDTO {

    private Long reviewID;
    private Long orderID;
    private Long saleProductID;
    private Long consumerID;
    private Integer rating;
    private String comment;
    private LocalDate reviewDate;
}

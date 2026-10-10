package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewRequestDTO {
    private Long orderId;
    private int rating;
    private String comment;

    // Getters and Setters
}

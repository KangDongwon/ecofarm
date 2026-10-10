package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PendingSellerDTO {

    private Long sellerID;
    private String sellerName;
    private String phoneNumber;
    private String email;
    private String apartmentName;
    private LocalDate approvedDate;
}
package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SellerDTO {

    private Long sellerID;
    private String sellerName;
    private String phoneNumber;
    private Long apartmentID; // 외래 키로 사용
    private LocalDate approvedDate;
    private String email;
    private String password;
}

package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDTO {

    private Long adminID;
    private String adminName;
    private String email;
    private String password;
}
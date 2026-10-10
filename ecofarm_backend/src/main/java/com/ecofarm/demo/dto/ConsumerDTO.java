package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConsumerDTO {

    private Long consumerID;
    private String consumerName;
    private String phoneNumber;
    private String email;
    private String password;
    private String address;
    private LocalDateTime signupDate;
    private int age;
    private String diseases;
}

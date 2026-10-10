package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ApartmentDTO {

    private Long apartmentID;
    private String apartmentName;
    private String address;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
}

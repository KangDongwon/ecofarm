package com.ecofarm.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AitemsDTO {
    private String productGroup;
    private String ageGroup;
    private List<String> diseases;
}

package com.ecofarm.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApartmentID")
    private Long apartmentID;

    @Column(name = "ApartmentName")
    private String apartmentName;

    @Column(name = "Address")
    private String address;

    @Column(name = "ContractStartDate")
    private LocalDate contractStartDate;

    @Column(name = "ContractEndDate")
    private LocalDate contractEndDate;
}

package com.ecofarm.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SellerID") // 데이터베이스 컬럼명과 매핑
    private Long sellerID;

    @Column(name = "SellerName")
    private String sellerName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Email")
    private String email;

    @Column(name = "Password")
    private String password;

    @ManyToOne
    @JoinColumn(name = "ApartmentID") // 외래 키로 Apartment 테이블과 매핑
    private Apartment apartment;

    @Column(name = "ApprovedDate")
    private LocalDate approvedDate;
}

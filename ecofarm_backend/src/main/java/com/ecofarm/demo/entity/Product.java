package com.ecofarm.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID") // 데이터베이스 컬럼명과 매핑
    private Long productID;

    @Column(name = "ProductName")
    private String productName;

    @Column(name = "Description")
    private String description;
}

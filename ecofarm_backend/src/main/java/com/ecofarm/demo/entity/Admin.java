package com.ecofarm.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AdminID") // 데이터베이스 컬럼명과 매핑
    private Long adminID;

    @Column(name = "AdminName")
    private String adminName;

    @Column(name = "Email")
    private String email;

    @Column(name = "Password")
    private String password;
}

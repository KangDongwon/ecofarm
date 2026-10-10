package com.ecofarm.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConsumerID")
    private Long consumerID;

    @Column(name = "ConsumerName")
    private String consumerName;

    @Column(name = "PhoneNumber")
    private String phoneNumber;

    @Column(name = "Email")
    private String email;

    @Column(name = "Password")
    private String password;

    @Column(name = "Address")
    private String address;

    @Column(name = "SignupDate")
    private LocalDateTime signupDate;

    @Column(name = "Age")
    private int age;

    @Column(name = "Diseases")
    private String Diseases;

    @OneToMany(mappedBy = "consumer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerOrder> customerOrders;
}

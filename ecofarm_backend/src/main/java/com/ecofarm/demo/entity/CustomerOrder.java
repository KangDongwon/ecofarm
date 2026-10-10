package com.ecofarm.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "customerorder")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderID")
    private Long orderID;

    @ManyToOne
    @JoinColumn(name = "ConsumerID", nullable = false)
    private Consumer consumer;

    @ManyToOne
    @JoinColumn(name = "SaleProductID", nullable = false)
    private SaleProduct saleProduct;

    @Column(name = "OrderDate", nullable = false)
    private LocalDate orderDate;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "TotalPrice", nullable = false)
    private Double totalPrice;

    @Column(name = "Status", nullable = false)
    private String status;

    @OneToMany(mappedBy = "customerOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews; // 한 주문에 여러 리뷰가 있을 수 있음
}

package com.ecofarm.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class SaleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleProductID")
    private Long saleProductID;

    @ManyToOne
    @JoinColumn(name = "SellerID", nullable = false)
    private Seller seller;

    @ManyToOne
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @Column(name = "Price", nullable = false)
    private Double price;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDate createdDate;

    @Column(name = "AvailableUntilDate", nullable = false)
    private LocalDate availableUntilDate;

    @Column(name = "ApprovalStatus", nullable = false)
    private String approvalStatus;

    @Column(name = "ApprovalDate")
    private LocalDate approvalDate;

    @Column(name = "YouTubeLink")
    private String youTubeLink;

    @OneToMany(mappedBy = "saleProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
}

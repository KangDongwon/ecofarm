package com.ecofarm.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewID")
    private Long reviewID;

    @ManyToOne
    @JoinColumn(name = "OrderID", nullable = false)
    private CustomerOrder customerOrder;

    @ManyToOne
    @JoinColumn(name = "SaleProductID", nullable = false)
    private SaleProduct saleProduct;

    @ManyToOne
    @JoinColumn(name = "ConsumerID", nullable = false)
    private Consumer consumer;

    @Column(name = "Rating", nullable = false)
    private Integer rating;

    @Column(name = "Comment")
    private String comment;

    @Column(name = "ReviewDate", nullable = false)
    private LocalDate reviewDate;
}

package com.ecofarm.demo.controller;

import com.ecofarm.demo.dto.ReviewDTO;
import com.ecofarm.demo.dto.ReviewRequestDTO;
import com.ecofarm.demo.dto.ReviewResponseDTO;
import com.ecofarm.demo.entity.Review;
import com.ecofarm.demo.entity.CustomerOrder;
import com.ecofarm.demo.entity.SaleProduct;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.service.ReviewService;
import com.ecofarm.demo.service.CustomerOrderService;
import com.ecofarm.demo.service.SaleProductService;
import com.ecofarm.demo.service.ConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CustomerOrderService customerOrderService;

    @Autowired
    private SaleProductService saleProductService;

    @Autowired
    private ConsumerService consumerService;

    @GetMapping
    public List<ReviewDTO> getAllReviews() {
        return reviewService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.findById(id);
        return review.map(r -> ResponseEntity.ok(convertToDTO(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("/consumer/{email}")
    public List<ReviewResponseDTO> getReviewByConsumerEmail(@PathVariable String email) {
        List<ReviewResponseDTO> reviewResponseDTOList = reviewService.findByConsumerEmail(email);

        return reviewResponseDTOList;
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("/seller/{id}")
    public List<ReviewResponseDTO> getReviewBySellerID(@PathVariable Long id) {
        List<ReviewResponseDTO> reviewResponseDTOList = reviewService.findBySeller(id);

        return reviewResponseDTOList;
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @PostMapping
    public ResponseEntity<String> createReview(@RequestBody ReviewRequestDTO request) {
        log.info(request.getOrderId().toString());
        log.info(Integer.toString(request.getRating()));
        log.info(request.getComment());

        boolean success = reviewService.submitReview(request.getOrderId(), request.getRating(), request.getComment());
        if(success) {
            return ResponseEntity.ok("리뷰가 성공적으로 작성되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("주문을 찾을 수 없습니다.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
                .reviewID(review.getReviewID())
                .orderID(review.getCustomerOrder() != null ? review.getCustomerOrder().getOrderID() : null)
                .saleProductID(review.getSaleProduct() != null ? review.getSaleProduct().getSaleProductID() : null)
                .consumerID(review.getConsumer() != null ? review.getConsumer().getConsumerID() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .reviewDate(review.getReviewDate())
                .build();
    }

    private Review convertToEntity(ReviewDTO dto) {
        Review review = new Review();
        review.setReviewID(dto.getReviewID());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setReviewDate(dto.getReviewDate());

        if (dto.getOrderID() != null) {
            Optional<CustomerOrder> customerOrder = customerOrderService.findById(dto.getOrderID());
            if (customerOrder.isPresent()) {
                review.setCustomerOrder(customerOrder.get());
            } else {
                throw new RuntimeException("CustomerOrder not found");
            }
        }

        if (dto.getSaleProductID() != null) {
            Optional<SaleProduct> saleProduct = saleProductService.findById(dto.getSaleProductID());
            if (saleProduct.isPresent()) {
                review.setSaleProduct(saleProduct.get());
            } else {
                throw new RuntimeException("SaleProduct not found");
            }
        }

        if (dto.getConsumerID() != null) {
            Optional<Consumer> consumer = consumerService.findById(dto.getConsumerID());
            if (consumer.isPresent()) {
                review.setConsumer(consumer.get());
            } else {
                throw new RuntimeException("Consumer not found");
            }
        }

        return review;
    }
}

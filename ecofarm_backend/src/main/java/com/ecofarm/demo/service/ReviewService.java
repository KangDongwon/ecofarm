package com.ecofarm.demo.service;

import com.ecofarm.demo.dto.ReviewResponseDTO;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.entity.CustomerOrder;
import com.ecofarm.demo.entity.Review;
import com.ecofarm.demo.entity.SaleProduct;
import com.ecofarm.demo.repository.ReviewRepository;
import com.ecofarm.demo.repository.CustomerOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }

    public List<ReviewResponseDTO> findByConsumerEmail(String email) { return reviewRepository.findByConsumerEmail(email); }

    public List<ReviewResponseDTO> findBySeller(Long id) { return reviewRepository.findBySeller(id); }

    @Transactional
    public boolean submitReview(Long orderId, int rating, String comment) {
        // CustomerOrder에서 정보 가져오기
        Optional<CustomerOrder> orderOptional = customerOrderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            return false;
        }

        CustomerOrder order = orderOptional.get();
        SaleProduct saleProduct = order.getSaleProduct();
        Consumer consumer = order.getConsumer();

        // Review 객체 생성
        Review review = new Review();
        review.setCustomerOrder(order);
        review.setSaleProduct(saleProduct);
        review.setConsumer(consumer);
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewDate(LocalDate.now());

        // 리뷰 저장
        reviewRepository.save(review);

        // 주문 상태 업데이트
        order.setStatus("리뷰 작성 완료");
        customerOrderRepository.save(order);

        return true;
    }

    @Transactional
    public void createTestReviews() {
        Random random = new Random();

        // 댓글 텍스트 배열
        String[] comments = {
                "맛있고 신선합니다.",
                "몸이 아주 건강해지는 기분이에요.",
                "신선한 어린잎 채소 맛도 건강도 최고입니다!",
                "신선도가 떨어져서 아쉽습니다..",
                "파우더 스프와 같이 먹으니 더 좋습니다!!"
        };

        for (int i = 1; i <= 10000; i++) {
            // Randomly select an OrderID from 1 to 1000
            long orderId = random.nextInt(10000) + 1;
            Optional<CustomerOrder> orderOptional = customerOrderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                // Find the Order to retrieve ConsumerID and SaleProductID
                CustomerOrder order = orderOptional.get();

                if (!order.getStatus().equals("물품 전달 완료")) {
                    continue;
                }

                Consumer consumer = order.getConsumer();
                SaleProduct saleProduct = order.getSaleProduct();

                // Randomly select a comment
                String comment = comments[random.nextInt(comments.length)];

                // Randomly select a rating between 1 and 5
                int rating;
                double randomValue = Math.random(); // 0.0 (포함) ~ 1.0 (미포함) 사이의 랜덤 값 생성

                if (randomValue < 0.5) {
                    rating = 5; // 50% 확률로 5
                } else if (randomValue < 0.8) {
                    rating = 4; // 30% 확률로 4
                } else if (randomValue < 0.95) {
                    rating = 3; // 15% 확률로 3
                } else if (randomValue < 0.99) {
                    rating = 2; // 4% 확률로 2
                } else {
                    rating = 1; // 1% 확률로 1
                }

                // ReviewDate is between 1 to 6 days after OrderDate
                LocalDate reviewDate = order.getOrderDate().plusDays(random.nextInt(3) + 1);

                // Create a Review entity and save it
                Review review = new Review();
                review.setComment(comment);
                review.setRating(rating);
                review.setReviewDate(reviewDate);
                review.setConsumer(consumer);
                review.setCustomerOrder(order);
                review.setSaleProduct(saleProduct);

                reviewRepository.save(review);
            }
        }
    }
}

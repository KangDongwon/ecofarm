package com.ecofarm.demo.service;

import com.ecofarm.demo.dto.SaleProductDetailDTO;
import com.ecofarm.demo.dto.SaleProductResponseDTO;
import com.ecofarm.demo.entity.Product;
import com.ecofarm.demo.entity.SaleProduct;
import com.ecofarm.demo.entity.Seller;
import com.ecofarm.demo.repository.ProductRepository;
import com.ecofarm.demo.repository.SaleProductRepository;
import com.ecofarm.demo.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class SaleProductService {

    @Autowired
    private SaleProductRepository saleProductRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<SaleProduct> findAll() {
        return saleProductRepository.findAll();
    }

    public Optional<SaleProduct> findById(Long id) {
        return saleProductRepository.findById(id);
    }

    public SaleProduct save(SaleProduct saleProduct) {
        return saleProductRepository.save(saleProduct);
    }

    public void deleteById(Long id) {
        saleProductRepository.deleteById(id);
    }

    public List<SaleProductResponseDTO> getSaleProductsBySellerEmail(String email) {
        return saleProductRepository.findSaleProductsBySellerEmail(email);
    }

    public List<SaleProduct> getApprovedProducts() {
        return saleProductRepository.findByApprovalStatus("승인");
    }

    public List<SaleProduct> getApprovedAndRejectedProducts() {
        return saleProductRepository.findByApprovalStatusIn(List.of("승인", "거절"));
    }

    public Optional<SaleProductDetailDTO> getSaleProductDetail(Long saleProductID) {
        return saleProductRepository.findSaleProductDetailById(saleProductID);
    }

    public void updateQuantity(Long saleProductID, Integer quantity) {
        SaleProduct saleProduct = saleProductRepository.findById(saleProductID)
                .orElseThrow(() -> new RuntimeException("판매 물품을 찾을 수 없습니다."));

        // 수량 업데이트
        if (saleProduct.getQuantity() < quantity) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        saleProduct.setQuantity(saleProduct.getQuantity() - quantity);
        saleProductRepository.save(saleProduct);
    }

    @Transactional
    public void createTestSaleProducts() {
        Random random = new Random();
        String[] youtubeLinks = {
                "https://www.youtube.com/watch?v=wbEzAijWYgQ&ab_channel=%EC%8B%A0%EB%B0%94%EB%9E%8C%EA%B1%B4%EA%B0%95%EC%A0%95%EB%B3%B4", //브로콜리
                "https://www.youtube.com/watch?v=Kmmh3asyeLA&ab_channel=%EC%8B%A0%EB%B0%94%EB%9E%8C%EA%B1%B4%EA%B0%95%EC%A0%95%EB%B3%B4", //팥순
                "https://www.youtube.com/watch?v=cSj_vkk2xMo&ab_channel=%EB%8C%80%EC%82%B0%EB%86%8D%EC%B4%8C%EC%9E%AC%EB%8B%A8", //케일
                "https://www.youtube.com/watch?v=nd_OWppgs1w&ab_channel=%EB%A7%88%EC%9D%B4%EC%8A%A4%EC%9C%97%ED%8C%9CMySweetFarm", //적양배추
                "https://www.youtube.com/watch?v=JEddy1Afxl4&ab_channel=%EB%86%8D%EC%B4%8C%EC%A7%84%ED%9D%A5%EC%B2%AD", //무순
                "https://www.youtube.com/watch?v=nK8jDh1qzdM", //해바라기
                "https://www.youtube.com/watch?v=8L6q2ws1NvQ", //완두콩
                "https://www.youtube.com/watch?v=A_facLplF1w", //시금치
                "https://www.youtube.com/watch?v=Irw6Vnyz1UM", //와사비
                "https://www.youtube.com/watch?v=PIByHQiNqjo", //루꼴라
                "https://www.youtube.com/watch?v=Kq2F5Igp0Zk", //비트
                "https://www.youtube.com/watch?v=oggJeoRUEqM", //아루굴라
                "https://www.youtube.com/watch?v=jO20A5LbXEI" //유채
        };

        for (int i = 1; i <= 1000; i++) {
            // SellerID는 30~150 사이의 랜덤 값
            long sellerId = random.nextInt(145) + 1;
            Optional<Seller> optionalSeller = sellerRepository.findById(sellerId);
            // ProductID는 1~5 사이의 랜덤 값
            long productId = random.nextInt(13) + 1;
            Optional<Product> optionalProduct = productRepository.findById(productId);

            if (optionalProduct.isPresent() && optionalSeller.isPresent()) {
                LocalDate approvalDate = null;
                String approvalStatus = null;
                int quantity = 0;

                if (i<901) {
                    //2023~ 현재일 기준 10일전
                    approvalDate = LocalDate.of(2023, 1, 1).plusDays(new Random().nextInt((int) (LocalDate.now().minusDays(10).toEpochDay() - LocalDate.of(2023, 1, 1).toEpochDay() + 1)));
                    approvalStatus = "승인";
                    if (Math.random() > 0.9) {
                        quantity = random.nextInt(10);
                    }
                } else if(i<991) {
                    approvalDate = LocalDate.now().minusDays(3).plusDays(new Random().nextInt(4));
                    approvalStatus = "승인";
                    quantity = random.nextInt(41) + 10;
                } else {
                    approvalDate = null;
                    approvalStatus = "대기중";
                    quantity = random.nextInt(41) + 10;
                }

                LocalDate createdDate;
                if (approvalDate != null)
                    createdDate = approvalDate.minusDays(random.nextInt(15)); // ApprovalDate의 최대 14일 전
                else
                    createdDate = LocalDate.now().minusDays(3).plusDays(new Random().nextInt(4));

                // AvailableUntilDate는 ApprovalDate에서 7일 후
                LocalDate availableUntilDate = null;
                if (approvalDate != null)
                    availableUntilDate = approvalDate.plusDays(7);
                else
                    availableUntilDate = createdDate.plusDays(7);

                // Price는 5000~10000 사이의 랜덤 값
                double price = 5000 + (random.nextInt(6) * 1000); // 5000 + (0~5) * 1000

                // YouTubeLink 랜덤 선택
                String youtubeLink = youtubeLinks[Long.valueOf(productId).intValue()-1];

                // SaleProduct 객체 생성 및 저장
                SaleProduct saleProduct = new SaleProduct();
                saleProduct.setApprovalDate(approvalDate);
                saleProduct.setApprovalStatus(approvalStatus);
                saleProduct.setAvailableUntilDate(availableUntilDate);
                saleProduct.setCreatedDate(createdDate);
                saleProduct.setPrice(price);
                saleProduct.setQuantity(quantity);
                saleProduct.setYouTubeLink(youtubeLink);
                saleProduct.setProduct(optionalProduct.get());
                saleProduct.setSeller(optionalSeller.get());

                saleProductRepository.save(saleProduct); // 데이터베이스에 저장
            }
        }
    }
}

package com.ecofarm.demo.controller;

import com.ecofarm.demo.dto.SaleProductDTO;
import com.ecofarm.demo.dto.SaleProductDetailDTO;
import com.ecofarm.demo.dto.SaleProductRequestDTO;
import com.ecofarm.demo.dto.SaleProductResponseDTO;
import com.ecofarm.demo.entity.SaleProduct;
import com.ecofarm.demo.entity.Seller;
import com.ecofarm.demo.entity.Product;
import com.ecofarm.demo.service.SaleProductService;
import com.ecofarm.demo.service.SellerService;
import com.ecofarm.demo.service.ProductService;
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
@RequestMapping("/api/saleproducts")
public class SaleProductController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SaleProductService saleProductService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<SaleProductResponseDTO> getAllSaleProducts() {
        return saleProductService.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("/approved")
    public List<SaleProductResponseDTO> getApprovedSaleProducts() {
        return saleProductService.getApprovedProducts().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/by-seller")
    public List<SaleProductResponseDTO> getSaleProductsBySellerEmail(@RequestParam String email) {

        return saleProductService.getSaleProductsBySellerEmail(email);
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("/{saleProductID}")
    public ResponseEntity<SaleProductDetailDTO> getSaleProductDetail(@PathVariable Long saleProductID) {
        Optional<SaleProductDetailDTO > saleProductDetailDTO = saleProductService.getSaleProductDetail(saleProductID);
        if (saleProductDetailDTO.isPresent()) {
            return new ResponseEntity<>(saleProductDetailDTO.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public SaleProductDTO createSaleProduct(@RequestBody SaleProductDTO saleProductDTO) {
        SaleProduct saleProduct = convertToEntity(saleProductDTO);
        SaleProduct savedSaleProduct = saleProductService.save(saleProduct);
        return convertToDTO(savedSaleProduct);
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/add")
    public ResponseEntity<?> addSaleProduct(@RequestBody SaleProductRequestDTO request) {
        try {
            // 판매자 이메일로 판매자 ID 찾기
            Optional<Seller> optionalSeller = sellerService.findByEmail(request.getSellerEmail());
            if (optionalSeller.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("판매자 정보 오류!");
            }
            Seller seller = optionalSeller.get();
            SaleProductDTO saleProductDTO = SaleProductDTO.builder()
                    .sellerID(seller.getSellerID())
                    .productID(request.getProductID())
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .youTubeLink(request.getYouTubeLink())
                    .availableUntilDate(request.getAvailableUntilDate())
                    .approvalStatus("대기중")
                    .approvalDate(null)
                    .createdDate(LocalDate.now())
                    .build();
            saleProductService.save(convertToEntity(saleProductDTO));
            return ResponseEntity.ok("판매 물품이 성공적으로 추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("판매 물품 추가 중 오류 발생");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaleProduct(@PathVariable Long id) {
        saleProductService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/approve/{productID}")
    public ResponseEntity<String> approveSaleProduct(@PathVariable Long productID) {
        Optional<SaleProduct> optionalSaleProduct = saleProductService.findById(productID);
        if (optionalSaleProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SaleProduct saleProduct = optionalSaleProduct.get();
        saleProduct.setApprovalStatus("승인");
        saleProduct.setApprovalDate(LocalDate.now());

        try {
            saleProductService.save(saleProduct);
            return new ResponseEntity<>("SaleProduct approve successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to approve saleproduct", HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/reject/{productID}")
    public ResponseEntity<String> rejectSaleProduct(@PathVariable Long productID) {
        Optional<SaleProduct> optionalSaleProduct = saleProductService.findById(productID);
        if (optionalSaleProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SaleProduct saleProduct = optionalSaleProduct.get();
        saleProduct.setApprovalStatus("거절");

        try {
            saleProductService.save(saleProduct);
            return new ResponseEntity<>("SaleProduct reject successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to reject saleproduct", HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/update/{productID}")
    public ResponseEntity<String> updateSaleProduct(@PathVariable Long productID, @RequestBody SaleProductRequestDTO saleProductRequestDTO) {

        Optional<SaleProduct> optionalSaleProduct = saleProductService.findById(productID);
        if (optionalSaleProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SaleProduct saleProduct = optionalSaleProduct.get();
        // DTO에서 받은 데이터로 상품 업데이트
        if (saleProductRequestDTO.getProductID() != null) {
            Optional<Product> optionalProduct = productService.findById(saleProductRequestDTO.getProductID());
            if (optionalProduct.isPresent()) {
                saleProduct.setProduct(optionalProduct.get());
            }
        }
        if (saleProductRequestDTO.getAvailableUntilDate() != null) {
            saleProduct.setAvailableUntilDate(saleProductRequestDTO.getAvailableUntilDate());
        }
        if (saleProductRequestDTO.getQuantity() != null) {
            saleProduct.setQuantity(saleProductRequestDTO.getQuantity());
        }
        if (saleProductRequestDTO.getPrice() != null) {
            saleProduct.setPrice(saleProductRequestDTO.getPrice());
        }
        if (saleProductRequestDTO.getYouTubeLink() != null) {
            saleProduct.setYouTubeLink(saleProductRequestDTO.getYouTubeLink());
        }
        if (saleProduct.getApprovalStatus().equals("거절")) {
            saleProduct.setApprovalStatus("대기중");
        }

        // 요청 본문에서 받은 데이터로 상품 업데이트 처리
        try {
            saleProductService.save(saleProduct);
            return new ResponseEntity<>("SaleProduct updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update saleproduct", HttpStatus.BAD_REQUEST);
        }
    }

    private SaleProductDTO convertToDTO(SaleProduct saleProduct) {
        return SaleProductDTO.builder()
                .saleProductID(saleProduct.getSaleProductID())
                .sellerID(saleProduct.getSeller() != null ? saleProduct.getSeller().getSellerID() : null)
                .productID(saleProduct.getProduct() != null ? saleProduct.getProduct().getProductID() : null)
                .price(saleProduct.getPrice())
                .quantity(saleProduct.getQuantity())
                .createdDate(saleProduct.getCreatedDate())
                .availableUntilDate(saleProduct.getAvailableUntilDate())
                .approvalStatus(saleProduct.getApprovalStatus())
                .approvalDate(saleProduct.getApprovalDate())
                .youTubeLink(saleProduct.getYouTubeLink())
                .build();
    }

    private SaleProductResponseDTO convertToResponseDTO(SaleProduct saleProduct) {
        return SaleProductResponseDTO.builder()
                .saleProductID(saleProduct.getSaleProductID())
                .apartmentName(saleProduct.getSeller() != null ? (saleProduct.getSeller().getApartment() != null ? saleProduct.getSeller().getApartment().getApartmentName() : null) : null)
                .sellerID(saleProduct.getSeller() != null ? saleProduct.getSeller().getSellerID() : null)
                .sellerName(saleProduct.getSeller() != null ? saleProduct.getSeller().getSellerName() : null)
                .productName(saleProduct.getProduct() != null ? saleProduct.getProduct().getProductName() : null)
                .price(saleProduct.getPrice())
                .quantity(saleProduct.getQuantity())
                .createdDate(saleProduct.getCreatedDate())
                .availableUntilDate(saleProduct.getAvailableUntilDate())
                .approvalStatus(saleProduct.getApprovalStatus())
                .approvalDate(saleProduct.getApprovalDate())
                .youTubeLink(saleProduct.getYouTubeLink())
                .build();
    }

    private SaleProduct convertToEntity(SaleProductDTO dto) {
        SaleProduct saleProduct = new SaleProduct();
        saleProduct.setSaleProductID(dto.getSaleProductID());
        saleProduct.setPrice(dto.getPrice());
        saleProduct.setQuantity(dto.getQuantity());
        saleProduct.setCreatedDate(dto.getCreatedDate());
        saleProduct.setAvailableUntilDate(dto.getAvailableUntilDate());
        saleProduct.setApprovalStatus(dto.getApprovalStatus());
        saleProduct.setApprovalDate(dto.getApprovalDate());
        saleProduct.setYouTubeLink(dto.getYouTubeLink());

        if (dto.getSellerID() != null) {
            Optional<Seller> seller = sellerService.findById(dto.getSellerID());
            if (seller.isPresent()) {
                saleProduct.setSeller(seller.get());
            } else {
                throw new RuntimeException("Seller not found");
            }
        }

        if (dto.getProductID() != null) {
            Optional<Product> product = productService.findById(dto.getProductID());
            if (product.isPresent()) {
                saleProduct.setProduct(product.get());
            } else {
                throw new RuntimeException("Product not found");
            }
        }

        return saleProduct;
    }
}

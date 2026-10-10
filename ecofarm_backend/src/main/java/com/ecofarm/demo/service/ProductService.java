package com.ecofarm.demo.service;

import com.opencsv.CSVWriter;
import com.ecofarm.demo.dto.ProductDTO;
import com.ecofarm.demo.entity.Product;
import com.ecofarm.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public boolean existsByProductName(String productName) { return productRepository.existsByProductName(productName); }

    @Transactional
    public Product updateProduct(ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productDTO.getProductID())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productDTO.getProductID()));

        // Update the existing product with the new values
        existingProduct.setProductName(productDTO.getProductName());
        existingProduct.setDescription(productDTO.getDescription());

        // Save the updated product
        return productRepository.save(existingProduct);
    }

    public void saveProductsToCsv(String filePath) {
        List<Product> products = productRepository.findAll();

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // CSV 파일 헤더 작성
            String[] header = {"ITEM_ID", "NAME"};
            writer.writeNext(header);

            for (Product product : products) {
                String[] data = {
                        String.valueOf(product.getProductID()), // OrderID
                        product.getProductName()
                };
                writer.writeNext(data);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Transactional
    public void createTestProducts() {
        for (int i = 0; i < PRODUCT_NAMES.length; i++) {
            Product product = new Product();
            product.setProductName(PRODUCT_NAMES[i]);
            product.setDescription(DESCRIPTIONS[i]);
            productRepository.save(product);
        }
    }

    private static final String[] PRODUCT_NAMES = {
            "브로콜리",
            "팥순",
            "케일",
            "적양배추",
            "무순",
            "해바라기",
            "완두콩",
            "시금치",
            "와사비",
            "루꼴라",
            "비트",
            "아루굴라",
            "유채"
    };

    private static final String[] DESCRIPTIONS = {
            "굴루코라파닌 성분, 항암 효과",
            "아주카사포닌 성분, 비만 예방",
            "루테인-지아잔틴 성분, 항산화-항염증 효과",
            "글루코시놀레이트 성분, 심혈관질환(LDL) 예방",
            "설포라판 성분, 전립선암-유방암 예방",
            "비타민E, 셀레늄 성분, 심혈관 건강 증진", //해바라기
            "단백질 성분, 체중-혈당 관리", //완두콩
            "철분-엽산 성분, 고혈압 예방", //시금치
            "글루코시놀레이트 성분, 심혈관질환 건강 개선", //와사비
            "루테인-베타카로틴 성분, 항암 효과", //루꼴라
            "질산염 성분, 심혈관 건강 개선", //비트
            "글루코시놀레이트 성분, 항암 효과, 심혈관 건강 개선", //아루굴라
            "비타민 C,K 성분, 심장병 예방, 항염 효과" //유채
    };
}

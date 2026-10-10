package com.ecofarm.demo.service;

import com.ecofarm.demo.dto.OrderResponseDTO;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.entity.CustomerOrder;
import com.ecofarm.demo.entity.SaleProduct;
import com.ecofarm.demo.repository.ConsumerRepository;
import com.ecofarm.demo.repository.CustomerOrderRepository;
import com.ecofarm.demo.repository.SaleProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class CustomerOrderService {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private SaleProductRepository saleProductRepository;

    public List<CustomerOrder> findAll() {
        return customerOrderRepository.findAll();
    }

    public Optional<CustomerOrder> findById(Long id) {
        return customerOrderRepository.findById(id);
    }

    public CustomerOrder save(CustomerOrder customerOrder) {
        return customerOrderRepository.save(customerOrder);
    }

    public void deleteById(Long id) {
        customerOrderRepository.deleteById(id);
    }

    public List<OrderResponseDTO> getOrdersByConsumerEmail(String email) {
        return customerOrderRepository.findOrdersByConsumerEmail(email);
    }

    public List<OrderResponseDTO> getOrdersBySellerEmail(String email) {
        return customerOrderRepository.findOrdersBySellerEmail(email);
    }

    public List<OrderResponseDTO> findAllOrders() {
        return customerOrderRepository.findAllOrders();
    }

    @Transactional
    public void createTestCustomerOrders() {
        Random random = new Random();

        for (int i = 1; i <= 10000; i++) {
            // ConsumerID는 1~200 사이의 랜덤 값
            long consumerId = random.nextInt(200) + 1; // 1부터 200까지
            Optional<Consumer> optionalConsumer = consumerRepository.findById(consumerId);

            long saleProductId;
            if (random.nextInt(200) == 0) { // 0.5% 확률로
                saleProductId = random.nextInt(90) + 901; // 901부터 990까지
            } else {
                saleProductId = random.nextInt(900) + 1; // 1부터 900까지
            }

            Optional<SaleProduct> optionalSaleProduct = saleProductRepository.findById(saleProductId);

            if (optionalSaleProduct.isPresent() && optionalConsumer.isPresent()) {
                // SaleProduct 엔티티에서 SaleProductID에 해당하는 상품을 조회
                SaleProduct saleProduct = optionalSaleProduct.get();

                LocalDate orderDate = saleProduct.getAvailableUntilDate().minusDays(random.nextInt(7)); // 0일부터 6일 이내
                // Quantity는 1에서 SaleProduct의 Quantity 이내
                int quantity = random.nextInt(saleProduct.getQuantity()+40) + 10;

                // TotalPrice는 SaleProduct의 Price * Quantity
                double totalPrice = saleProduct.getPrice() * quantity;

                // Status는 '물품 인수 완료' 또는 '구매 취소' 랜덤 선택
                String status = "";
                if (saleProductId < 901) {
                    status = random.nextDouble() < 0.9 ? "물품 전달 완료" : "구매 취소";
                } else {
                    orderDate = LocalDate.now();
                    status = random.nextDouble() < 0.3 ? "물품 전달 대기" : (random.nextDouble() < 0.65 ? "입금 대기" : "입금 완료");
                }

                // CustomerOrder 객체 생성 및 저장
                CustomerOrder customerOrder = new CustomerOrder();
                customerOrder.setOrderDate(orderDate);
                customerOrder.setQuantity(quantity);
                customerOrder.setStatus(status);
                customerOrder.setTotalPrice(totalPrice);
                customerOrder.setConsumer(optionalConsumer.get());
                customerOrder.setSaleProduct(saleProduct);

                customerOrderRepository.save(customerOrder); // 데이터베이스에 저장
            }
        }
    }

    public void saveOrdersToCsv(String filePath) {
        List<CustomerOrder> orders = customerOrderRepository.findDeliveredOrders();

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // CSV 파일 헤더 작성
            String[] header = {"INTERACTION_ID", "USER_ID", "ITEM_ID", "TIMESTAMP"};
            writer.writeNext(header);

            for (CustomerOrder order : orders) {
                String[] data = {
                        String.valueOf(order.getOrderID()), // OrderID
                        String.valueOf(order.getConsumer().getConsumerID()), // ConsumerID
                        String.valueOf(order.getSaleProduct().getProduct().getProductID()),
                        String.valueOf(convertToUnixTimestamp(order.getOrderDate()))
                };
                writer.writeNext(data);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // 주문 날짜를 timestamp 형식으로 변환하는 메서드
    // 주문 날짜를 Unix timestamp로 변환하는 메서드
    private long convertToUnixTimestamp(LocalDate orderDate) {
        // LocalDate를 Instant로 변환
        return orderDate.atStartOfDay(ZoneId.of("UTC")).toInstant().getEpochSecond();
    }
}

package com.ecofarm.demo.controller;

import com.ecofarm.demo.dto.CustomerOrderDTO;
import com.ecofarm.demo.dto.OrderResponseDTO;
import com.ecofarm.demo.dto.PurchaseDTO;
import com.ecofarm.demo.entity.CustomerOrder;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.entity.SaleProduct;
import com.ecofarm.demo.service.CustomerOrderService;
import com.ecofarm.demo.service.ConsumerService;
import com.ecofarm.demo.service.SaleProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customerorders")
public class CustomerOrderController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerOrderService customerOrderService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private SaleProductService saleProductService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllCustomerOrders() {
        try {
            List<OrderResponseDTO> orders = customerOrderService.findAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류 발생");
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrderDTO> getCustomerOrderById(@PathVariable Long id) {
        Optional<CustomerOrder> customerOrder = customerOrderService.findById(id);
        return customerOrder.map(o -> ResponseEntity.ok(convertToDTO(o)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("/consumerorders")
    public ResponseEntity<?> getConsumerOrdersByEmail(@RequestParam String email) {
        try {
            List<OrderResponseDTO> orders = customerOrderService.getOrdersByConsumerEmail(email);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류 발생");
        }
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/sellerorders")
    public ResponseEntity<?> getSellerOrdersByEmail(@RequestParam String email) {
        try {
            List<OrderResponseDTO> orders = customerOrderService.getOrdersBySellerEmail(email);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류 발생");
        }
    }

    @PostMapping
    public CustomerOrderDTO createCustomerOrder(@RequestBody CustomerOrderDTO customerOrderDTO) {
        CustomerOrder customerOrder = convertToEntity(customerOrderDTO);
        CustomerOrder savedCustomerOrder = customerOrderService.save(customerOrder);
        return convertToDTO(savedCustomerOrder);
    }

    @Transactional
    @PreAuthorize("hasRole('CONSUMER')")
    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseSaleProduct(@RequestBody PurchaseDTO purchaseDTO) {

        try {
            Optional<Consumer> optionalConsumer = consumerService.findByEmail(purchaseDTO.getConsumerEmail());
            if (optionalConsumer.isEmpty()) {
                ResponseEntity.notFound().build();
            }

            saleProductService.updateQuantity(purchaseDTO.getSaleProductID(), purchaseDTO.getPurchaseQuantity());

            CustomerOrderDTO customerOrderDTO = CustomerOrderDTO.builder()
                    .saleProductID(purchaseDTO.getSaleProductID())
                    .consumerID(optionalConsumer.get().getConsumerID())
                    .orderDate(LocalDate.now())
                    .quantity(purchaseDTO.getPurchaseQuantity())
                    .totalPrice(purchaseDTO.getTotalPrice())
                    .status("입금 대기")
                    .build();

            customerOrderService.save(convertToEntity(customerOrderDTO));
            return ResponseEntity.ok("구매가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("구매 처리 중 오류 발생");
        }
    }

    @PreAuthorize("hasRole('CONSUMER') or hasRole('SELLER') or hasRole('ADMIN')")
    @PutMapping("/{orderID}")
    public ResponseEntity<String> updateOrderStatus(@PathVariable Long orderID, @RequestBody Map<String, String> requestBody) {
        try {
            String status = requestBody.get("status"); // 요청 본문에서 상태를 가져옴
            if(status.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 요청입니다.");
            }
            Optional<CustomerOrder> orderOptional = customerOrderService.findById(orderID);
            if(orderOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("주문을 찾을 수 없습니다.");
            }

            CustomerOrder order = orderOptional.get();
            order.setStatus(status);
            customerOrderService.save(order);
            return ResponseEntity.ok("입금이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("입금 처리 중 오류 발생");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerOrder(@PathVariable Long id) {
        customerOrderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CustomerOrderDTO convertToDTO(CustomerOrder customerOrder) {
        return CustomerOrderDTO.builder()
                .orderID(customerOrder.getOrderID())
                .consumerID(customerOrder.getConsumer() != null ? customerOrder.getConsumer().getConsumerID() : null)
                .saleProductID(customerOrder.getSaleProduct() != null ? customerOrder.getSaleProduct().getSaleProductID() : null)
                .orderDate(customerOrder.getOrderDate())
                .quantity(customerOrder.getQuantity())
                .totalPrice(customerOrder.getTotalPrice())
                .status(customerOrder.getStatus())
                .build();
    }

    private CustomerOrder convertToEntity(CustomerOrderDTO dto) {
        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setOrderID(dto.getOrderID());
        customerOrder.setOrderDate(dto.getOrderDate());
        customerOrder.setQuantity(dto.getQuantity());
        customerOrder.setTotalPrice(dto.getTotalPrice());
        customerOrder.setStatus(dto.getStatus());

        if (dto.getConsumerID() != null) {
            Optional<Consumer> consumer = consumerService.findById(dto.getConsumerID());
            if (consumer.isPresent()) {
                customerOrder.setConsumer(consumer.get());
            } else {
                throw new RuntimeException("Consumer not found");
            }
        }

        if (dto.getSaleProductID() != null) {
            Optional<SaleProduct> saleProduct = saleProductService.findById(dto.getSaleProductID());
            if (saleProduct.isPresent()) {
                customerOrder.setSaleProduct(saleProduct.get());
            } else {
                throw new RuntimeException("SaleProduct not found");
            }
        }

        return customerOrder;
    }
}

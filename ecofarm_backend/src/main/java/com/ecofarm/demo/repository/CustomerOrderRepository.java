package com.ecofarm.demo.repository;

import com.ecofarm.demo.dto.OrderResponseDTO;
import com.ecofarm.demo.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    @Query("SELECT new com.ecofarm.demo.dto.OrderResponseDTO(co.orderID, a.apartmentName, p.productName, s.sellerName, s.sellerID, c.consumerName, c.consumerID, co.orderDate, co.quantity, co.totalPrice, co.status) " +
            "FROM CustomerOrder co " +
            "JOIN co.saleProduct sp " +
            "JOIN sp.product p " +
            "JOIN sp.seller s " +
            "JOIN s.apartment a " +
            "JOIN co.consumer c " +
            "WHERE c.email = :email") // 이메일로 필터링
    List<OrderResponseDTO> findOrdersByConsumerEmail(@Param("email") String email);

    @Query("SELECT new com.ecofarm.demo.dto.OrderResponseDTO(co.orderID, a.apartmentName, p.productName, s.sellerName, s.sellerID, c.consumerName, c.consumerID, co.orderDate, co.quantity, co.totalPrice, co.status) " +
            "FROM CustomerOrder co " +
            "JOIN co.saleProduct sp " +
            "JOIN sp.product p " +
            "JOIN sp.seller s " +
            "JOIN s.apartment a " +
            "JOIN co.consumer c " +
            "WHERE s.email = :email")
    List<OrderResponseDTO> findOrdersBySellerEmail(@Param("email") String email);


    @Query("SELECT new com.ecofarm.demo.dto.OrderResponseDTO(co.orderID, a.apartmentName, p.productName, s.sellerName, s.sellerID, c.consumerName, c.consumerID, co.orderDate, co.quantity, co.totalPrice, co.status) " +
            "FROM CustomerOrder co " +
            "JOIN co.saleProduct sp " +
            "JOIN sp.product p " +
            "JOIN sp.seller s " +
            "JOIN s.apartment a " +
            "JOIN co.consumer c") // CustomerOrder와 Review 조인
    List<OrderResponseDTO> findAllOrders();

    @Query("SELECT o FROM CustomerOrder o WHERE o.status = '물품 전달 완료'")
    List<CustomerOrder> findDeliveredOrders();

}

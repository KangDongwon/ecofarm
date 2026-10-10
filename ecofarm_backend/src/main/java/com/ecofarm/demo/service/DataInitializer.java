package com.ecofarm.demo.service; // 또는 다른 관련 패키지

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ConsumerService consumerService;
    private final AdminService adminService;
    private final ApartmentService apartmentService;
    private final ProductService productService;
    private final DatabaseResetService databaseResetService;
    private final SellerService sellerService;
    private final SaleProductService saleProductService;
    private final CustomerOrderService customerOrderService;
    private final ReviewService reviewService;

    public DataInitializer(ConsumerService consumerService, AdminService adminService,
                           ApartmentService apartmentService, ProductService productService,
                           DatabaseResetService databaseResetService, SellerService sellerService,
                           SaleProductService saleProductService, CustomerOrderService customerOrderService,
                           ReviewService reviewService) {
        this.consumerService = consumerService;
        this.adminService = adminService;
        this.apartmentService = apartmentService;
        this.productService = productService;
        this.databaseResetService = databaseResetService;
        this.sellerService = sellerService;
        this.saleProductService = saleProductService;
        this.customerOrderService = customerOrderService;
        this.reviewService = reviewService;
    }

    @Override
    public void run(String... args) {
        boolean reset = true;
        if (!reset) {
            //csv 파일 생성
//            String currentDirectory = Paths.get("").toAbsolutePath().toString();
//            System.out.println("directory: " + currentDirectory);
//            consumerService.saveConsumersToCsv(currentDirectory+"\\user_data.csv");
//            productService.saveProductsToCsv(currentDirectory+"\\item_data.csv");
//            customerOrderService.saveOrdersToCsv(currentDirectory+"\\interaction_data.csv");
            return ;
        }

        databaseResetService.resetAutoIncrement("review");
        databaseResetService.resetAutoIncrement("customerorder");
        databaseResetService.resetAutoIncrement("saleproduct");
        databaseResetService.resetAutoIncrement("seller");
        databaseResetService.resetAutoIncrement("consumer");
        databaseResetService.resetAutoIncrement("product");
        databaseResetService.resetAutoIncrement("apartment");
        databaseResetService.resetAutoIncrement("admin");

        if (adminService.findAll().isEmpty()) {
            adminService.createTestAdmin();
        }

        if (apartmentService.findAll().isEmpty()) {
            apartmentService.createTestApartments();
        }

        if (productService.findAll().isEmpty()) {
            productService.createTestProducts();
        }

        if (sellerService.findAll().isEmpty()) {
            sellerService.createTestSellers();
        }

        if (consumerService.findAll().isEmpty()) {
            consumerService.createTestConsumers();
        }

        if (saleProductService.findAll().isEmpty()) {
            saleProductService.createTestSaleProducts();
        }

        if (customerOrderService.findAll().isEmpty()) {
            customerOrderService.createTestCustomerOrders();
        }

        if (reviewService.findAll().isEmpty()) {
            reviewService.createTestReviews();
        }
    }
}

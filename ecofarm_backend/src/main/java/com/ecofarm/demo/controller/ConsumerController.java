package com.ecofarm.demo.controller;

import com.ecofarm.demo.auth.dto.ErrorResponse;
import com.ecofarm.demo.auth.dto.JsonResponse;
import com.ecofarm.demo.dto.ConsumerDTO;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.service.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consumers")
public class ConsumerController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ConsumerDTO> getAllConsumers() {
        return consumerService.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("/{id}")
    public ResponseEntity<ConsumerDTO> getConsumerById(@PathVariable Long id) {
        Optional<Consumer> consumer = consumerService.findById(id);
        return consumer.map(c -> ResponseEntity.ok(convertToDTO(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @GetMapping("/profile/{email}")
    public ResponseEntity<ConsumerDTO> getConsumerByEmail(@PathVariable String email) {
        Optional<Consumer> consumer  = consumerService.findByEmail(email);
        return consumer.map(c -> ResponseEntity.ok(convertToDTO(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsumer(@PathVariable Long id) {
        consumerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @PutMapping("/delete/{email}")
    public ResponseEntity<Void> withdrawConsumer(@PathVariable String email) {
        try {
            consumerService.withdrawConsumer(email);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('CONSUMER')")
    @PutMapping("/update")
    public ResponseEntity<Consumer> updateConsumer(@RequestBody ConsumerDTO consumerDTO) {
        try {
            // 비밀번호가 널이 아닌 경우 비밀번호를 포함하여 업데이트
            if (consumerDTO.getPassword() != null && consumerDTO.getPassword().length() > 0) {
                Consumer updatedConsumer = consumerService.updateConsumerWithPassword(consumerDTO);
                return ResponseEntity.ok(updatedConsumer);
            } else {
                // 비밀번호가 널인 경우 비밀번호를 제외하고 업데이트
                Consumer updatedConsumer = consumerService.updateConsumerWithoutPassword(consumerDTO);
                return ResponseEntity.ok(updatedConsumer);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerConsumer(@RequestBody ConsumerDTO consumerDTO) {
        if (consumerDTO.getPassword() == null || consumerDTO.getPassword().length() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Password is empty."));
        }

        // 이메일 중복 체크
        if (consumerService.existsByEmail(consumerDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Email already in use."));
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(consumerDTO.getPassword());
        consumerDTO.setPassword(encodedPassword);

        consumerDTO.setSignupDate(LocalDateTime.now());

        // Consumer 엔티티 생성 및 저장
        Consumer consumer = convertToEntity(consumerDTO);
        consumerService.save(consumer);

        return ResponseEntity.status(HttpStatus.CREATED).body(new JsonResponse("Consumer registered successfully."));
    }

    private ConsumerDTO convertToDTO(Consumer consumer) {
        return ConsumerDTO.builder()
                .consumerID(consumer.getConsumerID())
                .consumerName(consumer.getConsumerName())
                .phoneNumber(consumer.getPhoneNumber())
                .email(consumer.getEmail())
                .password(consumer.getPassword())
                .address(consumer.getAddress())
                .age(consumer.getAge())
                .diseases(consumer.getDiseases())
                .signupDate(consumer.getSignupDate())
                .build();
    }

    private Consumer convertToEntity(ConsumerDTO dto) {
        Consumer consumer = new Consumer();
        consumer.setConsumerID(dto.getConsumerID());
        consumer.setConsumerName(dto.getConsumerName());
        consumer.setPhoneNumber(dto.getPhoneNumber());
        consumer.setEmail(dto.getEmail());
        consumer.setPassword(dto.getPassword());
        consumer.setAddress(dto.getAddress());
        consumer.setAge(dto.getAge());
        consumer.setDiseases((dto.getDiseases()));
        consumer.setSignupDate(dto.getSignupDate());
        return consumer;
    }
}
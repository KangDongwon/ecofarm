package com.ecofarm.demo.service;

import com.opencsv.CSVWriter;
import com.ecofarm.demo.dto.ConsumerDTO;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.entity.Product;
import com.ecofarm.demo.repository.ConsumerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ConsumerService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConsumerRepository consumerRepository;

    public List<Consumer> findAll() {
        return consumerRepository.findAll();
    }

    public Optional<Consumer> findById(Long id) {
        return consumerRepository.findById(id);
    }

    public Consumer save(Consumer consumer) {
        return consumerRepository.save(consumer);
    }

    public void deleteById(Long id) {
        consumerRepository.deleteById(id);
    }

    public ConsumerService(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    public boolean existsByEmail(String email) {
        return consumerRepository.existsByEmail(email);
    }

    public Optional<Consumer> findByEmail(String email) { return consumerRepository.findByEmail(email); }

    public Consumer updateConsumerWithPassword(ConsumerDTO consumerDTO) {
        Consumer consumer = consumerRepository.findByEmail(consumerDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Consumer not found"));

        // DTO에서 가져온 정보로 업데이트
        consumer.setConsumerName(consumerDTO.getConsumerName());
        consumer.setPhoneNumber(consumerDTO.getPhoneNumber());
        consumer.setAddress(consumerDTO.getAddress());
        consumer.setAge(consumerDTO.getAge());
        consumer.setDiseases(consumerDTO.getDiseases());

        // 비밀번호를 암호화하여 업데이트
        consumer.setPassword(passwordEncoder.encode(consumerDTO.getPassword()));

        return consumerRepository.save(consumer);
    }

    public Consumer updateConsumerWithoutPassword(ConsumerDTO consumerDTO) {
        Consumer consumer = consumerRepository.findByEmail(consumerDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Consumer not found"));

        // DTO에서 가져온 정보로 업데이트
        consumer.setConsumerName(consumerDTO.getConsumerName());
        consumer.setPhoneNumber(consumerDTO.getPhoneNumber());
        consumer.setAddress(consumerDTO.getAddress());
        consumer.setAge(consumerDTO.getAge());
        consumer.setDiseases(consumerDTO.getDiseases());

        return consumerRepository.save(consumer);
    }

    public void withdrawConsumer(String email) {
        Consumer consumer = consumerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 업데이트 필드 설정
        consumer.setConsumerName(null);
        consumer.setEmail(null);
        consumer.setPassword(null);
        consumer.setPhoneNumber(null);

        // 나머지 필드는 그대로 유지
        consumerRepository.save(consumer);
    }

    // 테스트 데이터 생성
    private static final String[] ADDRESSES = {
            "세종시 보람동", "세종시 아름동", "세종시 어진동",
            "세종시 고운동", "세종시 집현동", "대전시 유성구",
            "세종시 세종동", "세종시 나성동", "세종시 한솔동", "세종시 조치원"
    };

    private static final String[] NAMES = {
            "김철수", "이영희", "이을서", "갑을병", "홍길동",
            "김갑동", "박병남", "홍길순", "아무개", "김이박"
    };

    private static final String[] DISEASES = {
            "고지혈증", "비만", "고혈압", "심혈관질환", "암", "치매", "염증", "당뇨", "간질환",
            "고지혈증, 비만", "비만, 고혈압", "고혈압, 심혈관질환", "당뇨, 염증", "치매, 비만", "암, 염증",
            "고지혈증, 심혈관질환", "염증, 고혈압", "비만, 간질환", "당뇨, 치매", "심혈관질환, 비만", "비만, 염증"
    };

    @Transactional
    public void createTestConsumers() {
        Random random = new Random();
        for (int i = 1; i <= 200; i++) {
            String name = NAMES[random.nextInt(NAMES.length)];
            String email = "consumer" + i + "@test.com";
            String password = passwordEncoder.encode("1111");
            String phoneNumber = String.format("010-%04d-%04d", random.nextInt(10000), random.nextInt(10000));
            int age = random.nextInt(60) + 30;
            String disease = DISEASES[random.nextInt(DISEASES.length)];
            LocalDateTime signupDate = generateRandomSignupDate();

            Consumer consumer = new Consumer();
            consumer.setAddress(ADDRESSES[random.nextInt(ADDRESSES.length)]);
            consumer.setConsumerName(name);
            consumer.setEmail(email);
            consumer.setPassword(password);
            consumer.setPhoneNumber(phoneNumber);
            consumer.setSignupDate(signupDate);
            consumer.setAge(age);
            consumer.setDiseases(disease);

            consumerRepository.save(consumer);
        }
    }

    private LocalDateTime generateRandomSignupDate() {
        Random random = new Random();
        int year = random.nextBoolean() ? 2023 : 2024;
        int month = 0;
        if (year == 2023) {
            month = (int) (Math.random() * 12) + 1; // 1부터 12 사이의 무작위 월
        } else {
            month = (int) (Math.random() * 10) + 1; // 1부터 12 사이의 무작위 월
        }

        int day = (int) (Math.random() * 28) + 1; // 1부터 28 사이의 무작위 일
        int hour = (int) (Math.random() * 24); // 0부터 23 사이의 무작위 시간
        int minute = (int) (Math.random() * 60); // 0부터 59 사이의 무작위 분
        int second = (int) (Math.random() * 60); // 0부터 59 사이의 무작위 초

        return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    public void saveConsumersToCsv(String filePath) {
        List<Consumer> consumers = consumerRepository.findAll();

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // CSV 파일 헤더 작성
            String[] header = {"USER_ID", "AGE", "DISEASE"};
            writer.writeNext(header);

            for (Consumer consumer : consumers) {
                String[] data = {
                        String.valueOf(consumer.getConsumerID()), // OrderID
                        getAgeGroup(consumer.getAge()),
                        getFirstDisease(consumer.getDiseases())
                };
                writer.writeNext(data);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    // 질환 목록에서 첫 번째 질환을 가져오는 메서드
    private String getFirstDisease(String diseases) {
        if (diseases != null && !diseases.isEmpty()) {
            // 쉼표로 분리하여 첫 번째 질환 반환
            String[] diseaseArray = diseases.split(",");
            return diseaseArray[0].trim(); // 공백 제거 후 반환
        }
        return ""; // 질환이 없으면 빈 문자열 반환
    }

    // 나이를 기반으로 연령대 분류하는 메서드
    private String getAgeGroup(int age) {
        if (age < 20) {
            return "10";
        } else if (age < 30) {
            return "20";
        } else if (age < 40) {
            return "30";
        } else if (age < 50) {
            return "40";
        } else if (age < 60) {
            return "50";
        } else if (age < 70) {
            return "60";
        } else if (age < 80) {
            return "70";
        } else {
            return "80"; // 80대 이상
        }
    }
}

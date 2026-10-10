package com.ecofarm.demo.repository;

import com.ecofarm.demo.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, Long> {

    boolean existsByEmail(String email);

    // 이메일로 소비자 조회
    Optional<Consumer> findByEmail(String email);
}

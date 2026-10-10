package com.ecofarm.demo.service;

import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.repository.ConsumerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ConsumerRepository consumerRepository;

    public CustomUserDetailsService(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Consumer consumer = consumerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return org.springframework.security.core.userdetails.User
                .withUsername(consumer.getEmail())
                .password(consumer.getPassword())
                .roles("USER") // 권한을 적절히 설정
                .build();
    }
}

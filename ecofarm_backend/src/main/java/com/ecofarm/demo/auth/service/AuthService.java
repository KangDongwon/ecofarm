package com.ecofarm.demo.auth.service;

import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.entity.Seller;
import com.ecofarm.demo.entity.Admin;
import com.ecofarm.demo.repository.ConsumerRepository;
import com.ecofarm.demo.repository.SellerRepository;
import com.ecofarm.demo.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Autowired
    private ConsumerRepository consumerRepository;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);
        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            return new org.springframework.security.core.userdetails.User(
                    admin.getEmail(),
                    admin.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }
        Optional<Seller> sellerOptional = sellerRepository.findByEmailAndApprovedDateNotNull(email);
        if (sellerOptional.isPresent()) {
            Seller seller = sellerOptional.get();
            return new org.springframework.security.core.userdetails.User(
                    seller.getEmail(),
                    seller.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_SELLER"))
            );
        }
        sellerOptional = sellerRepository.findByEmail(email);
        if (sellerOptional.isPresent()) {
            Seller seller = sellerOptional.get();
            return new org.springframework.security.core.userdetails.User(
                    seller.getEmail(),
                    seller.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_WAITSELLER"))
            );
        }
        Optional<Consumer> consumerOptional = consumerRepository.findByEmail(email);
        if (consumerOptional.isPresent()) {
            Consumer consumer = consumerOptional.get();
            return new org.springframework.security.core.userdetails.User(
                    consumer.getEmail(),
                    consumer.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CONSUMER"))
            );
        }
        throw new UsernameNotFoundException("User not found : " + email);
    }
}
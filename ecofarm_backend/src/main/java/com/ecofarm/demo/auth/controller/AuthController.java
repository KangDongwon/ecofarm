package com.ecofarm.demo.auth.controller;

import com.ecofarm.demo.auth.dto.ErrorResponse;
import com.ecofarm.demo.auth.dto.LoginRequest;
import com.ecofarm.demo.auth.jwt.JwtTokenProvider;
import com.ecofarm.demo.auth.dto.LoginResponse;
import com.ecofarm.demo.entity.Consumer;
import com.ecofarm.demo.entity.Seller;
import com.ecofarm.demo.service.ConsumerService;
import com.ecofarm.demo.service.SellerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private ConsumerService consumerService;
    @Autowired
    private SellerService sellerService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 사용자 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            String authorityName = null;
            for (GrantedAuthority authority : authorities) {
                authorityName = authority.getAuthority();
                log.info("Authority: " + authorityName);
            }

            if (authorityName.equals("ROLE_WAITSELLER")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("가입 승인 대기 중입니다."));
            } else if (authorityName.equals("ROLE_SELLER")) {
                Optional<Seller> sellerOptional = sellerService.findByEmail(authentication.getName());
                if (sellerOptional.isPresent()) {
                    // jwt token 생성
                    String jwtToken = jwtTokenProvider.createToken(authentication.getName(), authorityName);
                    return ResponseEntity.ok(new LoginResponse(jwtToken, sellerOptional.get().getSellerName()));
                }
            } else if (authorityName.equals("ROLE_CONSUMER")) {
                Optional<Consumer> consumerOptional = consumerService.findByEmail(authentication.getName());
                if (consumerOptional.isPresent()) {
                    // jwt token 생성
                    String jwtToken = jwtTokenProvider.createToken(authentication.getName(), authorityName);
                    return ResponseEntity.ok(new LoginResponse(jwtToken, consumerOptional.get().getConsumerName()));
                }
            } else if (authorityName.equals("ROLE_ADMIN")) {
                // jwt token 생성
                String jwtToken = jwtTokenProvider.createToken(authentication.getName(), authorityName);
                return ResponseEntity.ok(new LoginResponse(jwtToken, "관리자"));
            }

            return ResponseEntity.status(401).body(new ErrorResponse("Invalid email or password"));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid email or password"));
        }
    }
}

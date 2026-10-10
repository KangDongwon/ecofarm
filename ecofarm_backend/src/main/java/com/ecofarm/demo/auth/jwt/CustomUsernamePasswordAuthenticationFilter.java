package com.ecofarm.demo.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecofarm.demo.auth.dto.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;


public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public CustomUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 요청 본문에서 로그인 정보 추출
        try {
            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
            //String email = obtainUsername(request);
            //String password = obtainPassword(request);
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();

            //log.info("here!!" + loginRequest.getEmail());
            //log.info("here!!" + loginRequest.getPassword());
            log.info("here!!" + email);
            log.info("here!!" + password);

            // 사용자명 또는 비밀번호가 없을 경우 예외 처리
            if (email == null || password == null) {
                throw new BadCredentialsException("Username or password is null") {
                };
            }
            // 인증 요청 객체 생성
            UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                    email, password);

            // 요청 상세 설정
            setDetails(request, authRequest);

            // 인증 시도
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("here!!666");
        // 인증 성공 후 처리 로직
        //super.successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("here!!444");
        // 인증 실패 후 처리 로직
        //super.unsuccessfulAuthentication(request, response, failed);
    }
}

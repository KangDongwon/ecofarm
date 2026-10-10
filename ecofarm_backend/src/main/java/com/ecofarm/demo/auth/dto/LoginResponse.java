package com.ecofarm.demo.auth.dto;

public class LoginResponse {

    private String token;
    private String name;

    public LoginResponse(String token, String name) {
        this.token = token;
        this.name = name;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.ecofarm.demo.auth.dto;

public class JsonResponse {
    private String message;

    public JsonResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.example.account.dto;

import lombok.*;

import java.time.LocalDateTime;

public class CreateAccount {
    @Getter
    @Setter
    public static class Request{
        Long userId;
        Long initialBalance;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response{
        Long userId;
        Long accountNumber;
        LocalDateTime registeredAt;
    }
}

package com.example.account;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountDtoTest {

    @Test
    void accountDto() {
        //given


        //when


        //then
        AccountDto accountDto=new AccountDto(
                "accountNumber","summer",
                LocalDateTime.now()
        );
        accountDto.setAccountNumber("123456789");
        accountDto.getAccountNumber();
        System.out.println(accountDto.toString());
    }

}
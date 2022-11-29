package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.repository.AccountRepository;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    /*@Autowired  //생성자를 알아서 만들어주는 신기한 녀석,,*/
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createdAccount_UserNotFound() {
        //given

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception=assertThrows(AccountException.class,
                ()->accountService.createAccount(1L,1000L));


        //then
        assertEquals(ErrorCode.USER_NOT_FOUND,exception.getErrorCode());


    }

    @Test
    @DisplayName("유저 당 최대 계좌는 10개")
    void createAccount_maxAccountIs10() {
        //given
        AccountUser user=AccountUser.builder()
                .id(15L)
                .name("Pobi")
                .build();  //변수화

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);
        //when
        AccountException exception=assertThrows(AccountException.class,
                ()->accountService.createAccount(1L,1000L));
        //then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10,exception.getErrorCode());

    }

    @Test
    @DisplayName("계좌 해지 성공")
    void deleteAccountSuccess() {
        //given
        AccountUser user=AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();  //변수화

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1000000012").build()));
        ArgumentCaptor<Account> captor=ArgumentCaptor.forClass(Account.class);


        //when
        AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");

        //then
        verify(accountRepository,times(1)).save(captor.capture());
        assertEquals(12L,accountDto.getUserId());
        assertEquals("1000000012",captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED,captor.getValue().getAccountStatus());


    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {
        //given

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception=assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L,"1234567890"));


        //then
        assertEquals(ErrorCode.USER_NOT_FOUND,exception.getErrorCode());


    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() {
        //given
        AccountUser user=AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();  //변수화

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());



        //when
        AccountException exception=assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L,"1234567890"));


        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND,exception.getErrorCode());


    }

    @Test
    @DisplayName("계좌 소유주가 다름")
    void deleteAccountFailed_userUnMatch() {
        //given
        AccountUser Pobi=AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();  //변수화

        AccountUser Marry=AccountUser.builder()
                .id(13L)
                .name("Marry")
                .build();  //변수화

        given(accountUserRepository.findById(anyLong()))  //확이 계좌 소유주는 pobi
                .willReturn(Optional.of(Pobi));

        given(accountRepository.findByAccountNumber(anyString())) //근데 소유주가 Marry
                .willReturn(Optional.of(Account.builder()
                        .accountUser(Marry)
                        .balance(0L)
                        .accountNumber("1000000012").build()));


        //when
        AccountException exception=assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L,"1234567890"));


        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH,exception.getErrorCode());


    }

    @Test
    @DisplayName("해지 계좌는 잔액이 없어야 한다.")
    void deleteAccountFailed_balanceNotEmpty() {
        //given
        AccountUser Pobi=AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();  //변수화


        given(accountUserRepository.findById(anyLong()))  //소유주는 pobi로 같지만
                .willReturn(Optional.of(Pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(Pobi)
                        .balance(100L) //계좌 잔액이 남아있을때
                        .accountNumber("1000000012").build()));


        //when
        AccountException exception=assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L,"1234567890"));


        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY,exception.getErrorCode());


    }

    @Test
    @DisplayName("해지된 계좌는 해지할 수 없다.")
    void deleteAccountFailed_alreadyUnregistered() {
        //given
        AccountUser Pobi=AccountUser.builder()
                .id(12L)
                .name("Pobi")
                .build();  //변수화


        given(accountUserRepository.findById(anyLong()))  //소유주는 pobi로 같지만
                .willReturn(Optional.of(Pobi));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(Pobi)
                        .accountStatus(AccountStatus.UNREGISTERED)//계좌가 이미 해지 계좌라면
                        .balance(0L)
                        .accountNumber("1000000012").build()));


        //when
        AccountException exception=assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L,"1234567890"));


        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED,exception.getErrorCode());


    }

    @Test
    @DisplayName("첫번째 계좌 생성")
    void createdFirstAccount() {
        //given
        AccountUser user=AccountUser.builder()
                .id(15L)
                .name("Pobi")
                .build();  //변수화

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor=ArgumentCaptor.forClass(Account.class);


        //when
        AccountDto accountDto = accountService.createAccount(1L, 100L);

        //then
        verify(accountRepository,times(1)).save(captor.capture());
        assertEquals(15L,accountDto.getUserId());
        assertEquals("1000000000",captor.getValue().getAccountNumber());

    }




}
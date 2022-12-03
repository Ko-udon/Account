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
import java.util.Arrays;
import java.util.List;
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
        AccountUser user=new AccountUser().builder()
                .name("Pobi")
                .build();  //변수화

        user.setId(12L);

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
                .name("Pobi")
                .build();  //변수화
        user.setId(12L);

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
                .name("Pobi")
                .build();  //변수화
        user.setId(12L);

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
                .name("Pobi")
                .build();  //변수화
        Pobi.setId(12L);

        AccountUser Marry=AccountUser.builder()
                .name("Marry")
                .build();  //변수화
        Marry.setId(13L);

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
                .name("Pobi")
                .build();  //변수화
        Pobi.setId(12L);


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
                .name("Pobi")
                .build();  //변수화
        Pobi.setId(12L);


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
    void successGetAccountsByUserId() {
        //given
        AccountUser Pobi=AccountUser.builder()
                .name("Pobi")
                .build();  //변수화
        Pobi.setId(12L);
        List<Account> accounts= Arrays.asList(
                Account.builder()
                        .accountUser(Pobi)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(Pobi)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(Pobi)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
        );

        given(accountUserRepository.findById(anyLong()))  //소유주는 pobi로 같지만
                .willReturn(Optional.of(Pobi));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        //when
        List<AccountDto> accountDtos = accountService.getAccountsByUserId(1L);


        //then
        assertEquals(3,accountDtos.size());
        assertEquals("1111111111",accountDtos.get(0).getAccountNumber());
        assertEquals(1000,accountDtos.get(0).getBalance());


        assertEquals("2222222222",accountDtos.get(1).getAccountNumber());
        assertEquals(2000,accountDtos.get(1).getBalance());

        assertEquals("3333333333",accountDtos.get(2).getAccountNumber());
        assertEquals(3000,accountDtos.get(2).getBalance());
    }

    @Test
    void failedToGetAccounts() {  //해당 계좌 사용자가 없음
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception=assertThrows(AccountException.class,
                ()->accountService.getAccountsByUserId(1L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND,exception.getErrorCode());
    }

    @Test
    @DisplayName("첫번째 계좌 생성")
    void createdFirstAccount() {
        //given
        AccountUser user=AccountUser.builder()
                .name("Pobi")
                .build();  //변수화
        user.setId(15L);
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
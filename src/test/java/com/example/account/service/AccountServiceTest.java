package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.type.AccountStatus;
import com.example.account.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    /*@Autowired  //생성자를 알아서 만들어주는 신기한 녀석,,*/
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 조회 성공")
    void testXX() {
        //given
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789")
                        .build()));
        ArgumentCaptor<Long> captor=ArgumentCaptor.forClass(Long.class);

        //when
        Account account = accountService.getAccount(455L);

        //then
        verify(accountRepository,times(1)).findById(captor.capture());
        verify(accountRepository,times(0)).save(any());
        assertEquals(455L,captor.getValue());
        assertNotEquals(4555L,captor.getValue());

        assertEquals("65789",account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED,account.getAccountStatus());

    }

    @Test
    @DisplayName("계좌 조회 실패 - 음수값 조회")
    void testFailedToSearchAccount() {
        //given


        //when
        RuntimeException exception=assertThrows(RuntimeException.class,
            () ->accountService.getAccount(-10L));

        //then

        assertEquals("Minus",exception.getMessage());

    }

    /*@BeforeEach
    void init(){
        accountService.createAccount();

    }*/

    @Test
    @DisplayName("Test 이름 내 마음대로")
    void testGetAccount1(){
        accountService.createAccount();
        Account account=accountService.getAccount(1L);

        assertEquals("40000",account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE,account.getAccountStatus());
    }

    @Test
    void testGetAccount2(){
        accountService.createAccount();
        Account account=accountService.getAccount(2L);

        assertEquals("40000",account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE,account.getAccountStatus());
    }

}
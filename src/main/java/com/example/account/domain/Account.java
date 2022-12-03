package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
//account 테이블을 마치 만든다고 생각하면 됨
public class Account extends BaseEntity{


    @ManyToOne
    private AccountUser accountUser;
    private String accountNumber;

    @Enumerated(EnumType.STRING) //enum타입은 봤을때 뭔지 모르므로 이와같은 지정자를 해두는게 좋다
    private AccountStatus accountStatus;
    private Long balance;

    private LocalDateTime registerdAt;
    private LocalDateTime UnRegisterdAt;


    public void useBalance(Long amount){
        if(amount>balance){
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance-=amount;
    }

    public void cancelBalance(Long amount){
        if(amount<0){
            throw new AccountException(ErrorCode.INVALID_REQUEST);
        }
        balance+=amount;
    }

}

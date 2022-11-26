package com.example.account.domain;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity

//account 테이블을 마치 만든다고 생각하면 됨
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    private String accountNumber;

    @Enumerated(EnumType.STRING)  //enum타입은 봤을때 뭔지 모르므로 이와같은 지정자를 해두는게 좋다
    private AccountStatus accountStatus;
}

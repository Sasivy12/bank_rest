package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetCardResponse
{
    private Long id;

    private String maskedCardNumber;

    private String ownerName;

    private String expirationDate;

    private double balance;

    private CardStatus status;

}

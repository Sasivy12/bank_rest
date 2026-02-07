package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class CreateCardResponse
{
    private String cardNumber;

    private String ownerFullName;

    private String expirationDate;

    private double balance;

    private CardStatus status;
}

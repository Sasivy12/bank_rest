package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * DTO для передачи информации о банковской карте клиенту.
 */
@Getter
@Setter
@AllArgsConstructor
public class CreateCardResponse
{
    /** Номер карты **/
    private String cardNumber;

    /** Полное имя владельца карты **/
    private String ownerFullName;

    /** Дата окончания срока действия карты **/
    private String expirationDate;

    /** Баланс карты **/
    private double balance;

    /** Статус карты (ACTIVE, EXPIRED, BLOCKED) **/
    private CardStatus status;
}

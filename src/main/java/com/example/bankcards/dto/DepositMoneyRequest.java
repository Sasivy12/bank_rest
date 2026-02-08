package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для внесения денег на баланс карты
 */
@Getter
@Setter
@AllArgsConstructor
public class DepositMoneyRequest
{
    /** Номер карты **/
    private String cardNumber;

    /** Сумма депозита **/
    private double sum;
}

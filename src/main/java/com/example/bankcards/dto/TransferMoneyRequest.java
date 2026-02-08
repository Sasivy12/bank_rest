package com.example.bankcards.dto;


import com.example.bankcards.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для перевода денег с одной карты пользователя на другую
 */
@Getter
@Setter
@AllArgsConstructor
public class TransferMoneyRequest
{
    /** ID первой карты (с которой деньги будут сняты) **/
    private Long firstCardId;

    /** ID второй карты (на которую деньги будут переведены) **/
    private Long secondCardId;

    /** Сумма перевода **/
    private double amount;

}

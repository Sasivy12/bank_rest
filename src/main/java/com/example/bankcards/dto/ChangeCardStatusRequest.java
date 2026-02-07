package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для изменения статуса карты
 */
@Setter
@Getter
@AllArgsConstructor
public class ChangeCardStatusRequest
{
    /** Номер карты **/
    private String cardNumber;

    /** Статус карты **/
    private CardStatus status;
}

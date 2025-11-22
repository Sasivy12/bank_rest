package com.example.bankcards.dto;


import com.example.bankcards.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransferMoneyRequest
{
    private Long firstCardId;

    private Long secondCardId;

    private int amount;

}

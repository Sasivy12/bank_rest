package com.example.bankcards.util;

public class CardUtils
{
    public static  String maskCardNumber(String cardNumber)
    {
        String last4Digits = cardNumber.substring(cardNumber.length() - 4);

        return "**** **** **** " + last4Digits;
    }
}

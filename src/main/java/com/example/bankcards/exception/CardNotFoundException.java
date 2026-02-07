package com.example.bankcards.exception;

/**
 * Исключение выбрасывается, когда карта с указанными даннами не была найдена
 */
public class CardNotFoundException extends RuntimeException
{
    public CardNotFoundException(String message)
    {
        super(message);
    }
}

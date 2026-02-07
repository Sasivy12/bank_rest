package com.example.bankcards.exception;

/**
 * Исключение выбрасывается, когда сумма для перевода денег с одной карты на другую <=0
 */
public class IncorrectSumException extends RuntimeException
{
    public IncorrectSumException(String message)
    {
        super(message);
    }
}

package com.example.bankcards.exception;

/**
 * Исключение выбрасывается, когда данные для перевода денег с одной карты на другую некорректны
 */
public class NotAcceptableTransferException extends RuntimeException
{
    public NotAcceptableTransferException(String message)
    {
        super(message);
    }
}

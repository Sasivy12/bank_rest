package com.example.bankcards.exception;

/**
 * Исключение выбрасывается, когда пользователь пытается взаимодействовать с чужими картами и не является Админом
 */
public class UnavailableTransferException extends RuntimeException
{
    public UnavailableTransferException(String message)
    {
        super(message);
    }
}

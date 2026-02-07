package com.example.bankcards.exception;

/**
 * Исключение выбрасывается, когда аутентификация не удалась
 */
public class AuthenticationFailedException extends RuntimeException
{
    public AuthenticationFailedException(String message)
    {
        super(message);
    }
}

package com.example.bankcards.exception;

/**
 * Исключение выбрасывается, когда пользователь с указанными данными уже зарегестрирован в системе
 */
public class UserAlreadyExistsException extends RuntimeException
{
    public UserAlreadyExistsException(String message)
    {
        super(message);
    }
}

package com.example.bankcards.exception;

/**
 * Исключение выбрасывается, когда пользователь с указанными данными не был найден
 */
public class UserNotFoundException extends RuntimeException
{
    public UserNotFoundException(String message)
    {
        super(message);
    }
}

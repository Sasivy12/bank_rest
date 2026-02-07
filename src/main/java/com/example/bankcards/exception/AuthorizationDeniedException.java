package com.example.bankcards.exception;

public class AuthorizationDeniedException extends RuntimeException
{
    public AuthorizationDeniedException(String message)
    {
        super(message);
    }
}

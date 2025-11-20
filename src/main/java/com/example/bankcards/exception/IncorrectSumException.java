package com.example.bankcards.exception;

public class IncorrectSumException extends RuntimeException
{
    public IncorrectSumException(String message)
    {
        super(message);
    }
}

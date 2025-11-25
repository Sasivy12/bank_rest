package com.example.bankcards.exception;


public class NotAcceptableTransferException extends RuntimeException
{
    public NotAcceptableTransferException(String message)
    {
        super(message);
    }
}

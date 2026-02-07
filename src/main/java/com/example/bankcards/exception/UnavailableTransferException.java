package com.example.bankcards.exception;

public class UnavailableTransferException extends RuntimeException
{
    public UnavailableTransferException(String message)
    {
        super(message);
    }
}

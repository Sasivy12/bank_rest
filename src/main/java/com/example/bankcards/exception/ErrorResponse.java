package com.example.bankcards.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для передачи информации об ошибке в ответе API
 * Используется в глобальном обработчике исключений для возврата клиенту кода ошибки и сообщения
 */
@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse
{
    private String errorCode;
    private String message;
}

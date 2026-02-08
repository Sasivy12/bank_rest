package com.example.bankcards.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * DTO для логина пользователя в систему
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class LoginRequest
{
    /** Email пользователя **/
    private String email;

    /** Пароль пользователя **/
    private String password;
}

package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO для регистрации пользователя
 */
@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest
{

    /** Email пользователя **/
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /** Пароль пользователя **/
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    /** Полное имя пользователя **/
    @NotBlank(message = "Full name is required")
    private String fullName;

    /** Роль пользователя (USER, ADMIN) **/
    private Role role;
}
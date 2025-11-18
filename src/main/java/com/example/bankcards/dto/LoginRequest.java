package com.example.bankcards.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class LoginRequest
{
    private String email;

    private String password;

    private String fullName;
}

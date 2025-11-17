package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String email;

    private String password;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;
}

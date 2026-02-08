package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Сущность пользователя
 */
@RequiredArgsConstructor
@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
public class User
{
    /** Идентифкатор пользователя **/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Email пользователя **/
    private String email;

    /** Пароль пользователя **/
    private String password;

    /** Полное имя пользователя **/
    private String fullName;

    /** Роль пользователя (USER, ADMIN) **/
    @Enumerated(EnumType.STRING)
    private Role role;
}

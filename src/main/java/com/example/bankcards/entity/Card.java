package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Сущность карты
 */
@Entity
@Table(name = "cards")
@RequiredArgsConstructor
@Setter
@Getter
public class Card
{
    /** Идентифкатор карты **/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Номер карты **/
    private String cardNumber;

    /** Владелец карты **/
    @ManyToOne
    private User owner;

    /** Срок действия карты **/
    private Date expirationDate;

    /** Баланс карты **/
    private double balance;

    /** Статус карты (ACTIVE, BLOCKED, EXPIRED) **/
    @Enumerated(EnumType.STRING)
    private CardStatus status;
}

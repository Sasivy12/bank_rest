package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "cards")
@RequiredArgsConstructor
public class Card
{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long cardNumber;

    @ManyToOne
    private User owner;

    private Date expirationDate;

    private double balance;

    @Enumerated(EnumType.STRING)
    private CardStatus status;
}

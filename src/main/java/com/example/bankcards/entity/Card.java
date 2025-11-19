package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "cards")
@RequiredArgsConstructor
@Setter
@Getter
public class Card
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String cardNumber;

    @ManyToOne
    private User owner;

    private Date expirationDate;

    private double balance;

    @Enumerated(EnumType.STRING)
    private CardStatus status;
}

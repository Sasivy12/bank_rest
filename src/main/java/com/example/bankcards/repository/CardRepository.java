package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>
{

    boolean existsByCardNumber(String cardNumber);

    Optional<Card> findByCardNumber(String cardNumber);

    Optional<List<Card>> findByOwner(User owner);
}

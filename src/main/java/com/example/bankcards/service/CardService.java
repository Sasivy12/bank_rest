package com.example.bankcards.service;

import com.example.bankcards.dto.CardPageResponse;
import com.example.bankcards.dto.ChangeCardStatusRequest;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.DepositMoneyRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.IncorrectSumException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CreditCardNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardService
{
    private final CardRepository cardRepository;

    private final UserRepository userRepository;

    private final CreditCardNumberGenerator generator = new CreditCardNumberGenerator();

    public ResponseEntity<Card> createCard(CreateCardRequest request)
    {
        Card card = new Card();
        User cardHolder = userRepository.findById(request.getOwnerId()).orElseThrow(
                ()-> new UserNotFoundException("User not found"));

        String cardNumber = generator.generate("220000", 16);
        while(cardRepository.existsByCardNumber(cardNumber))
        {
            cardNumber = generator.generate("220000", 16);
        }

        card.setCardNumber(cardNumber);

        card.setOwner(cardHolder);
        card.setBalance(0);
        card.setExpirationDate(request.getExpirationDate());
        card.setStatus(CardStatus.ACTIVE);

        cardRepository.save(card);

        return ResponseEntity.ok(card);
    }

    public ResponseEntity<String> changeCardStatus(ChangeCardStatusRequest request)
    {
        Card card = cardRepository.findByCardNumber(request.getCardNumber()).orElseThrow(
                () -> new CardNotFoundException("Card not found"));

        card.setStatus(request.getStatus());

        cardRepository.save(card);

        return ResponseEntity.ok("Card status changed successfully");
    }

    public ResponseEntity<String> deleteCard(Long cardId)
    {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new CardNotFoundException("Card not found"));

        cardRepository.delete(card);

        return ResponseEntity.ok("Card deleted successfully");
    }

    public ResponseEntity<Double> getCardBalance(Long cardId)
    {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new CardNotFoundException("Card not found"));

        return ResponseEntity.ok(card.getBalance());
    }

    public ResponseEntity<CardPageResponse> getAllCardsForUser(Long userId, int page, int size)
    {
        User owner = userRepository.findById(userId).orElseThrow(
            ()-> new UserNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);

        Page<Card> userCards = cardRepository.findByOwner(owner, pageable);

        if (userCards.isEmpty())
        {
            throw new CardNotFoundException("Cards not found");
        }

        CardPageResponse response = new CardPageResponse
                (
                userCards.getContent(),
                userCards.getNumber(),
                userCards.getSize(),
                userCards.getTotalElements(),
                userCards.getTotalPages(),
                userCards.isFirst(),
                userCards.isLast()
                );

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<String> depositMoney(DepositMoneyRequest request)
    {
        Card card = cardRepository.findByCardNumber(request.getCardNumber()).orElseThrow(
                () -> new CardNotFoundException("Card not found"));

        if(request.getSum() > 0)
        {
            card.setBalance(card.getBalance() + request.getSum());
            cardRepository.save(card);
        }
        else
        {
            throw new IncorrectSumException("Sum of money should be higher than 0!");
        }

        return ResponseEntity.ok("Sum of: " + request.getSum() + " successfully deposited");
    }
}

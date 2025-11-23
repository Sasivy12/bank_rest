package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.IncorrectSumException;
import com.example.bankcards.exception.NotAcceptableTransferException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardUtils;
import com.example.bankcards.util.CreditCardNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

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

        List<CardResponse> cardResponses = userCards.getContent().stream()
                .map(card -> new CardResponse(
                        card.getId(),
                        CardUtils.maskCardNumber(card.getCardNumber()),
                        card.getOwner().getFullName(),
                        new SimpleDateFormat("MM/yy").format(card.getExpirationDate()),
                        card.getBalance(),
                        card.getStatus()
                ))
                .toList();

        return ResponseEntity.ok(new CardPageResponse
                (
                    cardResponses,
                    userCards.getNumber(),
                    userCards.getSize(),
                    userCards.getTotalElements(),
                    userCards.getTotalPages(),
                    userCards.isFirst(),
                    userCards.isLast()
                )
        );
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

    public ResponseEntity<String> transferMoney(TransferMoneyRequest request)
    {
        Card firstCard = cardRepository.findById(request.getFirstCardId()).orElseThrow(
                () -> new CardNotFoundException("Card not found"));

        Card secondCard = cardRepository.findById(request.getSecondCardId()).orElseThrow(
                () -> new CardNotFoundException("Card not found"));

        if(request.getAmount() > 0 && firstCard.getBalance() >= request.getAmount() &&
                firstCard.getStatus() == CardStatus.ACTIVE && secondCard.getStatus() == CardStatus.ACTIVE)
        {
            firstCard.setBalance(firstCard.getBalance() - request.getAmount());
            secondCard.setBalance(secondCard.getBalance() + request.getAmount());

            cardRepository.save(firstCard);
            cardRepository.save(secondCard);

            return ResponseEntity.ok("Transfer successfull");
        }
        else
        {
            throw new NotAcceptableTransferException("Transfer is not acceptable");
        }
    }
}

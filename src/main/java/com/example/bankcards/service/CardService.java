package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardUtils;
import com.example.bankcards.util.CreditCardNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public ResponseEntity<CreateCardResponse> createCard(CreateCardRequest request)
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

        CreateCardResponse response = new CreateCardResponse(
                card.getCardNumber(),
                card.getOwner().getFullName(),
                new SimpleDateFormat("MM/yy").format(card.getExpirationDate()),
                card.getBalance(),
                card.getStatus()
        );

        return ResponseEntity.ok(response);
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

        String currUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if(card.getOwner().getEmail().equals(currUserEmail))
        {
            return ResponseEntity.ok(card.getBalance());
        }
        else
        {
            throw new AccessDeniedException("You do not own this card");
        }
    }

    public ResponseEntity<CardPageResponse> getAllCardsForUser(Long userId, int page, int size)
    {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!owner.getEmail().equals(currentEmail) && !isAdmin)
        {
            throw new AccessDeniedException("You can access only your own cards");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Card> userCards = cardRepository.findByOwner(owner, pageable);

        if (userCards.isEmpty())
        {
            throw new CardNotFoundException("Cards not found");
        }

        List<GetCardResponse> cardResponses = userCards.getContent().stream()
                .map(card -> new GetCardResponse(
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

        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if(!card.getOwner().getEmail().equals(currentEmail))
        {
            throw new AccessDeniedException("You do not own this card");
        }

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

        if(!firstCard.getOwner().getEmail().equals(secondCard.getOwner().getEmail()))
        {
            throw new AccessDeniedException("You can only transfer money to your own cards");
        }

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

        public ResponseEntity<CardPageResponse> getAllCards(int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);

        Page<Card> userCards = cardRepository.findAll(pageable);

        if (userCards.isEmpty())
        {
            throw new CardNotFoundException("Cards not found");
        }

        List<GetCardResponse> cardResponses = userCards.getContent().stream()
                .map(card -> new GetCardResponse(
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
}

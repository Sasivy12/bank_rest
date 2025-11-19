package com.example.bankcards.service;


import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CreditCardNumberGenerator;
import lombok.RequiredArgsConstructor;
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

//    public ResponseEntity<String> blockCard()
//    {
//
//    }
}

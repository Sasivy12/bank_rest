package com.example.bankcards.service;

import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.CreateCardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CreditCardNumberGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CardServiceTest
{
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreditCardNumberGenerator generator;

    @InjectMocks
    private CardService cardService;


    @Test
    void createCard_success() {
        CreateCardRequest request = new CreateCardRequest(
                1L,
                Date.valueOf("2030-11-30")
        );

        User user = new User();
        user.setFullName("John Doe");

        when(userRepository.findById(request.getOwnerId())).thenReturn(Optional.of(user));

        when(generator.generate("220000", 16)).thenReturn("2200001234567890");

        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);

        ResponseEntity<CreateCardResponse> response =
                cardService.createCard(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        CreateCardResponse body = response.getBody();

        assertEquals("2200001234567890", body.getCardNumber());
        assertEquals("John Doe", body.getOwnerFullName());
        assertEquals(CardStatus.ACTIVE, body.getStatus());
        assertEquals(0, body.getBalance());

        verify(cardRepository).save(any(Card.class));
    }

}
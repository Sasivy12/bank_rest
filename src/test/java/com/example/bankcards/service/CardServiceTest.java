package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CreditCardNumberGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.data.domain.Pageable;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CardServiceTest
{
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreditCardNumberGenerator generator;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CardService cardService;


    @Test
    void createCard_success()
    {
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

    @Test
    void createCard_userNotFound()
    {
        CreateCardRequest request = new CreateCardRequest(
                1L,
                Date.valueOf("2030-11-30")
        );

        when(userRepository.findById(request.getOwnerId())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> cardService.createCard(request));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(request.getOwnerId());
    }

    @Test
    void changeCardStatus_success()
    {
        ChangeCardStatusRequest request = new ChangeCardStatusRequest(
                "2200001234567890",
                CardStatus.BLOCKED
        );

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("2200001234567890");
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findByCardNumber(request.getCardNumber())).thenReturn(Optional.of(card));

        ResponseEntity<String> response = cardService.changeCardStatus(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Card status changed successfully", response.getBody());
        assertEquals(CardStatus.BLOCKED, card.getStatus());

        verify(cardRepository).findByCardNumber(request.getCardNumber());
        verify(cardRepository).save(card);
    }

    @Test
    void changeCardStatus_cardNotFound()
    {
        ChangeCardStatusRequest request = new ChangeCardStatusRequest(
                "2200001234567890",
                CardStatus.BLOCKED
        );

        when(cardRepository.findByCardNumber(request.getCardNumber())).thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.changeCardStatus(request));

        assertEquals("Card not found", exception.getMessage());

        verify(cardRepository).findByCardNumber(request.getCardNumber());
    }

    @Test
    void deleteCard_success()
    {
        Long cardId = 1L;

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("2200001234567890");
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        ResponseEntity<String> response = cardService.deleteCard(cardId);

        assertEquals("Card deleted successfully", response.getBody());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(cardRepository).findById(cardId);
        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_cardNotFound()
    {
        Long cardId = 1L;

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.deleteCard(cardId));

        assertEquals("Card not found", exception.getMessage());

        verify(cardRepository).findById(cardId);
    }

    @Test
    void depositMoney_success()
    {
        DepositMoneyRequest request = new DepositMoneyRequest(
                "2200001234567890",
                1000
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("john@mail.com");

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("2200001234567890");
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(0);
        card.setOwner(user);

        when(cardRepository.findByCardNumber(request.getCardNumber())).thenReturn(Optional.of(card));

        when(authentication.getName()).thenReturn("john@mail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ResponseEntity<String> response = cardService.depositMoney(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Sum of: 1000.0 successfully deposited", response.getBody());
        assertNotNull(response.getBody());
        assertEquals(1000, card.getBalance());

        verify(cardRepository).findByCardNumber(request.getCardNumber());
        verify(cardRepository).save(card);
    }

    @Test
    void depositMoney_cardNotFound()
    {
        DepositMoneyRequest request = new DepositMoneyRequest(
                "2200001234567890",
                1000
        );

        when(cardRepository.findByCardNumber(request.getCardNumber())).thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.depositMoney(request));

        assertEquals("Card not found", exception.getMessage());

        verify(cardRepository).findByCardNumber(request.getCardNumber());
    }

    @Test
    void depositMoney_userDoesNotOwnTheCard()
    {
        DepositMoneyRequest request = new DepositMoneyRequest(
                "2200001234567890",
                1000
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("john@mail.com");

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("2200001234567890");
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(0);
        card.setOwner(user);

        when(cardRepository.findByCardNumber(request.getCardNumber())).thenReturn(Optional.of(card));

        when(authentication.getName()).thenReturn("notjohn@mail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UnavailableTransferException exception = assertThrows(UnavailableTransferException.class,
                () -> cardService.depositMoney(request));

        assertEquals("You do not own this card", exception.getMessage());

        verify(cardRepository).findByCardNumber(request.getCardNumber());
    }

    @Test
    void depositMoney_incorrectSum()
    {
        DepositMoneyRequest request = new DepositMoneyRequest(
                "2200001234567890",
                -1000
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("john@mail.com");

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("2200001234567890");
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(0);
        card.setOwner(user);

        when(cardRepository.findByCardNumber(request.getCardNumber())).thenReturn(Optional.of(card));

        when(authentication.getName()).thenReturn("john@mail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        IncorrectSumException exception = assertThrows(IncorrectSumException.class,
                () -> cardService.depositMoney(request));

        assertEquals("Sum of money should be higher than 0!", exception.getMessage());

        verify(cardRepository).findByCardNumber(request.getCardNumber());
    }

    @Test
    void transferMoney_success()
    {
        TransferMoneyRequest request = new TransferMoneyRequest(
                1L,
                2L,
                1000
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("john@mail.com");

        Card firstCard = new Card();
        firstCard.setId(1L);
        firstCard.setCardNumber("2200001234567890");
        firstCard.setStatus(CardStatus.ACTIVE);
        firstCard.setBalance(1500);
        firstCard.setOwner(user);

        Card secondCard = new Card();
        secondCard.setId(2L);
        secondCard.setCardNumber("2200002345678901");
        secondCard.setStatus(CardStatus.ACTIVE);
        secondCard.setBalance(0);
        secondCard.setOwner(user);

        when(cardRepository.findById(request.getFirstCardId())).thenReturn(Optional.of(firstCard));
        when(cardRepository.findById(request.getSecondCardId())).thenReturn(Optional.of(secondCard));

        ResponseEntity<String> response = cardService.transferMoney(request);

        assertEquals("Transfer successful", response.getBody());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals(500, firstCard.getBalance());
        assertEquals(1000, secondCard.getBalance());

        verify(cardRepository).findById(request.getFirstCardId());
        verify(cardRepository).findById(request.getSecondCardId());
        verify(cardRepository).save(firstCard);
        verify(cardRepository).save(secondCard);
    }

    @Test
    void transferMoney_cardNotFound()
    {
        TransferMoneyRequest request = new TransferMoneyRequest(
                1L,
                2L,
                1000
        );

        when(cardRepository.findById(request.getFirstCardId())).thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.transferMoney(request));

        assertEquals("Card not found", exception.getMessage());

        verify(cardRepository).findById(request.getFirstCardId());
    }

    @Test
    void transferMoney_unavailableTransfer()
    {
        TransferMoneyRequest request = new TransferMoneyRequest(
                1L,
                2L,
                1000
        );

        User firstUser = new User();
        firstUser.setId(1L);
        firstUser.setEmail("john@mail.com");

        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setEmail("notjohn@mail.com");

        Card firstCard = new Card();
        firstCard.setId(1L);
        firstCard.setCardNumber("2200001234567890");
        firstCard.setStatus(CardStatus.ACTIVE);
        firstCard.setBalance(1500);
        firstCard.setOwner(firstUser);

        Card secondCard = new Card();
        secondCard.setId(1L);
        secondCard.setCardNumber("2200002345678901");
        secondCard.setStatus(CardStatus.ACTIVE);
        secondCard.setBalance(0);
        secondCard.setOwner(secondUser);

        when(cardRepository.findById(request.getFirstCardId())).thenReturn(Optional.of(firstCard));
        when(cardRepository.findById(request.getSecondCardId())).thenReturn(Optional.of(secondCard));

        UnavailableTransferException exception = assertThrows(UnavailableTransferException.class,
                () -> cardService.transferMoney(request));

        assertEquals("You can only transfer money to your own cards", exception.getMessage());

        verify(cardRepository).findById(request.getFirstCardId());
        verify(cardRepository).findById(request.getSecondCardId());
    }

    @Test
    void transferMoney_transferNotAcceptable()
    {
        TransferMoneyRequest request = new TransferMoneyRequest(
                1L,
                2L,
                1000
        );

        User user = new User();
        user.setId(1L);
        user.setEmail("john@mail.com");

        Card firstCard = new Card();
        firstCard.setId(1L);
        firstCard.setCardNumber("2200001234567890");
        firstCard.setStatus(CardStatus.BLOCKED);
        firstCard.setBalance(1500);
        firstCard.setOwner(user);

        Card secondCard = new Card();
        secondCard.setId(2L);
        secondCard.setCardNumber("2200002345678901");
        secondCard.setStatus(CardStatus.ACTIVE);
        secondCard.setBalance(0);
        secondCard.setOwner(user);

        when(cardRepository.findById(request.getFirstCardId())).thenReturn(Optional.of(firstCard));
        when(cardRepository.findById(request.getSecondCardId())).thenReturn(Optional.of(secondCard));

        NotAcceptableTransferException exception = assertThrows(NotAcceptableTransferException.class,
                () -> cardService.transferMoney(request));

        assertEquals("Transfer is not acceptable", exception.getMessage());

        verify(cardRepository).findById(request.getFirstCardId());
        verify(cardRepository).findById(request.getSecondCardId());
    }

    @Test
    void getAllCardsForUser_success()
    {
        Long userId = 1L;
        int page = 0;
        int size = 5;

        User user = new User();
        user.setId(1L);
        user.setEmail("john@mail.com");

        when(authentication.getName()).thenReturn("john@mail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("2200001234567890");
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(1000);
        card.setOwner(user);
        card.setExpirationDate(Date.valueOf("2030-11-30"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardPage = new PageImpl<>(List.of(card), pageable, 1);

        when(cardRepository.findByOwner(user, pageable)).thenReturn(cardPage);

        ResponseEntity<CardPageResponse> response = cardService.getAllCardsForUser(userId, page, size);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        CardPageResponse cardPageResponse = response.getBody();

        assertEquals(1, cardPageResponse.getTotalElements());
        assertEquals(1, cardPageResponse.getCards().size());

        GetCardResponse getCardResponse = cardPageResponse.getCards().get(0);
        assertEquals(1L, getCardResponse.getId());
        assertEquals(CardStatus.ACTIVE, getCardResponse.getStatus());

        verify(userRepository).findById(userId);
        verify(cardRepository).findByOwner(user, pageable);
    }

    @Test
    void getAllCardForUser_userNotFound()
    {
        Long userId = 1L;
        int page = 0;
        int size = 5;

        when(authentication.getName()).thenReturn("john@mail.com");
        when(authentication.getAuthorities()).thenReturn(List.of());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> cardService.getAllCardsForUser(userId, page, size));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(userId);
    }

    @Test
    void getAllCardForUser_unavailableTransferException()
    {
        Long userId = 1L;
        int page = 0;
        int size = 5;

        User user = new User();
        user.setId(1L);
        user.setEmail("notjohn@mail.com");

        when(authentication.getName()).thenReturn("john@mail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UnavailableTransferException exception = assertThrows(UnavailableTransferException.class,
                () -> cardService.getAllCardsForUser(userId, page, size));

        assertEquals("You can access only your own cards", exception.getMessage());

        verify(userRepository).findById(userId);
    }

    @Test
    void getAllCardsForUser_cardsNotFound()
    {
        Long userId = 1L;
        int page = 0;
        int size = 5;

        User user = new User();
        user.setId(1L);
        user.setEmail("john@mail.com");

        when(authentication.getName()).thenReturn("john@mail.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cardPage = new PageImpl<>(List.of(), pageable, 1);

        when(cardRepository.findByOwner(user, pageable)).thenReturn(cardPage);

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.getAllCardsForUser(userId, page, size));

        assertEquals("Cards not found", exception.getMessage());

        verify(userRepository).findById(userId);
        verify(cardRepository).findByOwner(user, pageable);
    }

    @Test
    void getAllCards_success()
    {
        int page = 0;
        int size = 5;

        Pageable pageable = PageRequest.of(page, size);

        User user = new User();
        user.setId(1L);
        user.setEmail("notjohn@mail.com");

        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("2200001234567890");
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(1000);
        card.setOwner(user);
        card.setExpirationDate(Date.valueOf("2030-11-30"));

        Page<Card> userCards = new PageImpl<>(List.of(card), pageable, 1);

        when(cardRepository.findAll(pageable)).thenReturn(userCards);

        ResponseEntity<CardPageResponse> response = cardService.getAllCards(page, size);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

         CardPageResponse cardPageResponse = response.getBody();
         assertEquals(1, cardPageResponse.getTotalPages());
         assertEquals(1, cardPageResponse.getCards().size());

         GetCardResponse getCardResponse = cardPageResponse.getCards().get(0);
         assertEquals(1L, getCardResponse.getId());
         assertEquals(CardStatus.ACTIVE, getCardResponse.getStatus());

         verify(cardRepository).findAll(pageable);
    }

    @Test
    void getAllCards_cardsNotFound()
    {
        int page = 0;
        int size = 5;

        Pageable pageable = PageRequest.of(page, size);

        Page<Card> userCards = new PageImpl<>(List.of(), pageable, 1);

        when(cardRepository.findAll(pageable)).thenReturn(userCards);

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> cardService.getAllCards(page, size));

        assertEquals("Cards not found", exception.getMessage());

        verify(cardRepository).findAll(pageable);
    }

}
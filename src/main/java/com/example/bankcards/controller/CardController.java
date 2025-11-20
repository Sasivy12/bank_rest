package com.example.bankcards.controller;

import com.example.bankcards.dto.CardPageResponse;
import com.example.bankcards.dto.ChangeCardStatusRequest;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.DepositMoneyRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CardController
{
    private final CardService cardService;

    @PostMapping("/card")
    public ResponseEntity<Card> createCard(@RequestBody CreateCardRequest request)
    {
        return cardService.createCard(request);
    }

    @PostMapping("/card/status")
    public ResponseEntity<String> blockCard(@RequestBody ChangeCardStatusRequest request)
    {
        return cardService.changeCardStatus(request);
    }

    @DeleteMapping("/card/{cardId}")
    public ResponseEntity<String> deleteCard(@PathVariable("cardId") Long cardId)
    {
        return cardService.deleteCard(cardId);
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<Double> getCardBalance(@PathVariable("cardId") Long cardId)
    {
        return cardService.getCardBalance(cardId);
    }

    @GetMapping("/card/user/{userId}")
    public ResponseEntity<CardPageResponse> getAllCardsForUser(@PathVariable("userId") Long userId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size)
    {
        return cardService.getAllCardsForUser(userId, page, size);
    }

    @PostMapping("/card/deposit")
    public ResponseEntity<String> depositMoney(@RequestBody DepositMoneyRequest request)
    {
        return cardService.depositMoney(request);
    }
}

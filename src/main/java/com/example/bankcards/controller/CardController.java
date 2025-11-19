package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

}

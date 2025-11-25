package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Управление картами", description = "Все операции, связанные с управлением картами")
public class CardController
{
    private final CardService cardService;

    @PostMapping("/card")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Создание новой карты для пользователя")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<Card> createCard(@Valid @RequestBody CreateCardRequest request)
    {
        return cardService.createCard(request);
    }

    @PostMapping("/card/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Обновление статуса карты (ACTIVE, BLOCKED, EXPIRED)")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<String> changeCardStatus(@RequestBody ChangeCardStatusRequest request)
    {
        return cardService.changeCardStatus(request);
    }

    @DeleteMapping("/card/{cardId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Удаление карты")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<String> deleteCard(@PathVariable("cardId") Long cardId)
    {
        return cardService.deleteCard(cardId);
    }

    @GetMapping("/card/{cardId}")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Получение баланса карты")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<Double> getCardBalance(@PathVariable("cardId") Long cardId)
    {
        return cardService.getCardBalance(cardId);
    }

    @GetMapping("/card/user/{userId}")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Получение всех карт пользователя")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<CardPageResponse> getAllCardsForUser(@PathVariable("userId") Long userId,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size)
    {
        return cardService.getAllCardsForUser(userId, page, size);
    }

    @PostMapping("/card/deposit")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Пополнение денег на балансе карты")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "406", description = "Недопустимая сумма"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<String> depositMoney(@RequestBody DepositMoneyRequest request)
    {
        return cardService.depositMoney(request);
    }

    @PostMapping("/card/transfer")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Перевод денег с одной карты пользователя на другую")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "406", description = "Недопустимая сумма"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<String> transferMoney(@RequestBody TransferMoneyRequest request)
    {
        return cardService.transferMoney(request);
    }

    @GetMapping("/card")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Получение всех карт")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<CardPageResponse> getAllCards(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size)
    {
        return cardService.getAllCards(page, size);
    }

}

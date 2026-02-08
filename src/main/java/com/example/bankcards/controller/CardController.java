package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.*;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления банковскими картами
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Управление картами", description = "Все операции, связанные с управлением картами")
public class CardController
{
    private final CardService cardService;

    /**
     * Выполняет создание новых банковских карт
     * @param request объект с данными для создания карты
     * @return Response Entity с данными созданной карты
     * @throws UserNotFoundException если пользователь с таким id не найден
     */
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
    public ResponseEntity<CreateCardResponse> createCard(@Valid @RequestBody CreateCardRequest request)
    {
        return cardService.createCard(request);
    }

    /**
     * Выполняет изменение статуса карты (Active, Blocked, Expired)
     * @param request объект с данными для с изменения статуса карты
     * @return Response Entity с сообщением об успешном изменении статуса карты
     * @throws CardNotFoundException если карта с таким номером не найдена
     */
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

    /**
     * Выполняет удаление банковской карты
     * @param cardId уникальный идентификатор карты
     * @return Response Entity с сообщением об успешном удалении карты
     * @throws CardNotFoundException если карта с таким идентификатором не найдена
     */
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

    /**
     * Возвращает баланс карты
     * @param cardId уникальный идентфикатор карты
     * @return ResponseEntity с балансом карты
     * @throws CardNotFoundException если карта с таким идентификатором не найдена
     * @throws UnavailableTransferException если пользователь пытается получить баланс карты, которая ему не принадлежит
     */
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

    /**
     * Возвращает все карты пользователя
     * @param userId идентификатор пользователя
     * @param page номер страницы
     * @param size количество элементов на странице
     * @return ResponseEntity с объектом содержащим список карт и информацию о пагинации
     * @throws UserNotFoundException если пользователь с таким id не найден
     * @throws UnavailableTransferException если пользователь не является владельцем карт и не имеет роли Admin
     * @throws CardNotFoundException если у пользователя нет карт
     */
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

    /**
     * Выполняет внесение денег на баланс карты
     * @param request объект с данными для депозита денег на баланс карты пользователя
     * @return Response Entity с сообщением об успешном внесении денег на баланс
     * @throws CardNotFoundException если карта с таким номером не найдена
     * @throws IncorrectSumException если сумма депозита  <= 0
     */
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

    /**
     * Выполняет перевод денег с одной карты пользователя на другую
     * @param request объект с данными для перевода денег с карты на карту
     * @return Response Entity с сообщением об успешном переводе денег
     * @throws CardNotFoundException если карты с данными id не были найдены
     * @throws UnavailableTransferException если пользователь не является владельцем обеих карт
     * @throws NotAcceptableTransferException если карты не активны,
     * если баланс первой карты меньше, чем сумма перевода и если сумма перевода <=0
     */
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

    /**
     * Возвращает все банковские карты в системе
     * @param page номер страницы
     * @param size количество элементов на странице
     * @return Response Entity с объектом, содержащим список всех карт и данные пагинации
     * @throws CardNotFoundException если карт в системе нет
     */
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

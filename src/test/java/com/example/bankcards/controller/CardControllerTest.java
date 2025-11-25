package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private CardService cardService;

    @Test
    @WithMockUser(username = "admin@mail.com", authorities = "ADMIN")
    void testCreateCardSuccess() throws Exception
    {
        LocalDate expDate = LocalDate.of(2026, 12, 31);
        Date expirationDate = java.sql.Date.valueOf(expDate);

        CreateCardRequest request = new CreateCardRequest(1L, expirationDate);

        User user = new User();
        user.setId(1L);
        user.setEmail("user@mail.com");

        Card card = new Card();
        card.setId(1L);
        card.setOwner(user);
        card.setBalance(0);
        card.setStatus(CardStatus.ACTIVE);
        card.setCardNumber("2200001234567890");
        card.setExpirationDate(expirationDate);

        when(cardService.createCard(any(CreateCardRequest.class)))
                .thenReturn(ResponseEntity.ok(card));

        mockMvc.perform(post("/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value(org.hamcrest.Matchers.startsWith("220000")))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", authorities = "ADMIN")
    void testChangeCardStatusSuccess() throws Exception
    {
        ChangeCardStatusRequest request = new ChangeCardStatusRequest(
                "2200001234567890",
                CardStatus.BLOCKED
        );

        when(cardService.changeCardStatus(any(ChangeCardStatusRequest.class)))
                .thenReturn(ResponseEntity.ok("Card status changed successfully"));

        mockMvc.perform(post("/card/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Card status changed successfully"));
    }

    @Test
    @WithMockUser(username = "user@mail.com", authorities = "USER")
    void testGetCardBalanceSuccess() throws Exception
    {
        when(cardService.getCardBalance(anyLong())).thenReturn(ResponseEntity.ok(1500.0));

        mockMvc.perform(get("/card/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.0"));
    }

    @Test
    @WithMockUser(username = "user@mail.com", authorities = "USER")
    void testGetAllCardsForUserSuccess() throws Exception
    {
        CardPageResponse response = new CardPageResponse(
                Collections.emptyList(),
                0, 10, 0, 0, true, true
        );

        when(cardService.getAllCardsForUser(anyLong(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/card/user/1?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(username = "user@mail.com", authorities = {"USER"})
    void testDepositMoneySuccess() throws Exception
    {
        DepositMoneyRequest request = new DepositMoneyRequest("2200001234567890", 500.0);

        when(cardService.depositMoney(any(DepositMoneyRequest.class)))
                .thenReturn(ResponseEntity.ok("Sum of: 500.0 successfully deposited"));

        mockMvc.perform(post("/card/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Sum of: 500.0 successfully deposited"));
    }

    @Test
    @WithMockUser(username = "user@mail.com", authorities = {"USER"})
    void testTransferMoneySuccess() throws Exception
    {
        TransferMoneyRequest request = new TransferMoneyRequest(1L, 2L, 300.0);

        when(cardService.transferMoney(any(TransferMoneyRequest.class)))
                .thenReturn(ResponseEntity.ok("Transfer successful"));

        mockMvc.perform(post("/card/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful"));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", authorities = {"ADMIN"})
    void testGetAllCardsSuccess() throws Exception
    {
        CardPageResponse response = new CardPageResponse(
                Collections.emptyList(),
                0, 10, 0, 0, true, true
        );

        when(cardService.getAllCards(anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/card?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

}

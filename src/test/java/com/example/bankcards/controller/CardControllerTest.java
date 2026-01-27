package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.NotAcceptableTransferException;
import com.example.bankcards.exception.UnavailableTransferException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CardController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createCard_success() throws Exception {

        CreateCardRequest request = new CreateCardRequest(
                1L,
                new SimpleDateFormat("yyyy-MM-dd").parse("2028-12-31")
        );

        CreateCardResponse response = new CreateCardResponse(
                "220000******1234",
                "John Doe",
                "12/28",
                0.0,
                CardStatus.ACTIVE
        );

        when(cardService.createCard(any(CreateCardRequest.class)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value("220000******1234"))
                .andExpect(jsonPath("$.ownerFullName").value("John Doe"))
                .andExpect(jsonPath("$.expirationDate").value("12/28"))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(cardService).createCard(any(CreateCardRequest.class));
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void createCard_badRequest() throws Exception
    {
        CreateCardRequest request = new CreateCardRequest(
                null,
                new SimpleDateFormat("yyyy-MM-dd").parse("2028-12-31")
        );

        mockMvc.perform(post("/card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void changeCardStatus_success() throws Exception
    {
        ChangeCardStatusRequest request = new ChangeCardStatusRequest(
                "2200006646955942",
                CardStatus.ACTIVE
        );

        when(cardService.changeCardStatus(any(ChangeCardStatusRequest.class))).
                thenReturn(ResponseEntity.ok("Card status changed successfully"));

        mockMvc.perform(post("/card/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Card status changed successfully"));


        verify(cardService).changeCardStatus(any(ChangeCardStatusRequest.class));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void changeCardStatus_notFound() throws Exception
    {
        ChangeCardStatusRequest request = new ChangeCardStatusRequest(
                "2200006646955942",
                CardStatus.ACTIVE
        );

        when(cardService.changeCardStatus(any(ChangeCardStatusRequest.class))).
                thenThrow(new com.example.bankcards.exception.CardNotFoundException("Card not found"));

        mockMvc.perform(post("/card/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));

        verify(cardService).changeCardStatus(any(ChangeCardStatusRequest.class));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteCard_success() throws Exception
    {
        Long cardId = 1L;

        when(cardService.deleteCard(cardId)).thenReturn(ResponseEntity.ok("Card deleted successfully"));

        mockMvc.perform(delete("/card/{cardId}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Card deleted successfully"));

        verify(cardService).deleteCard(cardId);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteCard_notFound() throws Exception
    {
        Long cardId = 999L;

        when(cardService.deleteCard(cardId))
                .thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(delete("/card/{cardId}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));

        verify(cardService).deleteCard(cardId);
    }


    @Test
    @WithMockUser(authorities = "USER")
    void getCardBalance_success() throws Exception
    {
        Long cardId = 1L;
        Double balance = 1000.0;

        when(cardService.getCardBalance(cardId)).thenReturn(ResponseEntity.ok(balance));

        mockMvc.perform(get("/card/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.0"));

        verify(cardService).getCardBalance(cardId);
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getCardBalance_noAccess() throws Exception
    {
        Long cardId = 1L;

        when(cardService.getCardBalance(cardId))
                .thenThrow(new UnavailableTransferException("You do not own this card"));

        mockMvc.perform(get("/card/{cardId}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not own this card"));

        verify(cardService).getCardBalance(cardId);
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getCardBalance_notFound() throws Exception
    {
        Long cardId = 999L;

        when(cardService.getCardBalance(cardId))
                .thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(get("/card/{cardId}", cardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));

        verify(cardService).getCardBalance(cardId);
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getAllCardsForUser_successOwner() throws Exception
    {
        Long userId = 1L;

        GetCardResponse cardResponse = new GetCardResponse(
                1L,
                "220000******1234",
                "Jonn Doe",
                "11/30",
                1000.0,
                CardStatus.ACTIVE
        );

        CardPageResponse cardPageResponse = new CardPageResponse(
                List.of(cardResponse),
                0,
                10,
                1L,
                1,
                true,
                true
        );

        when(cardService.getAllCardsForUser(userId, 0, 10)).thenReturn(ResponseEntity.ok(cardPageResponse));

        mockMvc.perform(get("/card/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards.length()").value(1));

        verify(cardService).getAllCardsForUser(userId,0, 10);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllCardForUser_successAdmin() throws Exception
    {
        Long userId = 99L;

        GetCardResponse cardResponse = new GetCardResponse(
                1L,
                "220000******1234",
                "Jonn Doe",
                "11/30",
                1000.0,
                CardStatus.ACTIVE
        );

        CardPageResponse cardPageResponse = new CardPageResponse(
                List.of(cardResponse),
                0,
                10,
                1L,
                1,
                true,
                true
        );

        when(cardService.getAllCardsForUser(userId, 0, 10)).thenReturn(ResponseEntity.ok(cardPageResponse));

        mockMvc.perform(get("/card/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].ownerName").value("Jonn Doe"));

        verify(cardService).getAllCardsForUser(userId, 0, 10);
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getAllCardsForUser_forbiddenNotOwner() throws Exception
    {
        Long userId = 1L;

        when(cardService.getAllCardsForUser(userId, 0, 10))
                .thenThrow(new UnavailableTransferException("You can access only your own cards"));

        mockMvc.perform(get("/card/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void getAllCardsForUser_userNotFound() throws Exception
    {
        Long userId = 999L;

        when(cardService.getAllCardsForUser(userId, 0, 10))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/card/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void depositMoney_success() throws Exception
    {
        DepositMoneyRequest moneyRequest = new DepositMoneyRequest(
                "220000******1234",
                1000
        );

        when(cardService.depositMoney(any(DepositMoneyRequest.class)))
                .thenReturn(ResponseEntity.ok("Sum of: " + moneyRequest.getSum() + " successfully deposited"));

        mockMvc.perform(post("/card/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Sum of: 1000.0 successfully deposited"));

        verify(cardService).depositMoney(any(DepositMoneyRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void depositMoney_forbiddenNotOwner() throws Exception
    {
        DepositMoneyRequest moneyRequest = new DepositMoneyRequest(
                "220000******1234",
                1000
        );

        when(cardService.depositMoney(any(DepositMoneyRequest.class)))
                .thenThrow(new UnavailableTransferException("You do not own this card"));

        mockMvc.perform(post("/card/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not own this card"));

        verify(cardService).depositMoney(any(DepositMoneyRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void depositMoney_cardNotFound() throws Exception
    {
        DepositMoneyRequest moneyRequest = new DepositMoneyRequest(
                "220000******1234",
                1000
        );

        when(cardService.depositMoney(any(DepositMoneyRequest.class)))
                .thenThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(post("/card/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found"));

        verify(cardService).depositMoney(any(DepositMoneyRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void transferMoney_success() throws Exception
    {
        TransferMoneyRequest moneyRequest = new TransferMoneyRequest(
                1L,
                2L,
                1000
        );

        when(cardService.transferMoney(any(TransferMoneyRequest.class)))
                .thenReturn(ResponseEntity.ok("Transfer successful"));

        mockMvc.perform(post("/card/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transfer successful"));

        verify(cardService).transferMoney(any(TransferMoneyRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void transferMoney_forbiddenNotOwner() throws Exception
    {
        TransferMoneyRequest moneyRequest = new TransferMoneyRequest(
                1L,
                2L,
                1000
        );

        when(cardService.transferMoney(any(TransferMoneyRequest.class)))
                .thenThrow(new UnavailableTransferException("You can only transfer money to your own cards"));

        mockMvc.perform(post("/card/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You can only transfer money to your own cards"));

        verify(cardService).transferMoney(any(TransferMoneyRequest.class));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void transferMoney_notAcceptableTransfer() throws Exception
    {
        TransferMoneyRequest moneyRequest = new TransferMoneyRequest(
                1L,
                2L,
                -1000
        );

        when(cardService.transferMoney(any(TransferMoneyRequest.class)))
                .thenThrow(new NotAcceptableTransferException("Transfer is not acceptable"));

        mockMvc.perform(post("/card/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moneyRequest)))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.message").value("Transfer is not acceptable"));

        verify(cardService).transferMoney(any(TransferMoneyRequest.class));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllCards_successful() throws Exception
    {
        GetCardResponse getCardResponse = new GetCardResponse(
                1L,
                "220000******1234",
                "John Doe",
                "11/30",
                1000,
                CardStatus.ACTIVE
        );

        CardPageResponse cardPageResponse = new CardPageResponse(
                List.of(getCardResponse),
                0,
                10,
                1L,
                1,
                true,
                true
        );

        when(cardService.getAllCards(0, 10)).thenReturn(ResponseEntity.ok(cardPageResponse));

        mockMvc.perform(get("/card")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].id").value(1L))
                .andExpect(jsonPath("$.cards[0].maskedCardNumber").value("220000******1234"))
                .andExpect(jsonPath("$.cards[0].ownerName").value("John Doe"))
                .andExpect(jsonPath("$.cards[0].expirationDate").value("11/30"))
                .andExpect(jsonPath("$.cards[0].balance").value(1000))
                .andExpect(jsonPath("$.cards[0].status").value("ACTIVE"));

        verify(cardService).getAllCards(0, 10);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void gotAllCards_cardsNotFound() throws Exception
    {
        when(cardService.getAllCards(0, 10)).thenThrow(new CardNotFoundException("Cards not found"));

        mockMvc.perform(get("/card")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cards not found"));

        verify(cardService).getAllCards(0, 10);
    }

}

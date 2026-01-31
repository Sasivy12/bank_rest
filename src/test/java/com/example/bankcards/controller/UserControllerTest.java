package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationFailedException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
class UserControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "userService")
    private UserService userService;

    @MockBean
    private UserAuthService userAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "john@mail.com", authorities = "USER")
    void getUser_success() throws Exception
    {
        Long userId = 1L;

        User user = new User(
                1L,
                "john@mail.com",
                "john123",
                "John Doe",
                Role.USER
        );

        when(userService.getUser(userId)).thenReturn(ResponseEntity.ok(user));

        when(userService.isOwner(eq(userId), any())).thenReturn(true);

        mockMvc.perform(get("/user/{user_id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("john@mail.com"))
                .andExpect(jsonPath("$.password").value("john123"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getUser(userId);
    }

    @Test
    @WithMockUser(username = "john@mail.com", authorities = "USER")
    void getUser_notFound() throws Exception
    {
        Long userId = 1L;

        when(userService.getUser(userId)).thenThrow(new UserNotFoundException("User not found"));

        when(userService.isOwner(eq(userId), any())).thenReturn(true);

        mockMvc.perform(get("/user/{user_id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).getUser(userId);
    }

    @Test
    @WithMockUser(username = "johnadmin@mail.com", authorities = "ADMIN")
    void deleteUser_success() throws Exception
    {
        Long userId = 1L;

        when(userService.deleteUser(userId)).thenReturn(ResponseEntity.ok("User deleted successfully"));

        mockMvc.perform(delete("/user/{user_id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(username = "johnadmin@mail.com", authorities = "ADMIN")
    void deleteUser_notFound() throws Exception
    {
        Long userId = 1L;

        when(userService.deleteUser(userId)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(delete("/user/{user_id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(username = "john@mail.com", authorities = "USER")
    void updateUser_success() throws Exception
    {
        Long userId = 1L;

        User updatedUser = new User(
                1L,
                "john@mail.com",
                "newjohn123",
                "John Doe",
                Role.USER
        );

        when(userService.updateUser(eq(userId), any(User.class)))
                .thenReturn(ResponseEntity.ok("User updated successfully"));

        when(userService.isOwner(eq(userId), any())).thenReturn(true);

        mockMvc.perform(put("/user/{user_id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));

        verify(userService).updateUser(eq(userId), any(User.class));
    }

    @Test
    @WithMockUser(username = "john@mail.com", authorities = "USER")
    void updateUser_notFound() throws Exception
    {
        Long userId = 1L;
        User updatedUser = new User(
                1L,
                "john@mail.com",
                "newjohn123",
                "John Doe",
                Role.USER
        );

        when(userService.updateUser(eq(userId), any(User.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        when(userService.isOwner(eq(userId), any())).thenReturn(true);

        mockMvc.perform(put("/user/{user_id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).updateUser(eq(userId), any(User.class));
    }

    @Test
    void register_success() throws Exception
    {
        RegisterRequest registerRequest = new RegisterRequest(
                "john@mail.com",
                "john123",
                "John Doe",
                Role.USER
        );

        doNothing().when(userAuthService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User john@mail.com registered successfully"));


        verify(userAuthService).register(any(RegisterRequest.class));
    }

    @Test
    void register_userAlreadyExists() throws Exception
    {
        RegisterRequest registerRequest = new RegisterRequest(
                "john@mail.com",
                "john123",
                "John Doe",
                Role.USER
        );

        doThrow(new UserAlreadyExistsException("UserAlreadyExistsException"))
                .when(userAuthService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("UserAlreadyExistsException"));

        verify(userAuthService).register(any(RegisterRequest.class));
    }

    @Test
    void login_successful() throws Exception
    {
        LoginRequest loginRequest = new LoginRequest(
                "john@mail.com",
                "john123"
        );

        String jwtToken = "thisisjwt";

        when(userAuthService.verify(any(LoginRequest.class))).thenReturn(jwtToken);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(jwtToken));

        verify(userAuthService).verify(any(LoginRequest.class));
    }

    @Test
    void login_AuthenticationFailed() throws Exception
    {
        LoginRequest loginRequest = new LoginRequest(
                "john@mail.com",
                "john123"
        );

        when(userAuthService.verify(any(LoginRequest.class)))
                .thenThrow(new AuthenticationFailedException("Authentication failed for user: "
                        + loginRequest.getEmail()));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Authentication failed for user: john@mail.com"));

        verify(userAuthService).verify(any(LoginRequest.class));
    }
}
package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.service.JwtService;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserAuthService userAuthService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtFilter jwtFilter;


    @Test
    void testRegisterSuccess() throws Exception
    {
        RegisterRequest request = new RegisterRequest
                (
                "test@mail.com",
                "123456",
                "User User",
                Role.USER
        );

        doNothing().when(userAuthService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(content().string("User test@mail.com registered successfully"));
    }

    @Test
    void testLoginSuccess() throws Exception
    {
        LoginRequest login = new LoginRequest();
        login.setEmail("test@mail.com");
        login.setPassword("123456");

        when(userAuthService.verify(any(LoginRequest.class))).thenReturn("TOKEN_123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                        .andExpect(status().isOk())
                        .andExpect(content().string("TOKEN_123"));
    }

    @Test
    @WithMockUser(username = "test@mail.com", authorities = "USER")
    void testGetUserAsOwnerSuccess() throws Exception
    {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        when(userService.getUser(1L)).thenReturn(ResponseEntity.ok(user));

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", authorities = "ADMIN")
    void testGetUserAsAdminSuccess() throws Exception
    {
        User user = new User();
        user.setId(2L);
        user.setEmail("user@mail.com");

        when(userService.getUser(2L)).thenReturn(ResponseEntity.ok(user));

        mockMvc.perform(get("/user/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@mail.com"));
    }

    @Test
    @WithMockUser(username = "user1@mail.com", authorities = {"USER"})
    void testGetUserForbiddenForOtherUser() throws Exception
    {
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@mail.com");

        when(userService.getUser(2L)).thenReturn(ResponseEntity.ok(user2));

        mockMvc.perform(get("/user/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@mail.com", authorities = "ADMIN")
    void testDeleteUserAsAdmin_Success() throws Exception
    {
        when(userService.deleteUser(5L)).thenReturn(ResponseEntity.ok("Deleted"));

        mockMvc.perform(delete("/user/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }

    @Test
    @WithMockUser(username = "user@mail.com", authorities = "USER")
    void testDeleteUserForbiddenForUser() throws Exception
    {
        mockMvc.perform(delete("/user/5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@mail.com", authorities = "USER")
    void testUpdateUserAsOwnerSuccess() throws Exception
    {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@mail.com");

        when(userService.getUser(1L)).thenReturn(ResponseEntity.ok(existingUser));

        User updatedUser = new User();
        updatedUser.setEmail("test@mail.com");

        when(userService.updateUser(eq(1L), any(User.class)))
                .thenReturn(ResponseEntity.ok("Updated"));

        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated"));
    }


    @Test
    @WithMockUser(username = "admin@mail.com", authorities = {"ADMIN"})
    void testUpdateUserAsAdminSuccess() throws Exception
    {
        User updateUser = new User();
        updateUser.setEmail("user@mail.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("user@mail.com");

        when(userService.getUser(2L)).thenReturn(ResponseEntity.ok(existingUser));
        when(userService.updateUser(eq(2L), any(User.class)))
                .thenReturn(ResponseEntity.ok("User updated successfully"));

        mockMvc.perform(put("/user/2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));
    }


    @Test
    @WithMockUser(username = "user1@mail.com", authorities = "USER")
    void testUpdateUserForbiddenForOtherUser() throws Exception
    {
        User updated = new User();
        updated.setEmail("other@mail.com");

        when(userService.getUser(2L)).thenReturn(ResponseEntity.ok(updated));

        mockMvc.perform(put("/user/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isForbidden());
    }
}

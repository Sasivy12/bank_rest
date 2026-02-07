package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationFailedException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest
{
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserAuthService authService;

    @Test
    void register_success()
    {
        RegisterRequest request = new RegisterRequest(
                "john@mail.com",
                "john123",
                "John Doe",
                Role.USER
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        authService.register(request);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("john@mail.com", request.getEmail());
        assertEquals("John Doe", request.getFullName());
        assertEquals(Role.USER, request.getRole());

        assertNotEquals("john123", savedUser.getPassword());
        assertTrue(savedUser.getPassword().startsWith("$2"));
    }

    @Test
    void register_userAlreadyExists()
    {
        RegisterRequest request = new RegisterRequest(
                "john@mail.com",
                "john123",
                "John Doe",
                Role.USER
        );

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request));

        assertEquals("User with this email: john@mail.com already exists", exception.getMessage());

        verify(userRepository).existsByEmail(request.getEmail());
    }

    @Test
    void verify_success()
    {
        LoginRequest request = new LoginRequest(
                "john@mail.com",
                "john123"
        );

        Authentication authentication = Mockito.mock(Authentication.class);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(authentication.isAuthenticated()).thenReturn(true);

        when(jwtService.generateToken(request.getEmail())).thenReturn("jwt-token");

        String result = authService.verify(request);

        assertEquals("jwt-token", result);

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(request.getEmail());
    }

    @Test
    void verify_authFailed()
    {
        LoginRequest request = new LoginRequest(
                "john@mail.com",
                "wrongpassword"
        );

        Authentication authentication = Mockito.mock(Authentication.class);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad Credentials"));

        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authService.verify(request));

        assertEquals("Authentication failed for user: john@mail.com", exception.getMessage());

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
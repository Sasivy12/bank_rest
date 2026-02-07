package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest
{
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUser_success()
    {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setEmail("john@mail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userService.getUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john@mail.com", response.getBody().getEmail());

        verify(userRepository).findById(userId);
    }

    @Test
    void getUser_userNotFound()
    {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.getUser(userId));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(userId);
    }

    @Test
    void deleteUser_success()
    {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setEmail("john@mail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = userService.deleteUser(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User deleted successfully", response.getBody());

        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_userNotFound()
    {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(userId));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(userId);
    }

    @Test
    void updateUser_success()
    {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setEmail("newjohn@mail.com");
        updatedUser.setPassword("newjohn123");
        updatedUser.setRole(Role.ADMIN);
        updatedUser.setFullName("John Doe");

        User exisitingUser = new User();
        exisitingUser.setEmail("john@mail.com");
        exisitingUser.setPassword("john123");
        exisitingUser.setRole(Role.USER);
        exisitingUser.setFullName("John Doe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(exisitingUser));

        ResponseEntity<String> response = userService.updateUser(userId, updatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User updated successfully", response.getBody());

        assertEquals("newjohn@mail.com", exisitingUser.getEmail());
        assertEquals("newjohn123", exisitingUser.getPassword());
        assertEquals(Role.ADMIN, exisitingUser.getRole());
        assertEquals("John Doe", exisitingUser.getFullName());

        verify(userRepository).findById(userId);
        verify(userRepository).save(exisitingUser);
    }

    @Test
    void updateUser_userNotFound()
    {
        Long userId = 1L;
        User updatedUser = new User();
        updatedUser.setEmail("newjohn@mail.com");
        updatedUser.setPassword("newjohn123");
        updatedUser.setRole(Role.ADMIN);
        updatedUser.setFullName("John Doe");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(userId, updatedUser));

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(userId);
    }
}
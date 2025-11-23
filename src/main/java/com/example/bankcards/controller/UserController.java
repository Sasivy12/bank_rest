package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@Tag(name = "Управление пользователями",
        description = "Операции для регистрации, авторизации, получения, обновления и удаления пользователей")
public class UserController
{
    private final UserService userService;

    private final UserAuthService userAuthService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Ошибка валидации данных"),
                    @ApiResponse(responseCode = "409", description = "Пользователь с такими email уже существует"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request)
    {
        userAuthService.register(request);

        return ResponseEntity.ok("User " + request.getEmail() + " registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Вход пользователя в систему")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Неверный email или пароль"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public String login(@RequestBody LoginRequest loginRequest)
    {
        return userAuthService.verify(loginRequest);
    }

    @GetMapping("/user/{user_id}")
    @Operation(summary = "Получение пользователя по идентификатору")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<User> getUser(@PathVariable("user_id") Long userId)
    {
        return userService.getUser(userId);
    }

    @Operation(summary = "Удаление пользователя")
    @DeleteMapping("/user/{user_id}")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<String> deleteUser(@PathVariable("user_id") Long userId)
    {
        return userService.deleteUser(userId);
    }

    @Operation(summary = "Обновление пользователя")
    @PutMapping("/user/{user_id}")
    @ApiResponses(value =
            {
                    @ApiResponse(responseCode = "200", description = "Успешная операция"),
                    @ApiResponse(responseCode = "401", description = "Нет доступа"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
            })
    public ResponseEntity<String> updateUser(@PathVariable("user_id") Long userId, @RequestBody User user)
    {
        return userService.updateUser(userId, user);
    }
}

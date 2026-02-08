package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationFailedException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST-контроллер для управления пользователями
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Управление пользователями",
        description = "Операции для регистрации, авторизации, получения, обновления и удаления пользователей")
public class UserController
{
    private final UserService userService;

    private final UserAuthService userAuthService;

    /**
     * Регистрирует нового пользователя в системе.
     * При успешной регистрации пользователь сохраняется в базе данных
     * Если пользователь с таким email уже сущетвует - выбрасывает исключение
     * @param request объект с данными для регистрации пользователя
     * @return Response Entity с сообщением об успешной регистрации пользователя
     * @throws UserNotFoundException если пользователь с таким email уже сущетвует
     */
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

    /**
     * Выполняет аунтефикацию пользователя в системе
     * @param loginRequest объект с email и паролем пользователя
     * @return jwt-token при успешной аунтефикации
     * @throws AuthenticationFailedException если email и пароль некорректны
     */
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

    /**
     * Выполняет получение данных пользователя по идентефикатору
     * @param userId уникальный идентификатор пользователя
     * @return Response Entity с данными пользователя
     * @throws UserNotFoundException если пользователь с таким id не найден
     */
    @GetMapping("/user/{user_id}")
    @PreAuthorize("hasAuthority('ADMIN') or @userService.isOwner(#userId, authentication)")
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

    /**
     * Выполняет удаление пользователя из базы данных
     * @param userId уникальный идентификатор пользователя
     * @return Response Entity с сообщением об успешном удалении пользователя
     * @throws UserNotFoundException если пользователь с таким id не найден
     */
    @Operation(summary = "Удаление пользователя")
    @PreAuthorize("hasAuthority('ADMIN')")
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

    /**
     * Выполняет обновление данных пользователя
     * @param userId уникальный идентификатор пользователя
     * @param user пользователь с обновленными данными
     * @return ResponseEntity с сообщением об успешном обновлении пользователя
     * @throws UserNotFoundException если пользователь с таким id не найден
     */
    @Operation(summary = "Обновление пользователя")
    @PreAuthorize("hasAuthority('ADMIN') or @userService.isOwner(#userId, authentication)")
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

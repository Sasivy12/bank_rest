package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    private final UserAuthService userAuthService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user)
    {
        userAuthService.register(user);

        return ResponseEntity.ok("User " + user.getEmail() + " registered successfully");
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest)
    {
        return userAuthService.verify(loginRequest);
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<User> getUser(@PathVariable("user_id") Long userId)
    {
        return userService.getUser(userId);
    }

    @DeleteMapping("/user/{user_id}")
    public ResponseEntity<String> registerUser(@PathVariable("user_id") Long userId)
    {
        return userService.deleteUser(userId);
    }

    @PutMapping("/user/{user_id}")
    public ResponseEntity<String> updateUser(@PathVariable("user_id") Long userId, @RequestBody User user)
    {
        return userService.updateUser(userId, user);
    }
}

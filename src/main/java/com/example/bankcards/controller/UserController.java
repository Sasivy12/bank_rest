package com.example.bankcards.controller;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    @PostMapping("/user")
    public ResponseEntity<String> registerUser(@RequestBody User user)
    {
        return userService.createUser(user);
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

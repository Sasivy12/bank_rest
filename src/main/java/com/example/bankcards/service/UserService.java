package com.example.bankcards.service;


import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;


    public ResponseEntity<User> getUser(Long userId)
    {
       User user = userRepository.findById(userId).orElseThrow(
               () -> new UserNotFoundException("User not found"));

       return ResponseEntity.ok(user);
    }

    public ResponseEntity<String> deleteUser(Long userId)
    {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new  UserNotFoundException("User not found"));

        userRepository.delete(user);

        return ResponseEntity.ok("User deleted successfully");
    }

    public ResponseEntity<String> updateUser(Long userId, User updatedUser)
    {
        User exisitingUser = userRepository.findById(userId).orElseThrow(
                () -> new  UserNotFoundException("User not found"));

        exisitingUser.setEmail(updatedUser.getEmail());
        exisitingUser.setRole(updatedUser.getRole());
        exisitingUser.setFullName(updatedUser.getFullName());
        exisitingUser.setPassword(exisitingUser.getPassword());

        userRepository.save(exisitingUser);

        return ResponseEntity.ok("User updated successfully");
    }

}

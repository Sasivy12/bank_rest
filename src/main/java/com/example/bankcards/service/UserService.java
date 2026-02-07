package com.example.bankcards.service;


import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Сервис для управления данными пользователей
 */
@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;

    /**
     * Метод для получения данных юзера
     * @param userId идентификатор пользователя
     * @return Response Entity с данными пользователя
     * @throws UserNotFoundException если пользователь с таким id не найден
     */
    public ResponseEntity<User> getUser(Long userId)
    {
       User user = userRepository.findById(userId).orElseThrow(
               () -> new UserNotFoundException("User not found"));

       return ResponseEntity.ok(user);
    }

    /**
     * Метод для удаления пользователя
     * @param userId идентификатор пользователя
     * @return ResponseEntity с сообщением об успешном удалении пользователя
     * @throws UserNotFoundException если пользователь с таким id не найден
     */
    @Transactional
    public ResponseEntity<String> deleteUser(Long userId)
    {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found"));

        userRepository.delete(user);

        return ResponseEntity.ok("User deleted successfully");
    }

    /**
     * Метод для обновления пользователя
     * @param userId идентификатор пользователя
     * @param updatedUser пользователь с обновленными данными
     * @return ResponseEntity с сообщением об успешном обновлении пользователя
     * @throws UserNotFoundException если пользователь с таким id не найден
     */
    @Transactional
    public ResponseEntity<String> updateUser(Long userId, User updatedUser)
    {
        User exisitingUser = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User not found"));

        exisitingUser.setEmail(updatedUser.getEmail());
        exisitingUser.setRole(updatedUser.getRole());
        exisitingUser.setFullName(updatedUser.getFullName());
        exisitingUser.setPassword(updatedUser.getPassword());

        userRepository.save(exisitingUser);

        return ResponseEntity.ok("User updated successfully");
    }


    /**
     * Метод для определения является ли данный пользователь владельцем карты
     * @param userId идентификатор пользователя
     * @param authentication объект аутентификации текущего пользователя
     * @return true если юзер владелец карты, false если юзер не владелец
     */
    public boolean isOwner(Long userId, Authentication authentication)
    {
        return userRepository.findById(userId)
                .map(u -> u.getEmail().equals(authentication.getName()))
                .orElse(false);
    }

}

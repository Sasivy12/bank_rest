package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationFailedException;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервис для регистрации и аутентификации пользователей в системе
 */
@Service
@RequiredArgsConstructor
public class UserAuthService
{
    private final UserRepository userRepository;

    private final AuthenticationManager authManager;

    private final JwtService jwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * Метод для регистрации пользователя в системе
     * @param request объекст с данными для регистрации пользователя
     * @throws UserAlreadyExistsException если пользователь с таким email уже зарегистрирован
     */
    @Transactional
    public void register(RegisterRequest request)
    {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setFullName(request.getFullName());

        if (request.getRole() == null)
        {
            user.setRole(Role.USER);
        }
        else
        {
            user.setRole(request.getRole());
        }

        if(userRepository.existsByEmail(user.getEmail()))
        {
            throw new UserAlreadyExistsException("User with this email: " + user.getEmail() + " already exists");
        }
        else
        {
            user.setPassword(encoder.encode(user.getPassword()));
            userRepository.save(user);
        }
    }

    /**
     * Метод для верификации пользователя
     * @param loginRequest объект с данными для верификации пользователя в системе
     * @return Возвращает строку с jwt-токеном при успешной аунтефикации
     * @throws AuthenticationFailedException если аунтефикация оказалась неуспешной
     */
    @Transactional
    public String verify(LoginRequest loginRequest)
    {
        try
        {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            if (authentication.isAuthenticated())
            {
                return jwtService.generateToken(loginRequest.getEmail());
            }
        }
        catch (Exception e)
        {
            throw new AuthenticationFailedException("Authentication failed for user: " + loginRequest.getEmail());
        }
        return "FAILED";
    }
}

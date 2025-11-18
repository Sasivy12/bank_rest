package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserDetailsImpl;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService
{
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User with email: " + email + " not found"));

        if(user == null)
        {
            throw new UsernameNotFoundException("User not found");
        }
        else
        {
            return new UserDetailsImpl(user);
        }
    }
}

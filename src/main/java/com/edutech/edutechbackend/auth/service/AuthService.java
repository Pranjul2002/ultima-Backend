package com.edutech.edutechbackend.auth.service;

import com.edutech.edutechbackend.auth.dto.UserLoginRequest;
import com.edutech.edutechbackend.auth.dto.UserRegisterRequest;
import com.edutech.edutechbackend.security.JwtService;
import com.edutech.edutechbackend.user.entity.User;
import com.edutech.edutechbackend.user.enums.Role;
import com.edutech.edutechbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(UserRegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        String username = request.getUsername().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase().trim());
        } catch (Exception e) {
            throw new RuntimeException("Invalid role. Use STUDENT or MENTOR");
        }

        User user = User.builder()
                .email(email)
                .username(username)
                .dateOfBirth(request.getDateOfBirth())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
    }

    public String login(UserLoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtService.generateToken(user.getEmail());
    }
}
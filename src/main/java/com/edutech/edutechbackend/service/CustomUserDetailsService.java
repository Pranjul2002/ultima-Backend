package com.edutech.edutechbackend.service;

import com.edutech.edutechbackend.entity.Student;
import com.edutech.edutechbackend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Student not found with email: " + email
                        )
                );

        // Now returns actual role (ROLE_STUDENT or ROLE_ADMIN)
        // instead of empty authorities list
        return new User(
                student.getEmail(),
                student.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + student.getRole().name()))
        );
    }
}
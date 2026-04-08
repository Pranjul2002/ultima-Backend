package com.edutech.edutechbackend.user.entity;

import com.edutech.edutechbackend.subject.entity.Subject;
import com.edutech.edutechbackend.user.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"subjects", "settings", "password"})
@EqualsAndHashCode(exclude = {"subjects", "settings"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String username;

    private LocalDate dateOfBirth;

    @Column(length = 1000)
    private String bio;

    private String location;

    private String website;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @JsonIgnore
    @ManyToMany(mappedBy = "mentors")
    private Set<Subject> subjects;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserSettings settings;
}
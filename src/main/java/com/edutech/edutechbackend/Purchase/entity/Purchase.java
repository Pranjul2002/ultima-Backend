package com.edutech.edutechbackend.Purchase.entity;


import com.edutech.edutechbackend.test.entity.Test;
import com.edutech.edutechbackend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Student
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    // 🔗 Test
    @ManyToOne
    @JoinColumn(name = "test_id")
    private Test test;

    private LocalDateTime purchasedAt;
}

package com.edutech.edutechbackend.Purchase.repository;

import com.edutech.edutechbackend.Purchase.entity.Purchase;
import com.edutech.edutechbackend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByStudent(User student);
}

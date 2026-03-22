package com.edutech.edutechbackend.repository;

import com.edutech.edutechbackend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    // ↑ JpaRepository<Subject, Long> means:
    //   → this repo manages "Subject" entity
    //   → the ID type is "Long"
    //   Spring automatically gives us these methods for FREE:
    //   → save(subject)        → INSERT or UPDATE
    //   → findById(id)         → SELECT WHERE id = ?
    //   → findAll()            → SELECT * FROM subjects
    //   → deleteById(id)       → DELETE WHERE id = ?
    //   → existsById(id)       → SELECT COUNT WHERE id = ?

    Optional<Subject> findByName(String name);
    // ↑ we write this custom method
    //   Spring reads the method name and builds this SQL:
    //   SELECT * FROM subjects WHERE name = ?
    //
    //   Used when admin tries to create "Math" subject
    //   we first check: does "Math" already exist?
    //   if yes → don't create duplicate
    //   if no  → create it

    boolean existsByName(String name);
    // ↑ Spring builds: SELECT COUNT(*) FROM subjects WHERE name = ?
    //   returns true if subject with this name exists
    //   returns false if it doesn't
    //
    //   faster than findByName() when we just need yes/no
    //   don't need the full object — just existence check
}
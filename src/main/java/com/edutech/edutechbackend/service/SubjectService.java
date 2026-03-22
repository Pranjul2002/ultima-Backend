package com.edutech.edutechbackend.service;

import com.edutech.edutechbackend.dto.CreateSubjectRequestDTO;
import com.edutech.edutechbackend.dto.SubjectResponseDTO;
import com.edutech.edutechbackend.entity.Subject;
import com.edutech.edutechbackend.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    // ════════════════════════════════════════════════
    // CREATE SUBJECT (Admin only)
    // ════════════════════════════════════════════════
    public SubjectResponseDTO createSubject(CreateSubjectRequestDTO request) {

        // ── STEP 1: Check duplicate ───────────────────
        if (subjectRepository.existsByName(request.getName())) {
            throw new RuntimeException("Subject already exists: " + request.getName());
            // ↑ can't have two subjects both named "Math"
            //   we'll later replace RuntimeException with
            //   a custom exception like SubjectAlreadyExistsException
        }

        // ── STEP 2: Build and save ────────────────────
        Subject subject = Subject.builder()
                .name(request.getName())
                .build();

        Subject saved = subjectRepository.save(subject);
        // ↑ INSERT INTO subjects (name) VALUES (?)
        //   returns saved subject with generated id

        // ── STEP 3: Build response ────────────────────
        return SubjectResponseDTO.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }

    // ════════════════════════════════════════════════
    // GET ALL SUBJECTS (Student + Admin)
    // ════════════════════════════════════════════════
    public List<SubjectResponseDTO> getAllSubjects() {

        return subjectRepository.findAll()
                // ↑ SELECT * FROM subjects
                //   returns List<Subject>

                .stream()
                // ↑ converts List to a Stream
                //   allows us to process each item

                .map(subject -> SubjectResponseDTO.builder()
                        .id(subject.getId())
                        .name(subject.getName())
                        .build())
                // ↑ converts each Subject entity
                //   into a SubjectResponseDTO
                //   we never send raw entities to client

                .collect(Collectors.toList());
        // ↑ collects stream back into a List
        //   returns List<SubjectResponseDTO>
    }
}
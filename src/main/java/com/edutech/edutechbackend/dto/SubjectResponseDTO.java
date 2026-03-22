package com.edutech.edutechbackend.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@Builder
public class SubjectResponseDTO {

    private Long id;
    // ↑ client needs this to make future requests
    //   e.g. GET /api/tests/papers?subjectId=1
    //   student uses this id to browse tests under a subject

    private String name;
    // ↑ "Math", "Science" etc
    //   displayed in the UI subject list
}
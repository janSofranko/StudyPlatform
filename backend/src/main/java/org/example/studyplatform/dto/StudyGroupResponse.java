package org.example.studyplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudyGroupResponse {
    private Long id;
    private String name;
    private String description;
    private Long createdById;
}

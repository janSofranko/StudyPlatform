package org.example.studyplatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateResourceRequest {
    private String title;
    private String url;
    private Long groupId;
    private Long uploadedById;
    private Long taskId;
}

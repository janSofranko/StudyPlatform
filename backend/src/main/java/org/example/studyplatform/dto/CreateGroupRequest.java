package org.example.studyplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGroupRequest {

    @NotBlank(message = "Name is required")
    public String name;

    public String description;

    @NotNull(message = "creatorId is required")
    public Long creatorId;
}

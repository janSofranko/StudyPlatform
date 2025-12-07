package org.example.studyplatform.websocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDto {
    private String fromUser;
    private String content;
}

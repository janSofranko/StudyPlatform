package org.example.studyplatform.websocket.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyplatform.websocket.dto.ChatMessageDto;
import org.example.studyplatform.websocket.entity.ChatMessage;
import org.example.studyplatform.websocket.service.ChatMessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;

    // REST – odoslanie správy
    @PostMapping("/send")
    public ChatMessage sendViaRest(@RequestBody ChatMessageDto dto) {
        ChatMessage message = new ChatMessage();
        message.setFromUser(dto.getFromUser());
        message.setContent(dto.getContent());

        return chatMessageService.save(message);
    }

    // REST – načítanie histórie
    @GetMapping("/history")
    public List<ChatMessage> getHistory() {
        return chatMessageService.getAllMessages();
    }
}

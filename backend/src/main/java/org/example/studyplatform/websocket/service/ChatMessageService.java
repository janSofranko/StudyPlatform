package org.example.studyplatform.websocket.service;

import lombok.RequiredArgsConstructor;
import org.example.studyplatform.websocket.entity.ChatMessage;
import org.example.studyplatform.websocket.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository repository;

    public ChatMessage save(ChatMessage message) {
        return repository.save(message);
    }

    public List<ChatMessage> getAllMessages() {
        return repository.findAll();
    }
}

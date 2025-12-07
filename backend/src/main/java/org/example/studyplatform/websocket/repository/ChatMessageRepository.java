package org.example.studyplatform.websocket.repository;

import org.example.studyplatform.websocket.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}

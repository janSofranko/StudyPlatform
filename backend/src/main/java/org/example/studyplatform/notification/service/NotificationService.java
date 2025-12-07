package org.example.studyplatform.notification.service;

import lombok.RequiredArgsConstructor;
import org.example.studyplatform.notification.entity.Notification;
import org.example.studyplatform.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;


    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        try {
            emitter.send(SseEmitter.event().name("init").data("connected"));
        } catch (IOException ignored) {}

        return emitter;
    }

    public Notification createAndSend(Long userId, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setMessage(message);
        n.setUnread(true);

        Notification saved = notificationRepository.save(n);
        pushToClient(userId, saved);
        return saved;
    }

    private void pushToClient(Long userId, Notification notification) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification.getMessage()));
        } catch (IOException e) {
            emitters.remove(userId);
        }
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndUnreadTrue(userId);
    }

    // 🔔 teraz sa správy vymažú z DB
    public void markAllRead(Long userId) {
        var list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(list);
    }

    public List<Notification> list(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

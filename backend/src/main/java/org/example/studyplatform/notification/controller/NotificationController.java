package org.example.studyplatform.notification.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyplatform.notification.entity.Notification;
import org.example.studyplatform.notification.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping(path = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable("userId") Long userId) {
        return notificationService.subscribe(userId);
    }

    @GetMapping("/{userId}")
    public List<Notification> list(@PathVariable("userId") Long userId) {
        return notificationService.list(userId);
    }

    @GetMapping("/{userId}/unread-count")
    public long unreadCount(@PathVariable("userId") Long userId) {
        return notificationService.unreadCount(userId);
    }

    @PostMapping("/{userId}/mark-read")
    public void markRead(@PathVariable("userId") Long userId) {
        notificationService.markAllRead(userId);
    }
}

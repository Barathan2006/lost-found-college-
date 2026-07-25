package com.lostfound.controller;

import com.lostfound.model.Notification;
import com.lostfound.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public List<Notification> getNotifications(@RequestParam(required = false) UUID userId) {
        if (userId != null) {
            return notificationRepository.findByUserId(userId);
        }
        return notificationRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notification> markAsRead(@PathVariable UUID id) {
        return notificationRepository.findById(id).map(notif -> {
            notif.setIsRead(true);
            Notification updated = notificationRepository.save(notif);
            return ResponseEntity.ok(updated);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

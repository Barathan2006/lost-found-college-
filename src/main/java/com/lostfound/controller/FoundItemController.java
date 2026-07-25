package com.lostfound.controller;

import com.lostfound.model.FoundItem;
import com.lostfound.model.LostItem;
import com.lostfound.model.Match;
import com.lostfound.model.Notification;
import com.lostfound.repository.FoundItemRepository;
import com.lostfound.repository.LostItemRepository;
import com.lostfound.repository.MatchRepository;
import com.lostfound.repository.NotificationRepository;
import com.lostfound.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/found")
public class FoundItemController {

    @Autowired
    private FoundItemRepository foundItemRepository;

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MatchingService matchingService;

    @GetMapping
    public List<FoundItem> getAllFoundItems() {
        return foundItemRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<FoundItem> createFoundItem(@RequestBody FoundItem foundItem) {
        if (foundItem.getStatus() == null) {
            foundItem.setStatus("open");
        }
        FoundItem savedItem = foundItemRepository.save(foundItem);

        // Trigger match scan
        List<LostItem> openLostItems = lostItemRepository.findByStatus("open");
        for (LostItem lostItem : openLostItems) {
            int score = matchingService.calculateScore(lostItem, savedItem);
            if (score >= 60) {
                Match match = new Match();
                match.setLostItem(lostItem);
                match.setFoundItem(savedItem);
                match.setScore((float) score);
                match.setStatus("pending");
                Match savedMatch = matchRepository.save(match);

                // Create notification for the user who lost the item
                if (lostItem.getUser() != null) {
                    Notification notifLost = new Notification();
                    notifLost.setUser(lostItem.getUser());
                    notifLost.setMatch(savedMatch);
                    notifLost.setIsRead(false);
                    notifLost.setCreatedAt(new Date());
                    notificationRepository.save(notifLost);
                }

                // Create notification for the user who found the item
                if (savedItem.getUser() != null) {
                    Notification notifFound = new Notification();
                    notifFound.setUser(savedItem.getUser());
                    notifFound.setMatch(savedMatch);
                    notifFound.setIsRead(false);
                    notifFound.setCreatedAt(new Date());
                    notificationRepository.save(notifFound);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoundItem(@PathVariable UUID id) {
        if (foundItemRepository.existsById(id)) {
            foundItemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

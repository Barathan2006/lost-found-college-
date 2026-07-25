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
@RequestMapping("/lost")
public class LostItemController {

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private FoundItemRepository foundItemRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MatchingService matchingService;

    @GetMapping
    public List<LostItem> getAllLostItems() {
        return lostItemRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<LostItem> createLostItem(@RequestBody LostItem lostItem) {
        if (lostItem.getStatus() == null) {
            lostItem.setStatus("open");
        }
        LostItem savedItem = lostItemRepository.save(lostItem);

        // Trigger match scan
        List<FoundItem> openFoundItems = foundItemRepository.findByStatus("open");
        for (FoundItem foundItem : openFoundItems) {
            int score = matchingService.calculateScore(savedItem, foundItem);
            if (score >= 60) {
                Match match = new Match();
                match.setLostItem(savedItem);
                match.setFoundItem(foundItem);
                match.setScore((float) score);
                match.setStatus("pending");
                Match savedMatch = matchRepository.save(match);

                // Create notification for the user who lost the item
                if (savedItem.getUser() != null) {
                    Notification notifLost = new Notification();
                    notifLost.setUser(savedItem.getUser());
                    notifLost.setMatch(savedMatch);
                    notifLost.setIsRead(false);
                    notifLost.setCreatedAt(new Date());
                    notificationRepository.save(notifLost);
                }

                // Create notification for the user who found the item
                if (foundItem.getUser() != null) {
                    Notification notifFound = new Notification();
                    notifFound.setUser(foundItem.getUser());
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
    public ResponseEntity<Void> deleteLostItem(@PathVariable UUID id) {
        if (lostItemRepository.existsById(id)) {
            lostItemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

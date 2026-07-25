package com.lostfound.controller;

import com.lostfound.model.FoundItem;
import com.lostfound.model.LostItem;
import com.lostfound.repository.FoundItemRepository;
import com.lostfound.repository.LostItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/claim")
public class ClaimController {

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private FoundItemRepository foundItemRepository;

    @PutMapping("/{id}")
    public ResponseEntity<Void> claimItem(@PathVariable UUID id) {
        if (lostItemRepository.existsById(id)) {
            LostItem lostItem = lostItemRepository.findById(id).get();
            lostItem.setStatus("claimed");
            lostItemRepository.save(lostItem);
            return ResponseEntity.ok().build();
        }

        if (foundItemRepository.existsById(id)) {
            FoundItem foundItem = foundItemRepository.findById(id).get();
            foundItem.setStatus("claimed");
            foundItemRepository.save(foundItem);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }
}

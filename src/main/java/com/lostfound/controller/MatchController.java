package com.lostfound.controller;

import com.lostfound.model.Match;
import com.lostfound.repository.FoundItemRepository;
import com.lostfound.repository.LostItemRepository;
import com.lostfound.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/matches")
public class MatchController {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private FoundItemRepository foundItemRepository;

    @GetMapping
    public List<Match> getMatches(@RequestParam(required = false) UUID userId) {
        if (userId != null) {
            return matchRepository.findByLostItemUserIdOrFoundItemUserId(userId, userId);
        }
        return matchRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Match> getMatchById(@PathVariable UUID id) {
        Optional<Match> match = matchRepository.findById(id);
        return match.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Match> confirmMatch(@PathVariable UUID id) {
        return matchRepository.findById(id).map(match -> {
            match.setStatus("confirmed");
            
            if (match.getLostItem() != null) {
                match.getLostItem().setStatus("matched");
                lostItemRepository.save(match.getLostItem());
            }
            if (match.getFoundItem() != null) {
                match.getFoundItem().setStatus("matched");
                foundItemRepository.save(match.getFoundItem());
            }
            
            Match updatedMatch = matchRepository.save(match);
            return ResponseEntity.ok(updatedMatch);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

package com.lostfound.service;

import com.lostfound.model.FoundItem;
import com.lostfound.model.LostItem;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MatchingService {

    // Simple synonym map for colors
    private static final Map<String, List<String>> COLOR_SYNONYMS = Map.of(
        "red", Arrays.asList("red", "crimson", "burgundy", "maroon"),
        "blue", Arrays.asList("blue", "navy", "cyan", "teal", "azure"),
        "black", Arrays.asList("black", "charcoal", "ebony"),
        "white", Arrays.asList("white", "ivory", "cream")
    );

    public int calculateScore(LostItem lostItem, FoundItem foundItem) {
        int score = 0;

        // 1. Category match (exact) - 40 points
        if (lostItem.getCategory() != null && foundItem.getCategory() != null) {
            if (lostItem.getCategory().equalsIgnoreCase(foundItem.getCategory())) {
                score += 40;
            }
        }

        // 2. Color match (exact or synonym) - 20 points
        if (lostItem.getColor() != null && foundItem.getColor() != null) {
            if (isColorMatch(lostItem.getColor(), foundItem.getColor())) {
                score += 20;
            }
        }

        // 3. Location match (same area) - 20 points
        if (lostItem.getLocation() != null && foundItem.getLocation() != null) {
            // Simple string inclusion for area matching
            String lostLoc = lostItem.getLocation().toLowerCase();
            String foundLoc = foundItem.getLocation().toLowerCase();
            if (lostLoc.contains(foundLoc) || foundLoc.contains(lostLoc)) {
                score += 20;
            }
        }

        // 4. Date proximity (found date within 14 days of lost date) - 20 points
        if (lostItem.getDateLost() != null && foundItem.getDateFound() != null) {
            long diffInMillies = foundItem.getDateFound().getTime() - lostItem.getDateLost().getTime();
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            
            // Found date should typically be AFTER or SAME DAY as lost date, 
            // but we can allow a small buffer (e.g., -3 days) if the user isn't sure when they lost it.
            // And found date within 14 days of lost date.
            if (diffInDays >= -3 && diffInDays <= 14) {
                score += 20;
            }
        }

        return score;
    }

    private boolean isColorMatch(String color1, String color2) {
        if (color1.equalsIgnoreCase(color2)) {
            return true;
        }
        
        String c1 = color1.toLowerCase();
        String c2 = color2.toLowerCase();
        
        for (List<String> synonyms : COLOR_SYNONYMS.values()) {
            if (synonyms.contains(c1) && synonyms.contains(c2)) {
                return true;
            }
        }
        
        return false;
    }
}

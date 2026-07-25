package com.lostfound.service;

import com.lostfound.model.FoundItem;
import com.lostfound.model.LostItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatchingServiceTest {

    private MatchingService matchingService;

    @BeforeEach
    public void setUp() {
        matchingService = new MatchingService();
    }

    private Date getDate(int daysOffset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysOffset);
        return cal.getTime();
    }

    @Test
    public void testExactMatch_FullScore() {
        LostItem lost = new LostItem();
        lost.setCategory("Wallet");
        lost.setColor("Black");
        lost.setLocation("Library");
        lost.setDateLost(getDate(0));

        FoundItem found = new FoundItem();
        found.setCategory("Wallet");
        found.setColor("Black");
        found.setLocation("Library");
        found.setDateFound(getDate(1)); // Found 1 day later

        int score = matchingService.calculateScore(lost, found);
        assertEquals(100, score, "Exact match should yield 100 points");
    }

    @Test
    public void testCategoryAndColorMatch_Score60() {
        LostItem lost = new LostItem();
        lost.setCategory("Phone");
        lost.setColor("Red");
        lost.setLocation("Library");
        lost.setDateLost(getDate(0));

        FoundItem found = new FoundItem();
        found.setCategory("Phone");
        found.setColor("Crimson"); // Synonym match
        found.setLocation("Cafeteria"); // Different location
        found.setDateFound(getDate(30)); // 30 days later (out of threshold)

        int score = matchingService.calculateScore(lost, found);
        assertEquals(60, score, "Category (40) + Color Synonym (20) should yield 60 points");
    }

    @Test
    public void testNoMatch_Score0() {
        LostItem lost = new LostItem();
        lost.setCategory("Laptop");
        lost.setColor("Silver");
        lost.setLocation("Science Building");
        lost.setDateLost(getDate(0));

        FoundItem found = new FoundItem();
        found.setCategory("Jacket");
        found.setColor("Blue");
        found.setLocation("Gym");
        found.setDateFound(getDate(20));

        int score = matchingService.calculateScore(lost, found);
        assertEquals(0, score, "No matching attributes should yield 0 points");
    }

    @Test
    public void testDateProximity_EdgeCases() {
        LostItem lost = new LostItem();
        lost.setDateLost(getDate(0));

        FoundItem foundBefore = new FoundItem();
        foundBefore.setDateFound(getDate(-4)); // Found 4 days before (out of bounds)

        FoundItem foundExactBound = new FoundItem();
        foundExactBound.setDateFound(getDate(-3)); // Found 3 days before (in bounds)

        FoundItem foundAfter = new FoundItem();
        foundAfter.setDateFound(getDate(15)); // Found 15 days later (out of bounds)
        
        FoundItem foundInBoundAfter = new FoundItem();
        foundInBoundAfter.setDateFound(getDate(14)); // Found 14 days later (in bounds)

        assertEquals(0, matchingService.calculateScore(lost, foundBefore));
        assertEquals(20, matchingService.calculateScore(lost, foundExactBound));
        assertEquals(0, matchingService.calculateScore(lost, foundAfter));
        assertEquals(20, matchingService.calculateScore(lost, foundInBoundAfter));
    }
    
    @Test
    public void testLocationMatch_PartialString() {
        LostItem lost = new LostItem();
        lost.setLocation("Main Library 3rd Floor");

        FoundItem found = new FoundItem();
        found.setLocation("Library");

        int score = matchingService.calculateScore(lost, found);
        assertEquals(20, score, "Partial string match in location should yield 20 points");
    }
}

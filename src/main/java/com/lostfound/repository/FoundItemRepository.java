package com.lostfound.repository;

import com.lostfound.model.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItem, UUID> {
    List<FoundItem> findByStatus(String status);
}

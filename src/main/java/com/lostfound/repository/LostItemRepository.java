package com.lostfound.repository;

import com.lostfound.model.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LostItemRepository extends JpaRepository<LostItem, UUID> {
    List<LostItem> findByStatus(String status);
}

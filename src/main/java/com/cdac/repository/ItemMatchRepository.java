package com.cdac.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.entity.Item;
import com.cdac.entity.ItemMatch;
import com.cdac.enums.MatchStatus;

@Repository
public interface ItemMatchRepository extends JpaRepository<ItemMatch, Long> {

    List<ItemMatch> findByLostItemOrderByMatchScoreDesc(Item lostItem);

    List<ItemMatch> findByFoundItemOrderByMatchScoreDesc(Item foundItem);

    List<ItemMatch> findByLostItemAndMatchStatusOrderByMatchScoreDesc(Item lostItem, MatchStatus matchStatus);

    List<ItemMatch> findByFoundItemAndMatchStatusOrderByMatchScoreDesc(Item foundItem, MatchStatus matchStatus);

    List<ItemMatch> findByMatchStatusOrderByUpdatedAtDesc(MatchStatus matchStatus);

    boolean existsByLostItemAndFoundItem(Item lostItem, Item foundItem);

    Optional<ItemMatch> findByLostItemAndFoundItem(Item lostItem, Item foundItem);
    
    List<ItemMatch> findByLostItemOrFoundItemOrderByMatchScoreDesc(Item lostItem, Item foundItem);
    
    boolean existsByLostItemAndMatchStatus(Item lostItem, MatchStatus matchStatus);

    boolean existsByFoundItemAndMatchStatus(Item foundItem, MatchStatus matchStatus);
    
    Optional<ItemMatch> findByLostItemAndFoundItemAndMatchStatus(Item lostItem, Item foundItem,
            MatchStatus matchStatus);
}
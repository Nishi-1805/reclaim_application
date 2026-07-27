package com.cdac.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cdac.entity.Claim;
import com.cdac.entity.Item;
import com.cdac.entity.ItemMatch;
import com.cdac.entity.OwnershipResponse;
import com.cdac.entity.User;
import com.cdac.enums.ClaimStatus;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByClaimedByUserOrderByCreatedAtDesc(User claimedByUser);

    List<Claim> findByClaimedByUserAndStatusOrderByCreatedAtDesc(User claimedByUser, ClaimStatus claimStatus);

    List<Claim> findByItemMatchOrderByCreatedAtDesc(ItemMatch itemMatch);
    Optional<Claim> findByItemMatch(ItemMatch itemMatch);

    //List<Claim> findByItemMatchAndStatusOrderByCreatedAtDesc(ItemMatch itemMatch, ClaimStatus claimStatus);

    boolean existsByClaimedByUserAndItemMatch(User claimedByUser, ItemMatch itemMatch);

    Optional<Claim> findByClaimedByUserAndItemMatch(User claimedByUser, ItemMatch itemMatch);

    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus claimStatus);
    
    List<Claim> findByItemMatch_LostItemOrItemMatch_FoundItemOrderByCreatedAtDesc(
            Item lostItem,
            Item foundItem);
    
    long countByClaimedByUserAndStatus(User claimedByUser, ClaimStatus status);

    long countByClaimedByUserAndStatusIn(User claimedByUser, List<ClaimStatus> statuses);
    

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Claim c
            WHERE
            (
                c.itemMatch.lostItem = :item
                OR
                c.itemMatch.foundItem = :item
            )
            AND c.status IN :statuses
            """)
    
    boolean existsActiveClaimsForItem(@Param("item") Item item, @Param("statuses") List<ClaimStatus> statuses);
}
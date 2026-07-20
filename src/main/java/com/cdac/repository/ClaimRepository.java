package com.cdac.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.entity.Claim;
import com.cdac.entity.ItemMatch;
import com.cdac.entity.User;
import com.cdac.enums.ClaimStatus;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByClaimedByUserOrderByCreatedAtDesc(User claimedByUser);

    List<Claim> findByClaimedByUserAndStatusOrderByCreatedAtDesc(User claimedByUser, ClaimStatus claimStatus);

    List<Claim> findByItemMatchOrderByCreatedAtDesc(ItemMatch itemMatch);

    List<Claim> findByItemMatchAndStatusOrderByCreatedAtDesc(ItemMatch itemMatch, ClaimStatus claimStatus);

    boolean existsByClaimedByUserAndItemMatch(User claimedByUser, ItemMatch itemMatch);

    Optional<Claim> findByClaimedByUserAndItemMatch(User claimedByUser, ItemMatch itemMatch);

    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus claimStatus);
}
package com.cdac.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.entity.Claim;
import com.cdac.entity.OwnershipQuestion;
import com.cdac.entity.OwnershipResponse;

@Repository
public interface OwnershipResponseRepository extends JpaRepository<OwnershipResponse, Long> {

    List<OwnershipResponse> findByClaimOrderByOwnershipQuestion_DisplayOrderAsc(Claim claim);

    boolean existsByClaimAndOwnershipQuestion(Claim claim, OwnershipQuestion ownershipQuestion);

    Optional<OwnershipResponse> findByClaimAndOwnershipQuestion(Claim claim, OwnershipQuestion ownershipQuestion);
}
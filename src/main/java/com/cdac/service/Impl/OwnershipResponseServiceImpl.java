package com.cdac.service.Impl;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.dto.response.OwnershipAnswerResponse;
import com.cdac.entity.Claim;
import com.cdac.entity.OwnershipResponse;
import com.cdac.entity.User;
import com.cdac.enums.UserRole;
import com.cdac.exception.ForbiddenException;
import com.cdac.exception.ResourceNotFoundException;
import com.cdac.repository.ClaimRepository;
import com.cdac.repository.OwnershipResponseRepository;
import com.cdac.repository.UserRepository;
import com.cdac.service.OwnershipResponseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OwnershipResponseServiceImpl implements OwnershipResponseService {

    private final OwnershipResponseRepository ownershipResponseRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OwnershipAnswerResponse> getResponsesByClaim(Long claimId) {

    	Claim claim = claimRepository.findById(claimId)
    	        .orElseThrow(() ->
    	                new ResourceNotFoundException(
    	                        "Claim not found with id : " + claimId));
    	
    	log.info("Ownership responses requested for claim id={}", claimId);

    	User currentUser = getCurrentUser();
    	
    	log.info("User {} is accessing ownership responses for claim {}",
    	        currentUser.getEmail(),
    	        claimId);

    	User owner = claim.getItemMatch()
    	                  .getFoundItem()
    	                  .getReportedBy();

    	if (!owner.getId().equals(currentUser.getId())
    	        && currentUser.getRole() != UserRole.ADMIN) {

    	    throw new ForbiddenException(
    	            "You are not authorized to view these ownership responses.");
    	}

    	return ownershipResponseRepository
    	        .findByClaimOrderByOwnershipQuestion_DisplayOrderAsc(claim)
    	        .stream()
    	        .map(this::convertToResponse)
    	        .toList();
    }

    //-------------------------------
    //      HELPER METHODS
    // -------------------------------
    private OwnershipAnswerResponse convertToResponse(OwnershipResponse response) {
        return OwnershipAnswerResponse.builder()
                .ownershipQuestionId(response.getOwnershipQuestion().getId())
                .questionText(response.getOwnershipQuestion().getQuestionText())
                .responseText(response.getResponseText())
                .build();
    }
    
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found."));
    }
}
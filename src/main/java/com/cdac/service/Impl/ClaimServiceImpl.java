package com.cdac.service.Impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import com.cdac.dto.request.OwnershipAnswerRequest;
import com.cdac.dto.request.SubmitClaimRequest;
import com.cdac.dto.response.ClaimResponse;
import com.cdac.dto.response.ClaimSummaryResponse;
import com.cdac.entity.Claim;
import com.cdac.entity.Item;
import com.cdac.entity.ItemMatch;
import com.cdac.entity.OwnershipQuestion;
import com.cdac.entity.OwnershipResponse;
import com.cdac.entity.User;
import com.cdac.enums.ClaimStatus;
import com.cdac.enums.ItemStatus;
import com.cdac.enums.NotificationType;
import com.cdac.exception.ForbiddenException;
import com.cdac.exception.InvalidRequestException;
import com.cdac.exception.ResourceNotFoundException;
import com.cdac.repository.ClaimRepository;
import com.cdac.repository.ItemMatchRepository;
import com.cdac.repository.ItemRepository;
import com.cdac.repository.OwnershipQuestionRepository;
import com.cdac.repository.OwnershipResponseRepository;
import com.cdac.repository.UserRepository;
import com.cdac.service.ClaimService;
import com.cdac.service.NotificationService;
import com.cdac.dto.response.OwnershipAnswerResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaimServiceImpl implements ClaimService {
	
	private final ItemMatchRepository itemMatchRepository;
	private final ClaimRepository claimRepository;
	private final UserRepository userRepository;
	private final OwnershipQuestionRepository ownershipQuestionRepository;
	private final OwnershipResponseRepository ownershipResponseRepository;
	private final ItemRepository itemRepository;
	private final NotificationService notificationService;
	
	@Override
	@Transactional
	public ClaimResponse submitClaim(SubmitClaimRequest request) {

	    // Get logged-in user
		User currentUser = getCurrentUser();

	    // Fetch Item Match
	    ItemMatch itemMatch = getItemMatchByIdOrThrow(
	            request.getItemMatchId());

	    // Validate business rules
	    validateClaimEligibility(currentUser, itemMatch);

	    // Validate ownership responses
	    validateOwnershipResponses(itemMatch, request);

	    // Create Claim
	    Claim claim = Claim.builder()
	            .claimedByUser(currentUser)
	            .itemMatch(itemMatch)
	            .status(ClaimStatus.PENDING)
	            .matchScoreAtClaimTime(
	                    calculateMatchScoreSnapshot(itemMatch))
	            .build();

	    // Save Claim
	    Claim savedClaim = claimRepository.save(claim);

	    // Save Ownership Responses
	    createOwnershipResponses(savedClaim, request);

	    double verificationScore = verifyOwnershipAnswers(savedClaim);

	    if (verificationScore >= 80.0) {

	        approveClaim(savedClaim, verificationScore);

	    } else {

	        rejectClaim(savedClaim, verificationScore);
	    }
	    
	 // Notify found item owner that a new claim has been submitted
	    notificationService.createNotification(
	            itemMatch.getFoundItem().getReportedBy(),
	            NotificationType.CLAIM_SUBMITTED,
	            "A new claim has been submitted for your found item: "
	                    + itemMatch.getFoundItem().getTitle(),
	            itemMatch.getFoundItem(),
	            itemMatch,
	            savedClaim
	    );

	    // Return Response
	    return convertToClaimResponse(savedClaim);
	}
	
	@Override
	@Transactional(readOnly = true)
	public ClaimResponse getClaimById(Long claimId) {

	    Claim claim = getClaimByIdOrThrow(claimId);

	    return convertToClaimResponse(claim);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ClaimSummaryResponse> getMyClaims() {

	    User currentUser = getCurrentUser();

	    return claimRepository
	            .findByClaimedByUserOrderByCreatedAtDesc(currentUser)
	            .stream()
	            .map(this::convertToClaimSummaryResponse)
	            .toList();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ClaimSummaryResponse> getClaimsByItem(Long itemId) {

		Item item = itemRepository.findById(itemId)
		        .orElseThrow(() ->
		                new ResourceNotFoundException(
		                        "Item not found with id : " + itemId));

		User currentUser = getCurrentUser();

		if (!item.getReportedBy().getId().equals(currentUser.getId())) {
		    throw new ForbiddenException(
		            "You are not authorized to view claims for this item.");
		}

	    return claimRepository
	            .findByItemMatch_LostItemOrItemMatch_FoundItemOrderByCreatedAtDesc(
	                    item,
	                    item)
	            .stream()
	            .map(this::convertToClaimSummaryResponse)
	            .toList();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ClaimSummaryResponse> getClaimsByMatch(Long itemMatchId) {

	    ItemMatch itemMatch = getItemMatchByIdOrThrow(itemMatchId);
	    
	    User currentUser = getCurrentUser();

	    boolean isLostOwner =
	            itemMatch.getLostItem().getReportedBy().getId()
	                    .equals(currentUser.getId());

	    boolean isFoundReporter =
	            itemMatch.getFoundItem().getReportedBy().getId()
	                    .equals(currentUser.getId());

	    if (!isLostOwner && !isFoundReporter) {
	        throw new ForbiddenException(
	                "You are not authorized to view claims for this match.");
	    }

	    return claimRepository
	            .findByItemMatchOrderByCreatedAtDesc(itemMatch)
	            .stream()
	            .map(this::convertToClaimSummaryResponse)
	            .toList();
	}
	
	@Override
	@Transactional
	public void withdrawClaim(Long claimId) {

	    User currentUser = getCurrentUser();

	    Claim claim = getClaimByIdOrThrow(claimId);

	    validateClaimWithdrawal(currentUser, claim);

	    claim.setStatus(ClaimStatus.CANCELLED);

	    claimRepository.save(claim);
	    
	    notificationService.createNotification(
	            claim.getItemMatch().getFoundItem().getReportedBy(),
	            NotificationType.CLAIM_WITHDRAWN,
	            "A claimant has withdrawn their claim for your found item: "
	                    + claim.getItemMatch().getFoundItem().getTitle(),
	            claim.getItemMatch().getFoundItem(),
	            claim.getItemMatch(),
	            claim
	    );
	}
	//------------------------------------
	// HELPER METHODS
	//------------------------------------
	private User getCurrentUser() {

	    Authentication authentication = SecurityContextHolder
	            .getContext()
	            .getAuthentication();

	    String email = authentication.getName();

	    return userRepository.findByEmail(email)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("User not found."));
	}
	
	private Claim getClaimByIdOrThrow(Long claimId) {

	    return claimRepository.findById(claimId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException(
	                            "Claim not found with id : " + claimId));
	}
	
	private ItemMatch getItemMatchByIdOrThrow(Long itemMatchId) {

	    return itemMatchRepository.findById(itemMatchId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException(
	                            "Item Match not found with id : " + itemMatchId));
	}
	
	private ClaimSummaryResponse convertToClaimSummaryResponse(Claim claim) {

	    return ClaimSummaryResponse.builder()
	            .claimId(claim.getId())
	            .itemMatchId(claim.getItemMatch().getId())
	            .matchScoreAtClaimTime(claim.getMatchScoreAtClaimTime())
	            .status(claim.getStatus())
	            .createdAt(claim.getCreatedAt())
	            .build();
	}
	
	private ClaimResponse convertToClaimResponse(Claim claim) {

	    return ClaimResponse.builder()
	            .claimId(claim.getId())
	            .itemMatchId(claim.getItemMatch().getId())
	            .claimedByUserId(claim.getClaimedByUser().getId())
	            .claimedByUserName(claim.getClaimedByUser().getFullName())
	            .matchScoreAtClaimTime(claim.getMatchScoreAtClaimTime())
	            .status(claim.getStatus())
	            .createdAt(claim.getCreatedAt())
	            .ownershipAnswers(
	                    ownershipResponseRepository
	                            .findByClaimOrderByOwnershipQuestion_DisplayOrderAsc(claim)
	                            .stream()
	                            .map(response -> OwnershipAnswerResponse.builder()
	                                    .ownershipQuestionId(
	                                            response.getOwnershipQuestion().getId())
	                                    .questionText(
	                                            response.getOwnershipQuestion().getQuestionText())
	                                    .responseText(
	                                            response.getResponseText())
	                                    .build())
	                            .toList()
	            )
	            .build();
	}
	
	private void validateClaimEligibility(User currentUser, ItemMatch itemMatch) {

	    // Check if Lost Item is still OPEN
	    if (itemMatch.getLostItem().getStatus() != ItemStatus.OPEN) {
	        throw new InvalidRequestException(
	                "This lost item is no longer available for claiming.");
	    }

	    // Check if Found Item is still OPEN
	    if (itemMatch.getFoundItem().getStatus() != ItemStatus.OPEN) {
	        throw new InvalidRequestException(
	                "This found item is no longer available for claiming.");
	    }

	    // User cannot claim their own Found Item
	    if (itemMatch.getFoundItem().getReportedBy().getId()
	            .equals(currentUser.getId())) {

	        throw new InvalidRequestException(
	                "You cannot claim your own found item.");
	    }

	    // Prevent duplicate claims
	    if (claimRepository.existsByClaimedByUserAndItemMatch(
	            currentUser,
	            itemMatch)) {

	        throw new InvalidRequestException(
	                "You have already submitted a claim for this match.");
	    }
	}
	
	private void validateOwnershipResponses(ItemMatch itemMatch, SubmitClaimRequest request) {

		List<OwnershipQuestion> questions =
		        ownershipQuestionRepository.findByItemOrderByDisplayOrderAsc(
		                itemMatch.getFoundItem());

	    List<OwnershipAnswerRequest> answers =
	            request.getOwnershipAnswers();

	    // Number of answers must match number of questions
	    if (questions.size() != answers.size()) {
	        throw new InvalidRequestException(
	                "All ownership questions must be answered.");
	    }

	    Set<Long> validQuestionIds = questions.stream()
	            .map(OwnershipQuestion::getId)
	            .collect(Collectors.toSet());

	    Set<Long> submittedQuestionIds = new HashSet<>();

	    for (OwnershipAnswerRequest answer : answers) {

	        // Question must belong to this Found Item
	        if (!validQuestionIds.contains(
	                answer.getOwnershipQuestionId())) {

	            throw new InvalidRequestException(
	                    "Invalid ownership question submitted.");
	        }

	        // Duplicate question
	        if (!submittedQuestionIds.add(
	                answer.getOwnershipQuestionId())) {

	            throw new InvalidRequestException(
	                    "Duplicate ownership question detected.");
	        }
	    }
	}
	
	private Double calculateMatchScoreSnapshot(ItemMatch itemMatch) {

	    return itemMatch.getMatchScore();
	}
	
	private void createOwnershipResponses(Claim claim, SubmitClaimRequest request) {

	    List<OwnershipResponse> responses = new ArrayList<>();

	    for (OwnershipAnswerRequest answer : request.getOwnershipAnswers()) {

	        OwnershipQuestion question =
	                ownershipQuestionRepository.findById(
	                        answer.getOwnershipQuestionId())
	                .orElseThrow(() -> new ResourceNotFoundException(
	                        "Ownership Question not found with id : "
	                                + answer.getOwnershipQuestionId()));

	        OwnershipResponse response = OwnershipResponse.builder()
	                .claim(claim)
	                .ownershipQuestion(question)
	                .responseText(answer.getResponseText().trim())
	                .build();

	        responses.add(response);
	    }

	    ownershipResponseRepository.saveAll(responses);
	}
	
	private void validateClaimWithdrawal(
	        User currentUser,
	        Claim claim) {

	    // Only the claimant can withdraw
	    if (!claim.getClaimedByUser().getId()
	            .equals(currentUser.getId())) {

	        throw new ForbiddenException(
	                "You are not authorized to withdraw this claim.");
	    }

	    // Already withdrawn
	    if (claim.getStatus() == ClaimStatus.CANCELLED) {

	        throw new InvalidRequestException(
	                "Claim has already been withdrawn.");
	    }

	    // Approved claims cannot be withdrawn
	    if (claim.getStatus() == ClaimStatus.APPROVED) {

	        throw new InvalidRequestException(
	                "Approved claims cannot be withdrawn.");
	    }

	    // Rejected claims cannot be withdrawn
	    if (claim.getStatus() == ClaimStatus.REJECTED) {

	        throw new InvalidRequestException(
	                "Rejected claims cannot be withdrawn.");
	    }
	}
	
	private double verifyOwnershipAnswers(Claim claim) {

	    List<OwnershipResponse> responses =
	            ownershipResponseRepository
	                    .findByClaimOrderByOwnershipQuestion_DisplayOrderAsc(claim);

	    int totalQuestions = responses.size();
	    int correctAnswers = 0;

	    for (OwnershipResponse response : responses) {

	        String expectedAnswer =
	                normalizeAnswer(
	                        response.getOwnershipQuestion().getExpectedAnswer());

	        String userAnswer =
	                normalizeAnswer(response.getResponseText());

	        if (expectedAnswer.equals(userAnswer)) {
	            correctAnswers++;
	        }
	    }

	    return calculateVerificationScore(correctAnswers, totalQuestions);
	}
	
	private String normalizeAnswer(String answer) {

	    if (answer == null) {
	        return "";
	    }

	    return answer.trim()
	            .toLowerCase()
	            .replaceAll("\\s+", " ");
	}
	
	private double calculateVerificationScore(
	        int correctAnswers,
	        int totalQuestions) {

	    if (totalQuestions == 0) {
	        return 0.0;
	    }

	    return ((double) correctAnswers / totalQuestions) * 100;
	}
	
	private void approveClaim(
	        Claim claim,
	        double verificationScore) {

	    claim.setStatus(ClaimStatus.APPROVED);
	    claim.setResolvedAt(java.time.LocalDateTime.now());
	    claim.setReviewNotes(
	            "Automatically approved. Verification Score : "
	                    + verificationScore + "%");

	    claimRepository.save(claim);

	    closeMatchedItems(claim);

	    notificationService.createNotification(
	            claim.getClaimedByUser(),
	            NotificationType.MATCH_APPROVED,
	            "Congratulations! Your claim has been approved.",
	            claim.getItemMatch().getLostItem(),
	            claim.getItemMatch(),
	            claim);

	    notificationService.createNotification(
	            claim.getItemMatch().getFoundItem().getReportedBy(),
	            NotificationType.MATCH_APPROVED,
	            "A claim has been approved for your found item: "
	                    + claim.getItemMatch().getFoundItem().getTitle(),
	            claim.getItemMatch().getFoundItem(),
	            claim.getItemMatch(),
	            claim);
	}
	
	private void rejectClaim(
	        Claim claim,
	        double verificationScore) {

	    claim.setStatus(ClaimStatus.REJECTED);
	    claim.setResolvedAt(java.time.LocalDateTime.now());
	    claim.setReviewNotes(
	            "Automatically rejected. Verification Score : "
	                    + verificationScore + "%");

	    claimRepository.save(claim);

	    notificationService.createNotification(
	            claim.getClaimedByUser(),
	            NotificationType.CLAIM_REJECTED,
	            "Your claim could not be verified automatically.",
	            claim.getItemMatch().getLostItem(),
	            claim.getItemMatch(),
	            claim);
	}
	
	private void closeMatchedItems(Claim claim) {

	    Item lostItem = claim.getItemMatch().getLostItem();
	    Item foundItem = claim.getItemMatch().getFoundItem();

	    lostItem.setStatus(ItemStatus.CLOSED);
	    foundItem.setStatus(ItemStatus.CLOSED);

	    itemRepository.save(lostItem);
	    itemRepository.save(foundItem);
	}
	
}

package com.cdac.service.Impl;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.dto.request.ChangePasswordRequest;
import com.cdac.dto.request.UpdateProfileRequest;
import com.cdac.dto.request.UpdateUserStatusRequest;
import com.cdac.dto.response.UserDashboardResponse;
import com.cdac.dto.response.UserProfileResponse;
import com.cdac.dto.response.UserSummaryResponse;
import com.cdac.entity.User;
import com.cdac.enums.AccountStatus;
import com.cdac.enums.ClaimStatus;
import com.cdac.enums.ItemType;
import com.cdac.enums.UserRole;
import com.cdac.exception.InvalidRequestException;
import com.cdac.exception.ResourceNotFoundException;
import com.cdac.repository.ClaimRepository;
import com.cdac.repository.ItemRepository;
import com.cdac.repository.UserRepository;
import com.cdac.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	private final ItemRepository itemRepository;
	private final ClaimRepository claimRepository;
	private final PasswordEncoder passwordEncoder;
	
	// -----------------USER PROFILE---------------------
	@Override
	@Transactional(readOnly = true)
	public UserProfileResponse getMyProfile() {

	    User currentUser = getCurrentUser();
	    
	    log.info("Profile fetched for user: {} ({})", currentUser.getId(),
	            currentUser.getEmail());

	    return convertToUserProfileResponse(currentUser);
	}
	
	@Override
	public UserProfileResponse updateProfile(UpdateProfileRequest request) {

	    User currentUser = getCurrentUser();
	    
	    log.info("Profile update requested by user: {} ({})", currentUser.getId(),
	            currentUser.getEmail());

	    currentUser.setFullName(request.getFullName().trim());
	    currentUser.setPhoneNumber(request.getPhoneNumber());

	    userRepository.save(currentUser);
	    
	    log.info("Profile updated successfully for user: {}", currentUser.getId());

	    return convertToUserProfileResponse(currentUser);
	}
	
	@Override
	public void changePassword(ChangePasswordRequest request) {

	    User currentUser = getCurrentUser();
	    
	    log.info("Password change requested for user: {}", currentUser.getEmail());

	    validatePasswordChange(currentUser, request);

	    currentUser.setPassword(
	            passwordEncoder.encode(request.getNewPassword()));

	    userRepository.save(currentUser);
	    
	    log.info("Password changed successfully for user: {}", currentUser.getEmail());
	}
	
	@Override
	public void deactivateMyAccount() {

	    User currentUser = getCurrentUser();
	    
	    log.warn("Account deactivation requested by user: {} ({})", currentUser.getId(),
	            currentUser.getEmail());

	    if (currentUser.getAccountStatus() == AccountStatus.DEACTIVATED) {
	        throw new InvalidRequestException(
	                "Your account is already deactivated.");
	    }

	    currentUser.setAccountStatus(AccountStatus.DEACTIVATED);

	    userRepository.save(currentUser);
	    
	    log.warn("Account deactivated successfully for user: {}", currentUser.getEmail());
	}
	
	@Override
	@Transactional(readOnly = true)
	public UserDashboardResponse getDashboard() {

	    User currentUser = getCurrentUser();
	    
	    log.info("Dashboard requested by user: {}", currentUser.getEmail());

	    long totalItems =
	            itemRepository.countByReportedBy(currentUser);

	    long lostItems =
	            itemRepository.countByReportedByAndItemType(
	                    currentUser,
	                    ItemType.LOST);

	    long foundItems =
	            itemRepository.countByReportedByAndItemType(
	                    currentUser,
	                    ItemType.FOUND);

	    long activeClaims =
	            claimRepository.countByClaimedByUserAndStatusIn(
	                    currentUser,
	                    List.of(
	                            ClaimStatus.PENDING,
	                            ClaimStatus.APPROVED));

	    long approvedClaims =
	            claimRepository.countByClaimedByUserAndStatus(
	                    currentUser,
	                    ClaimStatus.APPROVED);

	    return UserDashboardResponse.builder()
	            .totalItems(totalItems)
	            .lostItems(lostItems)
	            .foundItems(foundItems)
	            .activeClaims(activeClaims)
	            .approvedClaims(approvedClaims)
	            .build();
	}
	
	//---------------ADMIN OPERATIONS---------------
	@Override
	@Transactional(readOnly = true)
	public List<UserSummaryResponse> getAllUsers() {
		
		log.info("Admin requested all registered users.");

	    return userRepository.findAll()
	            .stream()
	            .map(this::convertToUserSummaryResponse)
	            .toList();
	}
	
	@Override
	@Transactional(readOnly = true)
	public UserProfileResponse getUserById(Long userId) {

	    User user = getUserByIdOrThrow(userId);
	    
	    log.info("Admin viewed profile of user: {}", user.getEmail());

	    return convertToUserProfileResponse(user);
	}
	
	@Override
	public UserProfileResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {

	    User user = getUserByIdOrThrow(userId);

	    validateUserStatusUpdate(user, request);

	    user.setAccountStatus(request.getAccountStatus());

	    userRepository.save(user);
	    
	    log.warn("Admin action: user id={} ({}) account status changed to {}",
	            user.getId(), user.getEmail(), request.getAccountStatus());

	    return convertToUserProfileResponse(user);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<UserSummaryResponse> getUsersByStatus(AccountStatus status) {
		
		log.info("Admin requested users with status: {}", status);

	    return userRepository.findByAccountStatus(status)
	            .stream()
	            .map(this::convertToUserSummaryResponse)
	            .toList();
	}
	
	
	
	//----------------HELPER METHODS------------------
	private User getCurrentUser() {

	    Authentication authentication =
	            SecurityContextHolder.getContext().getAuthentication();

	    String email = authentication.getName();

	    return userRepository.findByEmail(email)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException(
	                            "User not found."));
	}
	
	private User getUserByIdOrThrow(Long userId) {

	    return userRepository.findById(userId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException(
	                            "User not found with id : " + userId));
	}
	
	private UserProfileResponse convertToUserProfileResponse(User user) {

	    return UserProfileResponse.builder()
	            .userId(user.getId())
	            .fullName(user.getFullName())
	            .email(user.getEmail())
	            .phoneNumber(user.getPhoneNumber())
	            .role(user.getRole())
	            .accountStatus(user.getAccountStatus())
	            .createdAt(user.getCreatedAt())
	            .build();
	}
	
	private UserSummaryResponse convertToUserSummaryResponse(User user) {

	    return UserSummaryResponse.builder()
	            .userId(user.getId())
	            .fullName(user.getFullName())
	            .email(user.getEmail())
	            .role(user.getRole())
	            .accountStatus(user.getAccountStatus())
	            .createdAt(user.getCreatedAt())
	            .build();
	}
	
	private void validatePasswordChange(
	        User user,
	        ChangePasswordRequest request) {

	    if (!passwordEncoder.matches(
	            request.getCurrentPassword(),
	            user.getPassword())) {

	        throw new InvalidRequestException(
	                "Current password is incorrect.");
	    }

	    if (!request.getNewPassword()
	            .equals(request.getConfirmPassword())) {

	        throw new InvalidRequestException(
	                "New password and confirm password do not match.");
	    }

	    if (passwordEncoder.matches(
	            request.getNewPassword(),
	            user.getPassword())) {

	        throw new InvalidRequestException(
	                "New password must be different from the current password.");
	    }
	}
	
	private void validateUserStatusUpdate(
	        User user,
	        UpdateUserStatusRequest request) {

	    if (user.getRole() == UserRole.ADMIN) {

	        throw new InvalidRequestException(
	                "Admin account status cannot be modified.");
	    }

	    if (user.getAccountStatus() == request.getAccountStatus()) {

	        throw new InvalidRequestException(
	                "User already has the selected account status.");
	    }
	}

}

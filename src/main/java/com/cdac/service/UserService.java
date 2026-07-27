package com.cdac.service;

import java.util.List;

import com.cdac.dto.request.ChangePasswordRequest;
import com.cdac.dto.request.UpdateProfileRequest;
import com.cdac.dto.request.UpdateUserStatusRequest;
import com.cdac.dto.response.UserDashboardResponse;
import com.cdac.dto.response.UserProfileResponse;
import com.cdac.dto.response.UserSummaryResponse;
import com.cdac.enums.AccountStatus;

public interface UserService {

    // ==========================================
    // USER OPERATIONS
    // ==========================================

    /**
     * Returns the profile of the currently logged-in user.
     */
    UserProfileResponse getMyProfile();

    /**
     * Updates the profile of the currently logged-in user.
     */
    UserProfileResponse updateProfile(UpdateProfileRequest request);

    /**
     * Changes the password of the currently logged-in user.
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Changes the password of the currently logged-in user.
     */
    void deactivateMyAccount();
    
    /**
     * Delete account of the logged-in user.
     */
    UserDashboardResponse getDashboard();

    // ==========================================
    // ADMIN OPERATIONS
    // ==========================================

    /**
     * Returns all registered users.
     */
    List<UserSummaryResponse> getAllUsers();

    /**
     * Returns details of a specific user.
     */
    UserProfileResponse getUserById(Long userId);

    /**
     * Updates the account status (ACTIVE/SUSPENDED) of a user.
     */
    UserProfileResponse updateUserStatus(Long userId, UpdateUserStatusRequest request);
    
    List<UserSummaryResponse> getUsersByStatus(AccountStatus status);
}
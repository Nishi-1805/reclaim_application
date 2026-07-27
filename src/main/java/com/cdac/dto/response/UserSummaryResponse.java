package com.cdac.dto.response;

import java.time.LocalDateTime;

import com.cdac.enums.AccountStatus;
import com.cdac.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private Long userId;

    private String fullName;

    private String email;

    private UserRole role;

    private AccountStatus accountStatus;
    
    private LocalDateTime createdAt;
}
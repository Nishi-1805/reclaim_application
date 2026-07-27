package com.cdac.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.cdac.enums.ClaimStatus;

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
public class ClaimResponse {

    private Long claimId;

    private Long itemMatchId;

    private Long claimedByUserId;
    
    private String claimedByUserName;

    private Double matchScoreAtClaimTime;

    private ClaimStatus status;

    private LocalDateTime createdAt;

    private List<OwnershipAnswerResponse> ownershipAnswers;

}
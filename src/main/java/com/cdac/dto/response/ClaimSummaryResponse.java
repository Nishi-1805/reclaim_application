package com.cdac.dto.response;

import java.time.LocalDateTime;

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
public class ClaimSummaryResponse {

    private Long claimId;

    private Long itemMatchId;

    private Double matchScoreAtClaimTime;

    private ClaimStatus status;

    private LocalDateTime createdAt;

}
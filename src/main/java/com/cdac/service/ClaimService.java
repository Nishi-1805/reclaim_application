package com.cdac.service;

import java.util.List;

import com.cdac.dto.request.SubmitClaimRequest;
import com.cdac.dto.response.ClaimResponse;
import com.cdac.dto.response.ClaimSummaryResponse;

public interface ClaimService {

    ClaimResponse submitClaim(SubmitClaimRequest request);

    ClaimResponse getClaimById(Long claimId);

    List<ClaimSummaryResponse> getMyClaims();

    List<ClaimSummaryResponse> getClaimsByItem(Long itemId);

    List<ClaimSummaryResponse> getClaimsByMatch(Long itemMatchId);

    void withdrawClaim(Long claimId);

}
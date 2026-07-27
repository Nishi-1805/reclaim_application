package com.cdac.service;

import java.util.List;

import com.cdac.dto.response.OwnershipAnswerResponse;

public interface OwnershipResponseService {

    List<OwnershipAnswerResponse> getResponsesByClaim(Long claimId);

}
package com.cdac.service;

import java.util.List;

import com.cdac.dto.response.OwnershipQuestionResponse;

public interface OwnershipQuestionService {

    /**
     * Returns all ownership questions for a given item.
     * Questions are returned in display order.
     */
    List<OwnershipQuestionResponse> getQuestionsByItem(Long itemId);

}
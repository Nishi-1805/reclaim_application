package com.cdac.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.dto.response.OwnershipQuestionResponse;
import com.cdac.entity.Item;
import com.cdac.entity.OwnershipQuestion;
import com.cdac.enums.ItemType;
import com.cdac.exception.InvalidRequestException;
import com.cdac.exception.ResourceNotFoundException;
import com.cdac.repository.ItemRepository;
import com.cdac.repository.OwnershipQuestionRepository;
import com.cdac.service.OwnershipQuestionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OwnershipQuestionServiceImpl implements OwnershipQuestionService {

    private final OwnershipQuestionRepository ownershipQuestionRepository;

    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OwnershipQuestionResponse> getQuestionsByItem(Long itemId) {

        Item item = getItemByIdOrThrow(itemId);
        
        if(item.getItemType() != ItemType.FOUND){
            throw new InvalidRequestException(
                    "Ownership questions are available only for found items.");
        }

        return ownershipQuestionRepository
                .findByItemOrderByDisplayOrderAsc(item)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // -------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------

    private Item getItemByIdOrThrow(Long itemId) {

        return itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Item not found with id : " + itemId));
    }

    private OwnershipQuestionResponse convertToResponse(OwnershipQuestion ownershipQuestion) {

        return OwnershipQuestionResponse.builder()
                .ownershipQuestionId(ownershipQuestion.getId())
                .questionText(ownershipQuestion.getQuestionText())
                .displayOrder(ownershipQuestion.getDisplayOrder())
                .build();
    }
}
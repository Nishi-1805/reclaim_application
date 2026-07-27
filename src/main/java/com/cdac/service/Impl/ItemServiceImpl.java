package com.cdac.service.Impl;

import java.util.List;

import com.cdac.exception.ForbiddenException;
import com.cdac.exception.InvalidRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.constant.AppConstants;
import com.cdac.dto.request.CreateItemRequest;
import com.cdac.dto.request.ItemImageRequest;
import com.cdac.dto.request.OwnershipQuestionRequest;
import com.cdac.dto.request.UpdateItemRequest;
import com.cdac.dto.response.ItemResponse;
import com.cdac.entity.Item;
import com.cdac.entity.ItemImage;
import com.cdac.entity.OwnershipQuestion;
import com.cdac.entity.User;
import com.cdac.enums.ClaimStatus;
import com.cdac.enums.ItemStatus;
import com.cdac.enums.ItemType;
import com.cdac.exception.ResourceNotFoundException;
import com.cdac.repository.ClaimRepository;
import com.cdac.repository.ItemImageRepository;
import com.cdac.repository.ItemRepository;
import com.cdac.repository.OwnershipQuestionRepository;
import com.cdac.repository.UserRepository;
import com.cdac.service.ItemMatchService;
import com.cdac.service.ItemService;
import com.cdac.dto.response.OwnershipQuestionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final OwnershipQuestionRepository ownershipQuestionRepository;
    private final ClaimRepository claimRepository;
    private final ItemMatchService itemMatchService;
    private final ItemImageRepository itemImageRepository;

    @Override
    public ItemResponse createItem(CreateItemRequest request) {

        // Get logged-in user
        User currentUser = getCurrentUser();

        // Validate ownership questions
        validateOwnershipQuestions(request);

        // Create Item entity
        Item item = Item.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .brand(request.getBrand())
                .color(request.getColor())
                .locationDescription(request.getLocationDescription())
                .itemDate(request.getItemDate())
                .itemType(request.getItemType())
                .status(ItemStatus.OPEN)
                .reportedBy(currentUser)
                .build();

        // Save Item first
        Item savedItem = itemRepository.save(item);

        // Create ownership questions only for FOUND items
        if (request.getItemType() == ItemType.FOUND) {

            createOwnershipQuestions(
                    savedItem,
                    request.getOwnershipQuestions());
        }
        
     // Save item images (for both LOST and FOUND items)
        createItemImages(savedItem, request.getImages());
        
        itemMatchService.generateMatchesForItem(savedItem.getId());

        // Return response
        return convertToItemResponse(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long itemId) {

        Item item = getItemByIdOrThrow(itemId);

        return convertToItemResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {

        return itemRepository.findByStatusOrderByCreatedAtDesc(ItemStatus.OPEN)
                .stream()
                .map(this::convertToItemResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getMyItems() {

        User currentUser = getCurrentUser();

        List<Item> items = itemRepository.findByReportedByOrderByCreatedAtDesc(currentUser);

        return items.stream()
                .map(this::convertToItemResponse)
                .toList();
    }

    @Override
    public ItemResponse updateItem(Long itemId, UpdateItemRequest request) {

        User currentUser = getCurrentUser();

        Item item = getItemByIdOrThrow(itemId);

        // Only the owner can update the item
        if (!item.getReportedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to update this item.");
        }

        // Validate business rules
        validateItemEditable(item);

        // Update fields
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setBrand(request.getBrand());
        item.setColor(request.getColor());
        item.setLocationDescription(request.getLocationDescription());
        item.setItemDate(request.getItemDate());

        itemRepository.save(item);

        return convertToItemResponse(item);
    }

    @Override
    public void deleteItem(Long itemId) {

        User currentUser = getCurrentUser();

        Item item = getItemByIdOrThrow(itemId);

        // Only the owner can cancel the item
        if (!item.getReportedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to cancel this item.");
        }

        // Validate business rules
        validateItemDeletion(item);

        // Soft delete
        item.setStatus(ItemStatus.CANCELLED);

        itemRepository.save(item);
    }

    
    /* Helper methods will go below */
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    private Item getItemByIdOrThrow(Long itemId) {

        return itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Item not found with id : " + itemId));
    }
    
    private void validateOwnershipQuestions(CreateItemRequest request) {

        if (request.getItemType() == ItemType.FOUND) {

            if (request.getOwnershipQuestions() == null
                    || request.getOwnershipQuestions().size() < 3
                    || request.getOwnershipQuestions().size() > 5) {

                throw new InvalidRequestException(
                        "Found items must have between 3 and 5 ownership questions.");
            }

        } else {

            if (request.getOwnershipQuestions() != null
                    && !request.getOwnershipQuestions().isEmpty()) {

                throw new InvalidRequestException(
                        "Ownership questions are allowed only for found items.");
            }
        }
    }
        
        private ItemResponse convertToItemResponse(Item item) {

            return ItemResponse.builder()
                    .id(item.getId())
                    .title(item.getTitle())
                    .description(item.getDescription())
                    .category(item.getCategory())
                    .brand(item.getBrand())
                    .color(item.getColor())
                    .locationDescription(item.getLocationDescription())
                    .itemDate(item.getItemDate())
                    .itemType(item.getItemType())
                    .status(item.getStatus())
                    .ownerId(item.getReportedBy().getId())
                    .ownerName(item.getReportedBy().getFullName())
                    .imageUrls(
                            item.getImages()
                                    .stream()
                                    .map(ItemImage::getImageUrl)
                                    .toList()
                    )
                    .ownershipQuestions(

                            item.getOwnershipQuestions()
                                    .stream()
                                    .map(question -> OwnershipQuestionResponse.builder()
                                            .ownershipQuestionId(question.getId())
                                            .questionText(question.getQuestionText())
                                            .displayOrder(question.getDisplayOrder())
                                            .build())
                                    .toList()

                    )
                    .build();
        }
        
        private void createOwnershipQuestions(Item item, List<OwnershipQuestionRequest> list) {

            int displayOrder = 1;

            for (OwnershipQuestionRequest question : list) {

                OwnershipQuestion ownershipQuestion = OwnershipQuestion.builder()
                        .item(item)
                        .questionText(question.getQuestionText())
                        .expectedAnswer(question.getExpectedAnswer())
                        .displayOrder(displayOrder++)
                        .build();

                ownershipQuestionRepository.save(ownershipQuestion);
            }
        }
        
        private void createItemImages(Item item, List<ItemImageRequest> imageRequests) {

        	if (imageRequests == null || imageRequests.isEmpty()) {
        	    return;
        	}

        	if (imageRequests.size() > AppConstants.MAX_ITEM_IMAGES) {
                throw new InvalidRequestException(
                        "Maximum " + AppConstants.MAX_ITEM_IMAGES +
                        " images are allowed.");
            }

            int displayOrder = 1;

            for (ItemImageRequest imageRequest : imageRequests) {

                if (imageRequest == null
                        || imageRequest.getImageUrl() == null
                        || imageRequest.getImageUrl().isBlank()) {
                    continue;
                }

                ItemImage itemImage = ItemImage.builder()
                        .item(item)
                        .imageUrl(imageRequest.getImageUrl().trim())
                        .displayOrder(displayOrder++)
                        .build();

                itemImageRepository.save(itemImage);
            }
        }
        
        private void validateItemEditable(Item item) {

            if (item.getStatus() != ItemStatus.OPEN) {
                throw new InvalidRequestException(
                        "Only open items can be edited.");
            }

            if (claimRepository.existsActiveClaimsForItem(
                    item,
                    List.of(
                            ClaimStatus.PENDING,
                            ClaimStatus.APPROVED))) {

                throw new InvalidRequestException(
                        "Item cannot be edited after a claim has been submitted.");
            }
        }
        
        private void validateItemDeletion(Item item) {

            if (item.getStatus() != ItemStatus.OPEN) {
                throw new InvalidRequestException(
                        "Only open items can be cancelled.");
            }

            if (claimRepository.existsActiveClaimsForItem(
                    item,
                    List.of(
                            ClaimStatus.PENDING,
                            ClaimStatus.APPROVED))) {

                throw new InvalidRequestException(
                        "Item cannot be cancelled after a claim has been submitted.");
            }
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItemResponse> getItemsByType(ItemType itemType) {

            List<Item> items =
                    itemRepository.findByItemTypeAndStatusOrderByCreatedAtDesc(
                            itemType,
                            ItemStatus.OPEN);

            return items.stream()
                    .map(this::convertToItemResponse)
                    .toList();
        }

}
package com.cdac.service.Impl;

import java.util.List;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.constant.AppConstants;
import com.cdac.dto.request.ItemImageRequest;
import com.cdac.dto.response.ItemImageResponse;
import com.cdac.entity.Item;
import com.cdac.entity.ItemImage;
import com.cdac.entity.User;
import com.cdac.enums.ItemStatus;
import com.cdac.exception.ForbiddenException;
import com.cdac.exception.InvalidRequestException;
import com.cdac.exception.ResourceNotFoundException;
import com.cdac.repository.ItemImageRepository;
import com.cdac.repository.ItemRepository;
import com.cdac.repository.UserRepository;
import com.cdac.service.ImageStorageService;
import com.cdac.service.ItemImageService;

import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImageServiceImpl implements ItemImageService {

    private final ItemImageRepository itemImageRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<ItemImageResponse> getImagesByItem(Long itemId) {

        Item item = getItemByIdOrThrow(itemId);

        return itemImageRepository.findByItemOrderByDisplayOrderAsc(item)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public ItemImageResponse addImageToItem(Long itemId, ItemImageRequest request) {

        User currentUser = getCurrentUser();
        Item item = getItemByIdOrThrow(itemId);

        validateItemOwner(currentUser, item);
        validateItemOpen(item);

        List<ItemImage> existingImages =
                itemImageRepository.findByItemOrderByDisplayOrderAsc(item);

        if (existingImages.size() >= AppConstants.MAX_ITEM_IMAGES) {
            throw new InvalidRequestException(
                    "Maximum " + AppConstants.MAX_ITEM_IMAGES + " images are allowed for one item.");
        }

        int nextDisplayOrder = existingImages.stream()
                .map(ItemImage::getDisplayOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        ItemImage itemImage = ItemImage.builder()
                .item(item)
                .imageUrl(request.getImageUrl().trim())
                .displayOrder(nextDisplayOrder)
                .build();

        ItemImage savedImage = itemImageRepository.save(itemImage);

        return convertToResponse(savedImage);
    }
    
    @Override
    public ItemImageResponse addImageToItem(
            Long itemId,
            MultipartFile file) {

        User currentUser = getCurrentUser();

        Item item = getItemByIdOrThrow(itemId);

        validateItemOwner(currentUser, item);

        validateItemOpen(item);

        List<ItemImage> existingImages =
                itemImageRepository.findByItemOrderByDisplayOrderAsc(item);

        if (existingImages.size() >= AppConstants.MAX_ITEM_IMAGES) {

            throw new InvalidRequestException(
                    "Maximum "
                            + AppConstants.MAX_ITEM_IMAGES
                            + " images are allowed for one item.");
        }

        String imageUrl = imageStorageService.uploadImage(file);

        int nextDisplayOrder =
                existingImages.stream()
                        .map(ItemImage::getDisplayOrder)
                        .filter(Objects::nonNull)
                        .max(Integer::compareTo)
                        .orElse(0) + 1;

        ItemImage itemImage = ItemImage.builder()
                .item(item)
                .imageUrl(imageUrl)
                .displayOrder(nextDisplayOrder)
                .build();

        ItemImage savedImage =
                itemImageRepository.save(itemImage);

        return convertToResponse(savedImage);
    }

    @Override
    public void deleteImage(Long itemId, Long imageId) {

        User currentUser = getCurrentUser();
        Item item = getItemByIdOrThrow(itemId);

        validateItemOwner(currentUser, item);
        validateItemOpen(item);

        ItemImage itemImage = itemImageRepository.findByIdAndItem(imageId, item)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Item image not found with id : " + imageId));
        
        imageStorageService.deleteImage(itemImage.getImageUrl());

        itemImageRepository.delete(itemImage);
    }

    // -------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found."));
    }

    private Item getItemByIdOrThrow(Long itemId) {

        return itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Item not found with id : " + itemId));
    }

    private void validateItemOwner(User currentUser, Item item) {

        if (!item.getReportedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(
                    "You are not authorized to manage images for this item.");
        }
    }

    private void validateItemOpen(Item item) {

        if (item.getStatus() != ItemStatus.OPEN) {
            throw new InvalidRequestException(
                    "Images can only be modified for open items.");
        }
    }

    private ItemImageResponse convertToResponse(ItemImage itemImage) {

        return ItemImageResponse.builder()
                .itemImageId(itemImage.getId())
                .itemId(itemImage.getItem().getId())
                .imageUrl(itemImage.getImageUrl())
                .displayOrder(itemImage.getDisplayOrder())
                .createdAt(itemImage.getCreatedAt())
                .build();
    }
}
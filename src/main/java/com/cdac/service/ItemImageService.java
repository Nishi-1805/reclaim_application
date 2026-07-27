package com.cdac.service;

import java.util.List;

import com.cdac.dto.request.ItemImageRequest;
import com.cdac.dto.response.ItemImageResponse;

public interface ItemImageService {

    List<ItemImageResponse> getImagesByItem(Long itemId);

    ItemImageResponse addImageToItem(Long itemId, ItemImageRequest request);

    void deleteImage(Long itemId, Long imageId);
}
package com.cdac.service;

import java.util.List;

import com.cdac.dto.request.ItemImageRequest;
import com.cdac.dto.response.ItemImageResponse;

import org.springframework.web.multipart.MultipartFile;

public interface ItemImageService {

    List<ItemImageResponse> getImagesByItem(Long itemId);

    ItemImageResponse addImageToItem(Long itemId, ItemImageRequest request);
    
    ItemImageResponse addImageToItem(Long itemId, MultipartFile file);

    void deleteImage(Long itemId, Long imageId);
}
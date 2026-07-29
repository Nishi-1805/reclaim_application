package com.cdac.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    /**
     * Uploads an image to cloud storage.
     *
     * @param file image file
     * @return secure image URL
     */
    String uploadImage(MultipartFile file);

    /**
     * Deletes an image from cloud storage.
     *
     * @param imageUrl stored image URL
     */
    void deleteImage(String imageUrl);
}
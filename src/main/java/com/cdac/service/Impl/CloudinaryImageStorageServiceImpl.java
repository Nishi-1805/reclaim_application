package com.cdac.service.Impl;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cdac.exception.InvalidRequestException;
import com.cdac.service.ImageStorageService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryImageStorageServiceImpl
        implements ImageStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) {

        validateImage(file);
        
        log.info("Uploading image to Cloudinary. Original filename={}, size={} bytes",
                file.getOriginalFilename(),
                file.getSize());

        try {

            Map<?, ?> uploadResult =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.emptyMap());

            String imageUrl = uploadResult.get("secure_url").toString();

            log.info("Image uploaded successfully to Cloudinary. URL={}",
                    imageUrl);

            return imageUrl;

        } catch (IOException e) {

        	 log.error("Cloudinary upload failed for file={}",
        	            file.getOriginalFilename(),
        	            e);
        	 
            throw new InvalidRequestException(
                    "Failed to upload image.");
        }
    }

    @Override
    public void deleteImage(String imageUrl) {

        try {

            String publicId = extractPublicId(imageUrl);

            log.info("Deleting Cloudinary image. publicId={}",
                    publicId);

            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap());
            
            log.info("Cloudinary image deleted successfully. publicId={}",
                    publicId);

        } catch (Exception e) {
        	

            log.error("Cloudinary image deletion failed. imageUrl={}",
                    imageUrl,
                    e);

            throw new InvalidRequestException(
                    "Failed to delete image.");
        }
    }

    /**
     * Validates uploaded image.
     */
    private void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new InvalidRequestException(
                    "Image file is required.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new InvalidRequestException(
                    "Image size cannot exceed 5 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null
                || !contentType.startsWith("image/")) {

            throw new InvalidRequestException(
                    "Only image files are allowed.");
        }
    }

    /**
     * Extracts Cloudinary public ID from secure URL.
     */
    private String extractPublicId(String imageUrl) {

        String[] parts = imageUrl.split("/");

        String fileName = parts[parts.length - 1];

        return fileName.substring(
                0,
                fileName.lastIndexOf('.'));
    }
}
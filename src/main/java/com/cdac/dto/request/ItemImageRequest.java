package com.cdac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemImageRequest {

    @NotBlank(message = "Image URL is required.")
    @Size(max = 500, message = "Image URL cannot exceed 500 characters.")
    private String imageUrl;
}
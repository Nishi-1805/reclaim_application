package com.cdac.dto.response;

import java.time.LocalDateTime;

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
public class ItemImageResponse {

    private Long itemImageId;

    private Long itemId;

    private String imageUrl;

    private Integer displayOrder;

    private LocalDateTime createdAt;
}
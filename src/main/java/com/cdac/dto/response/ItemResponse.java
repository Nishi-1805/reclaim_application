package com.cdac.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.cdac.enums.ItemStatus;
import com.cdac.enums.ItemType;

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
public class ItemResponse {

    private Long id;

    private String title;

    private String description;

    private String category;

    private String brand;

    private String color;

    private String locationDescription;

    private LocalDate itemDate;

    private ItemType itemType;

    private ItemStatus status;

    private Long ownerId;

    private String ownerName;

    private List<String> imageUrls;

    private List<OwnershipQuestionResponse> ownershipQuestions;
}
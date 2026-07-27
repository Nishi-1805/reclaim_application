package com.cdac.dto.response;

import java.time.LocalDateTime;

import com.cdac.enums.MatchStatus;

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
public class ItemMatchResponse {

    private Long itemMatchId;

    private Long lostItemId;

    private String lostItemTitle;

    private Long foundItemId;

    private String foundItemTitle;

    private Double matchScore;

    private MatchStatus matchStatus;

    private String matchReason;

    private LocalDateTime createdAt;
}
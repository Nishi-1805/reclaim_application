package com.cdac.dto.response;

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
public class ItemMatchSummaryResponse {

    private Long itemMatchId;

    private Long itemId;

    private String itemTitle;

    private Double matchScore;

    private MatchStatus matchStatus;
}

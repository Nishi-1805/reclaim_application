package com.cdac.dto.response;

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
public class UserDashboardResponse {

    private long totalItems;

    private long lostItems;

    private long foundItems;

    private long activeClaims;

    private long approvedClaims;
}
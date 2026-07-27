package com.cdac.service;

import java.util.List;

import com.cdac.dto.response.ItemMatchResponse;
import com.cdac.dto.response.ItemMatchSummaryResponse;

public interface ItemMatchService {
	
	 void generateMatchesForItem(Long itemId);

	    List<ItemMatchSummaryResponse> getMatchesForLostItem(Long lostItemId);

	    List<ItemMatchSummaryResponse> getMatchesForFoundItem(Long foundItemId);

	    ItemMatchResponse getMatchById(Long matchId);

	    void confirmMatch(Long matchId);

	    void rejectMatch(Long matchId);

}

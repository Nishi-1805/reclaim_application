package com.cdac.service.Impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.cdac.dto.response.ItemMatchResponse;
import com.cdac.dto.response.ItemMatchSummaryResponse;
import com.cdac.entity.Item;
import com.cdac.entity.ItemMatch;
import com.cdac.enums.ItemStatus;
import com.cdac.enums.ItemType;
import com.cdac.enums.MatchStatus;
import com.cdac.enums.NotificationType;
import com.cdac.exception.InvalidRequestException;
import com.cdac.exception.ResourceNotFoundException;
import com.cdac.repository.ItemMatchRepository;
import com.cdac.repository.ItemRepository;
import com.cdac.service.ItemMatchService;
import com.cdac.service.NotificationService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import static com.cdac.constant.AppConstants.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemMatchServiceImpl implements ItemMatchService {

    private final ItemRepository itemRepository;
    private final ItemMatchRepository itemMatchRepository;
    private final NotificationService notificationService;
    
    @Override
    public void generateMatchesForItem(Long itemId) {

        // Fetch the newly created item
        Item sourceItem = getItemByIdOrThrow(itemId);

        // Only OPEN items should be matched
        if (sourceItem.getStatus() != ItemStatus.OPEN) {
            return;
        }

        // Find all opposite-type candidate items
        List<Item> candidateItems = findCandidateItems(sourceItem);

        // Compare with every candidate
        for (Item candidateItem : candidateItems) {

            // Prevent duplicate ItemMatch entries
            boolean alreadyExists;

            if (sourceItem.getItemType() == ItemType.LOST) {

                alreadyExists = itemMatchRepository.existsByLostItemAndFoundItem(
                        sourceItem,
                        candidateItem);

            } else {

                alreadyExists = itemMatchRepository.existsByLostItemAndFoundItem(
                        candidateItem,
                        sourceItem);
            }

            if (alreadyExists) {
                continue;
            }

            // Calculate matching score
            double score = calculateMatchScore(sourceItem, candidateItem);

            // Ignore weak matches
            if (score < MATCH_THRESHOLD) {
                continue;
            }

            // Create ItemMatch
            ItemMatch itemMatch = ItemMatch.builder()
                    .lostItem(
                            sourceItem.getItemType() == ItemType.LOST
                                    ? sourceItem
                                    : candidateItem)
                    .foundItem(
                            sourceItem.getItemType() == ItemType.FOUND
                                    ? sourceItem
                                    : candidateItem)
                    .matchScore(score)
                    .matchStatus(MatchStatus.PENDING)
                    .matchReason(buildMatchReason(sourceItem, candidateItem))
                    .build();

            saveMatch(itemMatch);
            //Notification implementation
            notificationService.createNotification(
                    itemMatch.getLostItem().getReportedBy(),
                    NotificationType.MATCH_FOUND,
                    "A potential match has been found for your lost item: "
                            + itemMatch.getLostItem().getTitle(),
                    itemMatch.getLostItem(),
                    itemMatch,
                    null
            );

            notificationService.createNotification(
                    itemMatch.getFoundItem().getReportedBy(),
                    NotificationType.MATCH_FOUND,
                    "A potential match has been found for your found item: "
                            + itemMatch.getFoundItem().getTitle(),
                    itemMatch.getFoundItem(),
                    itemMatch,
                    null
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemMatchSummaryResponse> getMatchesForLostItem(Long lostItemId) {

        Item lostItem = getItemByIdOrThrow(lostItemId);

        if (lostItem.getItemType() != ItemType.LOST) {
            throw new IllegalArgumentException("Item is not a lost item.");
        }

        return itemMatchRepository
                .findByLostItemOrderByMatchScoreDesc(lostItem)
                .stream()
                .map(match -> convertToSummaryResponse(
                        match,
                        match.getFoundItem()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemMatchSummaryResponse> getMatchesForFoundItem(Long foundItemId) {

        Item foundItem = getItemByIdOrThrow(foundItemId);

        if (foundItem.getItemType() != ItemType.FOUND) {
            throw new IllegalArgumentException("Item is not a found item.");
        }

        return itemMatchRepository
                .findByFoundItemOrderByMatchScoreDesc(foundItem)
                .stream()
                .map(match -> convertToSummaryResponse(
                        match,
                        match.getLostItem()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemMatchResponse getMatchById(Long matchId) {

        ItemMatch itemMatch = itemMatchRepository
                .findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found with id : " + matchId));

        return convertToItemMatchResponse(itemMatch);
    }

    @Override
    public void confirmMatch(Long matchId) {

        ItemMatch itemMatch = itemMatchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found with id : " + matchId));

        if (itemMatch.getMatchStatus() != MatchStatus.PENDING) {
            throw new InvalidRequestException(
                    "Only pending matches can be confirmed.");
        }

        itemMatch.setMatchStatus(MatchStatus.CONFIRMED);

        itemMatch.getLostItem().setStatus(ItemStatus.CLOSED);
        itemMatch.getFoundItem().setStatus(ItemStatus.CLOSED);

        itemRepository.save(itemMatch.getLostItem());
        itemRepository.save(itemMatch.getFoundItem());

        itemMatchRepository.save(itemMatch);

     // Notification module
        notificationService.createNotification(
                itemMatch.getLostItem().getReportedBy(),
                NotificationType.MATCH_APPROVED,
                "Your lost item match has been confirmed.",
                itemMatch.getLostItem(),
                itemMatch,
                null);

        notificationService.createNotification(
                itemMatch.getFoundItem().getReportedBy(),
                NotificationType.MATCH_APPROVED,
                "Your found item match has been confirmed.",
                itemMatch.getFoundItem(),
                itemMatch,
                null);
    }

    @Override
    public void rejectMatch(Long matchId) {

        ItemMatch itemMatch = itemMatchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found with id : " + matchId));

        if (itemMatch.getMatchStatus() != MatchStatus.PENDING) {
            throw new InvalidRequestException(
                    "Only pending matches can be rejected.");
        }

        itemMatch.setMatchStatus(MatchStatus.REJECTED);

        itemMatchRepository.save(itemMatch);

     // Notification module
        notificationService.createNotification(
                itemMatch.getLostItem().getReportedBy(),
                NotificationType.MATCH_REJECTED,
                "A match for your lost item has been rejected.",
                itemMatch.getLostItem(),
                itemMatch,
                null);

        notificationService.createNotification(
                itemMatch.getFoundItem().getReportedBy(),
                NotificationType.CLAIM_REJECTED,
                "A match for your found item has been rejected.",
                itemMatch.getFoundItem(),
                itemMatch,
                null);
    }
    
    //Helper Methods
    private void saveMatch(ItemMatch itemMatch) {
        itemMatchRepository.save(itemMatch);
    }
    
    private Item getItemByIdOrThrow(Long itemId) {

        return itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Item not found with id : " + itemId));
    }

    private List<Item> findCandidateItems(Item item) {

        ItemType candidateType;

        if (item.getItemType() == ItemType.LOST) {
            candidateType = ItemType.FOUND;
        } else {
            candidateType = ItemType.LOST;
        }

        return itemRepository.findByItemTypeAndStatus(
                candidateType,
                ItemStatus.OPEN)
                .stream()
                .filter(candidate ->
                        !candidate.getReportedBy().getId()
                                .equals(item.getReportedBy().getId()))
                .toList();
    }

    private double calculateMatchScore(Item sourceItem, Item candidateItem) {

        double score = 0.0;

        score += calculateCategoryScore(sourceItem, candidateItem);

        score += calculateBrandScore(sourceItem, candidateItem);

        score += calculateColorScore(sourceItem, candidateItem);

        score += calculateLocationScore(sourceItem, candidateItem);

        score += calculateDateScore(sourceItem, candidateItem);

        score += calculateDescriptionScore(sourceItem.getDescription(), candidateItem.getDescription());

        return score;
    }
    
    private double calculateCategoryScore(Item sourceItem, Item candidateItem) {

        if (sourceItem.getCategory() == null
                || candidateItem.getCategory() == null) {
            return 0;
        }

        return sourceItem.getCategory()
                .equalsIgnoreCase(candidateItem.getCategory())
                ? CATEGORY_WEIGHT
                : 0;
    }
    
    private double calculateBrandScore(Item sourceItem, Item candidateItem) {

        if (sourceItem.getBrand() == null
                || candidateItem.getBrand() == null
                || sourceItem.getBrand().isBlank()
                || candidateItem.getBrand().isBlank()) {
            return 0;
        }

        return sourceItem.getBrand()
                .equalsIgnoreCase(candidateItem.getBrand())
                ? BRAND_WEIGHT
                : 0;
    }
    
    private double calculateColorScore(Item sourceItem, Item candidateItem) {

        if (sourceItem.getColor() == null
                || candidateItem.getColor() == null
                || sourceItem.getColor().isBlank()
                || candidateItem.getColor().isBlank()) {
            return 0;
        }

        return sourceItem.getColor()
                .equalsIgnoreCase(candidateItem.getColor())
                ? COLOR_WEIGHT
                : 0;
    }
    
    private double calculateDateScore(Item sourceItem, Item candidateItem) {

        long days = Math.abs(
                java.time.temporal.ChronoUnit.DAYS.between(
                        sourceItem.getItemDate(),
                        candidateItem.getItemDate()));

        if (days == 0)
            return DATE_WEIGHT;

        if (days <= 1)
            return 8;

        if (days <= 3)
            return 5;

        return 0;
    }
    
    private double calculateLocationScore(Item sourceItem, Item candidateItem) {

        if (sourceItem.getLocationDescription() == null
                || candidateItem.getLocationDescription() == null) {
            return 0;
        }

        return sourceItem.getLocationDescription()
                .equalsIgnoreCase(candidateItem.getLocationDescription())
                ? LOCATION_WEIGHT
                : 0;
    }
    
    private double calculateDescriptionScore(String text1, String text2) {

        if (text1 == null || text2 == null) {
            return 0.0;
        }

        Set<String> words1 = Arrays.stream(text1.toLowerCase().split("\\W+"))
                .filter(word -> word.length() > 2)
                .collect(Collectors.toSet());

        Set<String> words2 = Arrays.stream(text2.toLowerCase().split("\\W+"))
                .filter(word -> word.length() > 2)
                .collect(Collectors.toSet());

        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }

        Set<String> commonWords = new HashSet<>(words1);
        commonWords.retainAll(words2);

        Set<String> allWords = new HashSet<>(words1);
        allWords.addAll(words2);

        double similarity = (double) commonWords.size() / allWords.size();

        return similarity * DESCRIPTION_WEIGHT;
    }

    private String buildMatchReason(Item sourceItem, Item candidateItem) {

        StringBuilder reason = new StringBuilder();

        if (sourceItem.getCategory() != null
                && candidateItem.getCategory() != null
                && sourceItem.getCategory().equalsIgnoreCase(candidateItem.getCategory())) {

            reason.append("Category matched, ");
        }

        if (sourceItem.getBrand() != null
                && candidateItem.getBrand() != null
                && sourceItem.getBrand().equalsIgnoreCase(candidateItem.getBrand())) {

            reason.append("Brand matched, ");
        }

        if (sourceItem.getColor() != null
                && candidateItem.getColor() != null
                && sourceItem.getColor().equalsIgnoreCase(candidateItem.getColor())) {

            reason.append("Color matched, ");
        }

        if (sourceItem.getLocationDescription() != null
                && candidateItem.getLocationDescription() != null
                && sourceItem.getLocationDescription()
                        .equalsIgnoreCase(candidateItem.getLocationDescription())) {

            reason.append("Location matched, ");
        }

        long days = Math.abs(
                java.time.temporal.ChronoUnit.DAYS.between(
                        sourceItem.getItemDate(),
                        candidateItem.getItemDate()));

        if (days <= 3) {
            reason.append("Date proximity, ");
        }

        if (reason.length() > 2) {
            reason.setLength(reason.length() - 2);
        }

        return reason.toString();
    }

    private ItemMatchResponse convertToItemMatchResponse(ItemMatch itemMatch) {

        return ItemMatchResponse.builder()
                .itemMatchId(itemMatch.getId())

                .lostItemId(itemMatch.getLostItem().getId())
                .lostItemTitle(itemMatch.getLostItem().getTitle())

                .foundItemId(itemMatch.getFoundItem().getId())
                .foundItemTitle(itemMatch.getFoundItem().getTitle())

                .matchScore(itemMatch.getMatchScore())

                .matchStatus(itemMatch.getMatchStatus())

                .matchReason(itemMatch.getMatchReason())

                .createdAt(itemMatch.getCreatedAt())

                .build();
    }

    private ItemMatchSummaryResponse convertToSummaryResponse(ItemMatch itemMatch, Item displayItem) {

        return ItemMatchSummaryResponse.builder()
                .itemMatchId(itemMatch.getId())
                .itemId(displayItem.getId())
                .itemTitle(displayItem.getTitle())
                .matchScore(itemMatch.getMatchScore())
                .matchStatus(itemMatch.getMatchStatus())
                .build();
    }

}

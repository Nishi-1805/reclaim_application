package com.cdac.service;

import java.util.List;

import com.cdac.dto.request.CreateItemRequest;
import com.cdac.dto.request.UpdateItemRequest;
import com.cdac.dto.response.ItemResponse;
import com.cdac.enums.ItemType;

public interface ItemService {

    ItemResponse createItem(CreateItemRequest request);

    ItemResponse getItemById(Long itemId);

    List<ItemResponse> getAllItems();

    List<ItemResponse> getMyItems();

    ItemResponse updateItem(Long itemId, UpdateItemRequest request);
    
    List<ItemResponse> getItemsByType(ItemType itemType);

    void deleteItem(Long itemId);

}
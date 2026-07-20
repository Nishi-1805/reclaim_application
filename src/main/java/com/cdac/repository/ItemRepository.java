package com.cdac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.entity.Item;
import com.cdac.entity.User;
import com.cdac.enums.ItemStatus;
import com.cdac.enums.ItemType;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByReportedByOrderByCreatedAtDesc(User reportedBy);

    List<Item> findByItemType(ItemType itemType);

    List<Item> findByItemTypeAndStatus(ItemType itemType, ItemStatus status);

    List<Item> findByReportedByAndItemTypeOrderByCreatedAtDesc(User reportedBy, ItemType itemType);

    List<Item> findByReportedByAndStatusOrderByCreatedAtDesc(User reportedBy, ItemStatus status);

    List<Item> findByReportedByAndItemTypeAndStatusOrderByCreatedAtDesc(User reportedBy, ItemType itemType, ItemStatus status);
}
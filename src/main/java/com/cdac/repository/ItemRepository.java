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
	
	List<Item> findByStatusIn(List<ItemStatus> statuses);
	
	//List<Item> findByReportedBy(User reportedBy);

    List<Item> findByReportedByOrderByCreatedAtDesc(User reportedBy);

    List<Item> findByItemTypeAndStatusOrderByCreatedAtDesc(ItemType itemType, ItemStatus status);

    List<Item> findByItemTypeAndStatus(ItemType itemType, ItemStatus status);

    List<Item> findByReportedByAndItemTypeOrderByCreatedAtDesc(User reportedBy, ItemType itemType);

    List<Item> findByReportedByAndStatusOrderByCreatedAtDesc(User reportedBy, ItemStatus status);

    List<Item> findByReportedByAndItemTypeAndStatusOrderByCreatedAtDesc(User reportedBy, ItemType itemType, ItemStatus status);
    
    List<Item> findByStatusOrderByCreatedAtDesc(ItemStatus status);
    
    long countByReportedBy(User reportedBy);

    long countByReportedByAndItemType(User reportedBy, ItemType itemType);
}
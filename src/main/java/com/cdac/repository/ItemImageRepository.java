package com.cdac.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.entity.Item;
import com.cdac.entity.ItemImage;

@Repository
public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {

    List<ItemImage> findByItemOrderByDisplayOrderAsc(Item item);

    Optional<ItemImage> findByIdAndItem(Long id, Item item);

    boolean existsByItemAndDisplayOrder(Item item, Integer displayOrder);

    Optional<ItemImage> findByItemAndDisplayOrder(Item item, Integer displayOrder);
}
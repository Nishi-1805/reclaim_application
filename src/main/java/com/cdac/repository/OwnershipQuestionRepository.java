package com.cdac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.entity.Item;
import com.cdac.entity.OwnershipQuestion;

@Repository
public interface OwnershipQuestionRepository extends JpaRepository<OwnershipQuestion, Long> {

    List<OwnershipQuestion> findByItemOrderByDisplayOrderAsc(Item item);

  //  Optional<OwnershipQuestion> findByIdAndItem(Long id, Item item);

  //  boolean existsByItemAndDisplayOrder(Item item, Integer displayOrder);

  //  Optional<OwnershipQuestion> findByItemAndDisplayOrder(Item item, Integer displayOrder);
}
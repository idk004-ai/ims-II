package com.khoilnm.ims.repository;

import com.khoilnm.ims.model.Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MasterRepository extends JpaRepository<Master, Integer> {

    @Query(value = "SELECT m FROM Master m WHERE m.category = :category AND m.categoryValue = :categoryValue")
    Optional<Master> findByCategoryAndValue(String category, String categoryValue);

    @Query(value = "SELECT MAX(m.categoryId) FROM Master m WHERE m.category = :category")
    Optional<Integer> findMaxCategoryId(@Param("category") String category);
}

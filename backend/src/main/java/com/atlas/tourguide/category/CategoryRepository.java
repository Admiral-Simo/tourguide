package com.atlas.tourguide.category;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
  @Query("SELECT c FROM Category c LEFT JOIN FETCH c.posts")
  List<Category> findAllWithPostCount();
  boolean existsByNameIgnoreCase(String name);
}

package com.atlas.tourguide.tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {
  @Query("SELECT c FROM Tag c LEFT JOIN FETCH c.posts")
  List<Tag> findAllWithPostCount();

  List<Tag> findByNameIn(Set<String> names);
}

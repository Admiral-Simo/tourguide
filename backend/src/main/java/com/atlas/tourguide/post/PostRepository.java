package com.atlas.tourguide.post;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.atlas.tourguide.category.Category;
import com.atlas.tourguide.tag.Tag;
import com.atlas.tourguide.user.User;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
  List<Post> findAllByStatusAndCategoryAndTagsContaining(PostStatus status, Category category,
      Tag tag);
  List<Post> findAllByStatusAndCategory(PostStatus status, Category category);
  List<Post> findAllByStatusAndTagsContaining(PostStatus status, Tag tag);
  List<Post> findAllByStatus(PostStatus status);
  List<Post> findAllByAuthorAndStatus(User author, PostStatus status);
}

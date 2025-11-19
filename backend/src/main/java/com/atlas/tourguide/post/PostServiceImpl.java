package com.atlas.tourguide.post;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atlas.tourguide.category.Category;
import com.atlas.tourguide.tag.Tag;
import com.atlas.tourguide.user.User;
import com.atlas.tourguide.category.CategoryService;
import com.atlas.tourguide.tag.TagService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final CategoryService categoryService;
  private final TagService tagService;
  private static final int WORDS_PER_MINUTE = 200;

  @Transactional(readOnly = true)
  @Override
  public List<Post> getAllPosts(UUID categoryId, UUID tagId) {
    if (categoryId != null && tagId != null) {
      Category category = categoryService.getCategoryById(categoryId);
      Tag tag = tagService.getTagById(tagId);
      return postRepository.findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED,
          category, tag);
    }
    if (categoryId != null) {
      Category category = categoryService.getCategoryById(categoryId);
      return postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, category);
    }
    if (tagId != null) {
      Tag tag = tagService.getTagById(tagId);
      return postRepository.findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag);
    }
    return postRepository.findAllByStatus(PostStatus.PUBLISHED);
  }

  @Override
  public List<Post> getDraftPosts(User user) {
    return postRepository.findAllByAuthorAndStatus(user, PostStatus.DRAFT);
  }

  @Transactional
  @Override
  public Post createPost(User user, CreatePostRequest createPostRequest) {
    Post newPost = new Post();
    newPost.setTitle(createPostRequest.getTitle());
    String postContent = createPostRequest.getContent();

    newPost.setContent(postContent);
    newPost.setStatus(createPostRequest.getStatus());
    newPost.setAuthor(user);
    newPost.setReadingTime(calculateReadingTime(postContent));
    newPost.setLatitude(createPostRequest.getLatitude());
    newPost.setLongitude(createPostRequest.getLongitude());

    Category category = categoryService.getCategoryById(createPostRequest.getCategoryId());
    newPost.setCategory(category);

    Set<UUID> tagIds = createPostRequest.getTagIds();
    List<Tag> tags = tagService.getTagByIds(tagIds);
    newPost.setTags(new HashSet<>(tags));

    return postRepository.save(newPost);
  }

  private Integer calculateReadingTime(String content) {
    if (content == null || content.isEmpty()) {
      return 0;
    }
    int wordCount = content.trim().split("\\s+").length;
    return Math.ceilDiv(wordCount, WORDS_PER_MINUTE);
  }

  @Transactional
  @Override
  public Post updatePost(UUID id, UpdatePostRequest updatePostRequest) {
    Post existingPost = postRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Post does not exist with id " + id));
    existingPost.setTitle(updatePostRequest.getTitle());
    String postContent = updatePostRequest.getContent();
    existingPost.setContent(postContent);
    existingPost.setStatus(updatePostRequest.getStatus());
    existingPost.setReadingTime(calculateReadingTime(postContent));
    existingPost.setLatitude(updatePostRequest.getLatitude());
    existingPost.setLongitude(updatePostRequest.getLongitude());

    UUID updatePostRequestCategoryId = updatePostRequest.getCategoryId();
    if (!existingPost.getCategory().getId().equals(updatePostRequestCategoryId)) {
      Category newCategory = categoryService.getCategoryById(updatePostRequestCategoryId);
      existingPost.setCategory(newCategory);
    }

    Set<UUID> existingTagIds = existingPost.getTags().stream().map(Tag::getId)
        .collect(Collectors.toSet());

    Set<UUID> updatePostRequestTagIds = updatePostRequest.getTagIds();
    if (!existingTagIds.equals(updatePostRequestTagIds)) {
      List<Tag> newTags = tagService.getTagByIds(updatePostRequestTagIds);
      existingPost.setTags(new HashSet<>(newTags));
    }
    return postRepository.save(existingPost);
  }

  @Override
  public Post getPost(UUID id) {
    return postRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Post does not exist with id " + id));
  }

  @Override
  public void deletePost(UUID id) {
    Post post = getPost(id);
    postRepository.delete(post);
  }
}

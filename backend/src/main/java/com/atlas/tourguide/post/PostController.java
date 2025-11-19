package com.atlas.tourguide.post;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atlas.tourguide.post.dtos.CreatePostRequestDto;
import com.atlas.tourguide.post.dtos.PostDto;
import com.atlas.tourguide.post.dtos.UpdatePostRequestDto;
import com.atlas.tourguide.user.User;
import com.atlas.tourguide.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
  private final PostService postService;
  private final UserService userService;
  private final PostMapper postMapper;

  @GetMapping
  public ResponseEntity<List<PostDto>> getAllPosts(@RequestParam(required = false) UUID categoryId,
      @RequestParam(required = false) UUID tagId) {
    List<Post> posts = postService.getAllPosts(categoryId, tagId);
    List<PostDto> postDtos = posts.stream().map(postMapper::toDto).toList();
    return ResponseEntity.ok(postDtos);
  }

  @GetMapping("/drafts")
  public ResponseEntity<List<PostDto>> getDraftPosts(@RequestAttribute UUID userId) {
    User loggedInUser = userService.getUserById(userId);
    List<Post> drafPosts = postService.getDraftPosts(loggedInUser);
    List<PostDto> postDtos = drafPosts.stream().map(postMapper::toDto).toList();
    return ResponseEntity.ok(postDtos);
  }

  @PostMapping
  public ResponseEntity<PostDto> createPost(
      @Valid @RequestBody CreatePostRequestDto createPostRequestDto,
      @RequestAttribute UUID userId) {
    User loggedInUser = userService.getUserById(userId);
    CreatePostRequest createPostRequest = postMapper.toCreatePostRequest(createPostRequestDto);
    Post createdPost = postService.createPost(loggedInUser, createPostRequest);
    PostDto createdPostDto = postMapper.toDto(createdPost);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdPostDto);
  }

  @PutMapping("/{id}")
  public ResponseEntity<PostDto> updatePost(@PathVariable UUID id,
      @Valid @RequestBody UpdatePostRequestDto updatePostRequestDto) {
    UpdatePostRequest updatePostRequest = postMapper.toUpdatePostRequest(updatePostRequestDto);
    Post updatePost = postService.updatePost(id, updatePostRequest);
    PostDto updatedPostDto = postMapper.toDto(updatePost);
    return ResponseEntity.ok(updatedPostDto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PostDto> getPost(@PathVariable UUID id) {
    Post post = postService.getPost(id);
    PostDto postDto = postMapper.toDto(post);
    return ResponseEntity.ok(postDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
    postService.deletePost(id);
    return ResponseEntity.noContent().build();
  }
}

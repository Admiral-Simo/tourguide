package com.atlas.tourguide.post;

import com.atlas.tourguide.post.dtos.CreatePostRequestDto;
import com.atlas.tourguide.post.dtos.PostDto;
import com.atlas.tourguide.shared.exception.ErrorController;
import com.atlas.tourguide.user.User;
import com.atlas.tourguide.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostController Unit Tests")
class PostControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private PostService postService;
    @Mock
    private UserService userService;
    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostController postController;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setControllerAdvice(new ErrorController())
                .build();

        testUserId = UUID.randomUUID();
        testUser = User.builder().id(testUserId).name("Test User").build();
    }

    @Test
    @DisplayName("✅ [GET /posts] should return a list of posts and 200 OK")
    void getAllPosts_ShouldReturnListOfPosts() throws Exception {
        // Arrange
        Post post = new Post();
        PostDto postDto = PostDto.builder().title("Test Post").build();
        when(postService.getAllPosts(null, null)).thenReturn(List.of(post));
        when(postMapper.toDto(any(Post.class))).thenReturn(postDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Post"));
    }

    @Test
    @DisplayName("✅ [GET /posts/drafts] should return user's draft posts when authenticated")
    void getDraftPosts_WhenAuthenticated_ShouldReturnDrafts() throws Exception {
        // Arrange
        when(userService.getUserById(testUserId)).thenReturn(testUser);
        when(postService.getDraftPosts(testUser)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/drafts")
                        .requestAttr("userId", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("✅ [POST /posts] should create a post and return 201 Created when data is valid")
    void createPost_WhenDataIsValid_ShouldReturnCreated() throws Exception {
        // Arrange
        CreatePostRequestDto requestDto = CreatePostRequestDto.builder()
                .title("A Valid Title").content("This is some valid content that is long enough.")
                .categoryId(UUID.randomUUID()).status(PostStatus.DRAFT)
                .latitude(1.0).longitude(1.0).build();

        CreatePostRequest mappedRequest = new CreatePostRequest();
        Post savedPost = new Post();
        PostDto responseDto = PostDto.builder().id(UUID.randomUUID()).title("A Valid Title").build();

        when(userService.getUserById(testUserId)).thenReturn(testUser);
        when(postMapper.toCreatePostRequest(requestDto)).thenReturn(mappedRequest);
        when(postService.createPost(testUser, mappedRequest)).thenReturn(savedPost);
        when(postMapper.toDto(savedPost)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/posts")
                        .requestAttr("userId", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("A Valid Title"));
    }

    @Test
    @DisplayName("❌ [POST /posts] should return 400 Bad Request when data is invalid")
    void createPost_WhenDataIsInvalid_ShouldReturnBadRequest() throws Exception {
        // Arrange: DTO with multiple validation violations
        CreatePostRequestDto requestDto = CreatePostRequestDto.builder()
                .title("") // Invalid: violates @NotBlank and @Size
                .content("This content is valid.") // This is valid
                .status(null) // Invalid: violates @NotNull
                // categoryId, latitude, and longitude are also null and violate @NotNull
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/posts")
                        .requestAttr("userId", testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                // Correctly expect 6 errors for the 6 violations
                .andExpect(jsonPath("$.errors", hasSize(6)))
                .andExpect(jsonPath("$.errors[?(@.field == 'title')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'status')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'categoryId')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'latitude')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'longitude')]").exists());
    }

    @Test
    @DisplayName("✅ [GET /posts/{id}] should return a post when ID exists")
    void getPost_WhenIdExists_ShouldReturnPost() throws Exception {
        // Arrange
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        PostDto postDto = PostDto.builder().id(postId).title("Found Post").build();
        when(postService.getPost(postId)).thenReturn(post);
        when(postMapper.toDto(post)).thenReturn(postDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId.toString()));
    }

    @Test
    @DisplayName("❌ [GET /posts/{id}] should return 404 Not Found when ID does not exist")
    void getPost_WhenIdDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        UUID postId = UUID.randomUUID();
        String errorMessage = "Post does not exist with id " + postId;
        when(postService.getPost(postId)).thenThrow(new EntityNotFoundException(errorMessage));

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/{id}", postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @DisplayName("✅ [DELETE /posts/{id}] should return 204 No Content")
    void deletePost_ShouldReturnNoContent() throws Exception {
        // Arrange
        UUID postId = UUID.randomUUID();
        doNothing().when(postService).deletePost(postId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/posts/{id}", postId))
                .andExpect(status().isNoContent());
        verify(postService, times(1)).deletePost(postId);
    }
}
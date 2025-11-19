package com.atlas.tourguide.tag;

import com.atlas.tourguide.shared.exception.ErrorController;
import com.atlas.tourguide.tag.dtos.CreateTagsRequestDto;
import com.atlas.tourguide.tag.dtos.TagDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagController Unit Tests")
class TagControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TagService tagService;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagController tagController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tagController)
                .setControllerAdvice(new ErrorController())
                .build();
    }

    @Test
    @DisplayName("✅ [GET /tags] should return a list of tag DTOs and 200 OK")
    void getAllTags_ShouldReturnListOfTags() throws Exception {
        // Arrange
        Tag tag1 = Tag.builder().id(UUID.randomUUID()).name("Java").build();
        TagDto tagDto1 = new TagDto(tag1.getId(), tag1.getName(), 10);

        when(tagService.getTags()).thenReturn(List.of(tag1));
        when(tagMapper.toTagResponse(tag1)).thenReturn(tagDto1);

        // Act & Assert
        mockMvc.perform(get("/api/v1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].postCount").value(10));

        verify(tagService, times(1)).getTags();
    }

    @Test
    @DisplayName("✅ [POST /tags] should create tags and return 201 Created with a list of tags")
    void createTags_WithValidRequest_ShouldReturnCreated() throws Exception {
        // Arrange
        CreateTagsRequestDto requestDto = new CreateTagsRequestDto(Set.of("Java", "Spring"));
        Tag savedTag = Tag.builder().id(UUID.randomUUID()).name("Java").build();
        TagDto responseDto = new TagDto(savedTag.getId(), savedTag.getName(), 0);

        when(tagService.createTags(requestDto.getNames())).thenReturn(List.of(savedTag));
        when(tagMapper.toTagResponse(any(Tag.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Java"));
    }

    @Test
    @DisplayName("✅ [DELETE /tags/{id}] should delete tag and return 204 No Content")
    void deleteTag_WhenExists_ShouldReturnNoContent() throws Exception {
        // Arrange
        UUID tagId = UUID.randomUUID();
        doNothing().when(tagService).deleteTag(tagId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tags/{id}", tagId))
                .andExpect(status().isNoContent());

        verify(tagService, times(1)).deleteTag(tagId);
    }

    @Test
    @DisplayName("❌ [DELETE /tags/{id}] should return 409 Conflict when tag is in use")
    void deleteTag_WhenTagIsInUse_ShouldReturnConflict() throws Exception {
        // Arrange
        UUID tagId = UUID.randomUUID();
        doThrow(new IllegalStateException("Cannot delete tag with posts"))
                .when(tagService).deleteTag(tagId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/tags/{id}", tagId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Cannot delete tag with posts"));
    }
}
package com.atlas.tourguide.tag;

import java.util.List;
import java.util.UUID;

import com.atlas.tourguide.tag.dtos.CreateTagsRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atlas.tourguide.tag.dtos.TagDto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {
  private final TagMapper tagMapper;
  private final TagService tagService;

  @GetMapping
  public ResponseEntity<List<TagDto>> getAllTags() {
    List<Tag> tags = tagService.getTags();
    List<TagDto> tagResponses = tags.stream().map(tagMapper::toTagResponse).toList();
    return ResponseEntity.ok(tagResponses);
  }

  @PostMapping
  public ResponseEntity<List<TagDto>> createTags(
      @RequestBody CreateTagsRequestDto createTagsRequestDto) {
    List<Tag> savedTags = tagService.createTags(createTagsRequestDto.getNames());
    List<TagDto> tagResponses = savedTags.stream().map(tagMapper::toTagResponse).toList();
    return ResponseEntity.status(HttpStatus.CREATED).body(tagResponses);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> createTag(@PathVariable UUID id) {
    tagService.deleteTag(id);
    return ResponseEntity.noContent().build();
  }
}

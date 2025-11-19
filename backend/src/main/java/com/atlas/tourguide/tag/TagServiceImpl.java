package com.atlas.tourguide.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
  private final TagRepository tagRepository;

  @Override
  public List<Tag> getTags() {
    return tagRepository.findAllWithPostCount();
  }

  @Transactional
  @Override
  public List<Tag> createTags(Set<String> tagNames) {
    // Identify existing tags and new tags
    List<Tag> existingTags = tagRepository.findByNameIn(tagNames);
    Set<String> existingTagsNames = existingTags.stream().map(Tag::getName)
        .collect(Collectors.toSet());
    List<Tag> newTags = tagNames.stream().filter(name -> !existingTagsNames.contains(name))
        .map(name -> Tag.builder().name(name).build()).collect(Collectors.toList());

    // Save new tags
    List<Tag> savedTags = new ArrayList<>();
    if (!newTags.isEmpty()) {
      savedTags = tagRepository.saveAll(newTags);
    }

    // Combine existing and newly saved tags and return them as a result
    List<Tag> result = new ArrayList<>(savedTags);
    result.addAll(existingTags);

    return result;
  }

  @Transactional
  @Override
  public void deleteTag(UUID id) {
    tagRepository.findById(id).ifPresent(tag -> {
      if (!tag.getPosts().isEmpty()) {
        throw new IllegalStateException("Cannot delete tag with posts");
      }
      tagRepository.deleteById(id);
    });
  }

  @Override
  public Tag getTagById(UUID id) {
    return tagRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
  }

  @Override
  public List<Tag> getTagByIds(Set<UUID> ids) {
    List<Tag> foundTags = tagRepository.findAllById(ids);
    if (foundTags.size() != ids.size()) {
      throw new EntityNotFoundException("Not all specified tag IDs exist");
    }
    return foundTags;
  }
}

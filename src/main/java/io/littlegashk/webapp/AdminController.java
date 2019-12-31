package io.littlegashk.webapp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import io.littlegashk.webapp.entity.EntryType;
import io.littlegashk.webapp.rentity.Reference;
import io.littlegashk.webapp.rentity.ReferenceRepository;
import io.littlegashk.webapp.rentity.Tag;
import io.littlegashk.webapp.rentity.TagRepository;
import io.littlegashk.webapp.rentity.Topic;
import io.littlegashk.webapp.rentity.TopicRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Log4j2
public class AdminController {

  @Autowired
  TopicRepository topicRepository;

  @Autowired
  ReferenceRepository referenceRepository;

  @Autowired
  TagRepository tagRepository;

  @Operation(description = "insert topic")
  @PostMapping(value = "/topics", consumes = "application/json")
  public ResponseEntity<Topic> addTopic(@RequestBody Topic topic) {

    topic.setType(EntryType.TOPIC);
    topic.setLastUpdated(topic.getEventDate()
                              .toEpochSecond(ZoneOffset.ofHours(8)) * 1000);
    updateReference(topic);
    final Topic savedTopic = topicRepository.save(topic);
    saveTags(savedTopic);

    return ResponseEntity.ok(savedTopic);
  }

  private void updateReference(@RequestBody Topic topic) {
    Set<Reference> references = topic.getReferences()
                                     .stream()
                                     .map(r -> referenceRepository.findById(r.getHash())
                                                                  .orElse(r))
                                     .collect(Collectors.toSet());
    topic.setReferences(references);
  }

  @Operation(description = "insert a progress")
  @PostMapping(value = "/topics/{parentTopicId}/progress", consumes = "application/json")
  public ResponseEntity<Topic> addProgress(HttpServletRequest req, @PathVariable String parentTopicId, @RequestBody Topic topic) {
    return addSubTopic(parentTopicId, topic, EntryType.PROGRESS);
  }

  @Operation(description = "insert a public response")
  @PostMapping(value = "/topics/{parentTopicId}/response", consumes = "application/json")
  public ResponseEntity<Topic> addResponse(HttpServletRequest req, @PathVariable String parentTopicId, @RequestBody Topic topic) {

    return addSubTopic(parentTopicId, topic, EntryType.RESPONSE);
  }

  private ResponseEntity<Topic> addSubTopic(@PathVariable String parentTopicId, @RequestBody Topic topic, EntryType response) {
    Topic parent = topicRepository.findById(UUID.fromString(parentTopicId))
                                  .orElse(null);
    if (null == parent) {
      return ResponseEntity.badRequest()
                           .build();
    }
    topic.setType(response);
    topic.setLastUpdated(topic.getEventDate()
                              .toEpochSecond(ZoneOffset.ofHours(8)) * 1000);
    updateReference(topic);
    Topic savedTopic = topicRepository.save(topic);
    saveTags(savedTopic);
    parent.getChildren()
          .add(savedTopic);
    parent.setLastUpdated(Math.max(parent.getLastUpdated(), topic.getLastUpdated()));
    topicRepository.save(parent);
    return ResponseEntity.ok(savedTopic);
  }


  void findAndAddRelatedTopic(Topic topic) {

    final Multimap<Topic, String> topicToTag = ArrayListMultimap.create();
    for (String t : topic.getTags()) {
      topicRepository.findAllByTagRecordIn(List.of(new Tag().setTag(t)))
                     .forEach(tp -> topicToTag.put(tp, t));
    }

    TreeMultimap<Integer, Topic> sorted = TreeMultimap.create(Comparator.reverseOrder(), Comparator.comparing(Topic::getEventDate)
                                                                                                   .reversed());
    for (Map.Entry<Topic, Collection<String>> entry : topicToTag.asMap()
                                                                .entrySet()) {
      sorted.put(entry.getValue()
                      .size(), entry.getKey());
    }
    int i = 0;
    for (Map.Entry<Integer, Topic> entry : sorted.entries()) {
      if (i == 5) {
        break;
      }
      topic.getRelatedTopics()
           .add(entry.getValue());
      i++;
    }
  }


  @Operation(description = "delete a topic")
  @DeleteMapping("/topics/{topicId}")
  public ResponseEntity<?> deleteTopic(@PathVariable String topicId) {
    topicRepository.findById(UUID.fromString(topicId))
                   .ifPresent(value -> topicRepository.delete(value));
    return new ResponseEntity<>(topicId, HttpStatus.OK);
  }


  @Operation(description = "edit topic")
  @PutMapping(value = "/topics", consumes = "application/json")
  public ResponseEntity<Topic> editTopic(@RequestBody Topic topic) {
    //check parent exist
    if (!topicRepository.existsById(topic.getTopicId())) {
      return ResponseEntity.badRequest()
                           .build();
    }
    Topic db = topicRepository.getOne(topic.getTopicId());

    db.setTitle(topic.getTitle());
    db.setSummary(topic.getSummary());
    db.setTags(topic.getTags());
    db.setReferences(topic.getReferences());
    db.setRelatedTopics(topic.getRelatedTopics());
    db.setImageUrl(topic.getImageUrl());
    db.setUid(topic.getUid());
    db.getRelatedTopics()
      .clear();
    db.setReferences(topic.getReferences());
    db.setCategory(topic.getCategory());
    findAndAddRelatedTopic(db);
    updateReference(db);
    Topic savedTopic = topicRepository.save(db);
    saveTags(savedTopic);
    return ResponseEntity.ok(savedTopic);
  }


  @Operation(description = "change topic sequence")
  @PutMapping("/topics/{topicId}/sequence")
  public ResponseEntity<?> changeSequence(HttpServletRequest req, @PathVariable String topicId, @RequestParam Integer sequence) {

    Optional<Topic> optionalTopic = topicRepository.findById(UUID.fromString(topicId));
    if (optionalTopic.isPresent()) {
      Topic target = optionalTopic.get();
      target.setSeq(sequence);
      topicRepository.save(target);
      return ResponseEntity.ok(null);
    } else {
      return ResponseEntity.notFound()
                           .build();
    }
  }

  @Operation(description = "update event date")
  @PostMapping("/topics/{oldTopicId}/changeDate")
  public ResponseEntity<Topic> changeDate(@PathVariable String oldTopicId, @RequestParam String newDate) {
    Optional<Topic> optionalTopic = topicRepository.findById(UUID.fromString(oldTopicId));
    if (optionalTopic.isPresent()) {
      Topic target = optionalTopic.get();
      target.setEventDate(LocalDateTime.parse(newDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      topicRepository.save(target);
      return ResponseEntity.ok(null);
    } else {
      return ResponseEntity.notFound()
                           .build();
    }
  }

  @Operation(description = "migrateTag")
  @PutMapping("/tags/{oldTag}/migrate")
  public ResponseEntity<?> migrateTag(@PathVariable String oldTag, @RequestParam String newTag) {
    List<Topic> topics = topicRepository.findAllByTagRecordIn(List.of(new Tag().setTag(oldTag)));
    topics.forEach(t -> {
      String[] tags = t.getTags();
      tags = ArrayUtils.removeElement(tags, oldTag);
      tags = ArrayUtils.add(tags, newTag);
      t.setTags(tags);
      saveTags(t);
    });
    tagRepository.deleteById(oldTag);
    return ResponseEntity.ok(null);
  }

  @Operation(hidden = true)
  @PutMapping("/tags/fix")
  public ResponseEntity<?> fixTags() {
    List<Topic> topics = topicRepository.findAll();
    topics.forEach(this::saveTags);
    return ResponseEntity.ok(null);
  }


  private void saveTags(Topic updated) {

    if (updated.getTags() != null && updated.getTags().length > 0) {
      Set<Tag> tagSet = Arrays.stream(updated.getTags())
                              .map(s -> tagRepository.findById(s)
                                                     .orElse(new Tag().setTag(s)))
                              .collect(Collectors.toSet());
      tagRepository.saveAll(tagSet);
      updated.setTagRecord(tagSet);
      topicRepository.save(updated);
    }
  }


}

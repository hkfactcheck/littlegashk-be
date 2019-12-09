package io.littlegashk.webapp;

import io.littlegashk.webapp.entity.EntryType;
import io.littlegashk.webapp.rentity.Reference;
import io.littlegashk.webapp.rentity.ReferenceRepository;
import io.littlegashk.webapp.rentity.Topic;
import io.littlegashk.webapp.rentity.TopicRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/topics")
@Log4j2
public class TopicController {


  @Autowired
  TopicRepository topicRepository;


  @Autowired
  ReferenceRepository referenceRepository;


  @Operation(description = "get all topics, sorted by lastUpdated date desc")
  @GetMapping
  public ResponseEntity<Page<Topic>> getTopics(@RequestParam(required = false) Long lastUpdated,
                                               @RequestParam(required = false, defaultValue = "0") Integer page) {

    Page<Topic> allTopic = topicRepository.findByTypeAndLastUpdatedBeforeOrderByLastUpdatedDesc(EntryType.TOPIC,
                                                                                                lastUpdated == null ? Long.MAX_VALUE : lastUpdated,
                                                                                                PageRequest.of(page, 10));
    return ResponseEntity.ok(allTopic);
  }

  @Operation(description = "get all topics with specified date, sorted by topicId desc")
  @GetMapping("/date/{date}")
  public ResponseEntity<Map<String, Object>> getTopicsByDate(@Parameter(example = "2019-08-01") @PathVariable String date) {

    Map<String, Object> result = new HashMap<>();
    result.put("date", date);
    LocalDateTime start = LocalDateTime.parse(date + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    LocalDateTime end = LocalDateTime.parse(date + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    List<Topic> allTopic = topicRepository.findAllByEventDateBetweenOrderByEventDateDesc(start, end);
    result.put("topics", allTopic);
    return ResponseEntity.ok(result);
  }


  @Operation(description = "get all topics/progresses/responses with specified url")
  @GetMapping("/url")
  public ResponseEntity<List<Topic>> getTopicsByUrl(@Parameter(example = "https://google.com") @RequestParam String url) {
    Optional<Reference> reference = referenceRepository.findById(DigestUtils.md5Hex(url));
    if (reference.isEmpty()) {
      return ResponseEntity.ok(new ArrayList<>());
    } else {
      return ResponseEntity.ok(new ArrayList<>(reference.get()
                                                        .getTopics()));
    }
  }

  @Operation(description = "return all TOP LEVEL topics with children containing the given URL ")
  @GetMapping("/top-level-by-url")
  public ResponseEntity<List<Topic>> getTopLevelByUrl(@Parameter(example = "https://google.com") @RequestParam String url) {

    Optional<Reference> reference = referenceRepository.findById(DigestUtils.md5Hex(url));
    if (reference.isEmpty()) {
      return ResponseEntity.ok(new ArrayList<>());
    } else {
      Set<Topic> topLevel = new HashSet<>();
      reference.get()
               .getTopics()
               .stream()
               .forEach(t -> {
                 if (t.getType() == EntryType.TOPIC) {
                   topLevel.add(t);
                 } else {
                   topLevel.addAll(t.getParents());
                 }
               });
      return ResponseEntity.ok(new ArrayList<>(topLevel));
    }
  }

  @Operation(description = "get specific topic with topicId")
  @GetMapping("/{topicId}")
  public ResponseEntity<Topic> getTopicByTopicId(@Parameter(example = "95ad3949-b189-4d71-9d64-4264063d6ce8") @PathVariable String topicId) {

    Optional<Topic> topic = topicRepository.findById(UUID.fromString(topicId));
    if (topic.isEmpty()) {
      return ResponseEntity.notFound()
                           .build();
    } else {
      return ResponseEntity.ok(topic.get());
    }
  }

  @Operation(description = "get topic progresses")
  @GetMapping("/{topicId}/progress")
  public ResponseEntity<Page<Topic>> getAllTopicProgress(@Parameter(example = "95ad3949-b189-4d71-9d64-4264063d6ce8") @PathVariable String topicId,
                                                         @RequestParam(required = false, defaultValue = "9999") String lastChildId,
                                                         @Parameter(example = "0") @RequestParam(required = false, defaultValue = "0") Integer page) {

    Optional<Topic> topic = topicRepository.findById(UUID.fromString(topicId));
    if (topic.isEmpty()) {
      return ResponseEntity.notFound()
                           .build();
    } else {
      Page<Topic> progresses = topicRepository.findTopicsByParentsContainsAndTypeOrderByEventDateDesc(topic.get(), EntryType.PROGRESS,
                                                                                                      PageRequest.of(page, 10));
      return ResponseEntity.ok(progresses);
    }
  }

  @Operation(description = "get public responses on a topic")
  @GetMapping("/{topicId}/response")
  public ResponseEntity<Page<Topic>> getAllTopicResponse(@Parameter(example = "2019-08-01|1565877016020") @PathVariable String topicId,
                                                         @RequestParam(required = false, defaultValue = "9999") String lastChildId,
                                                         @Parameter(example = "0") @RequestParam(required = false, defaultValue = "0") Integer page) {

    Optional<Topic> topic = topicRepository.findById(UUID.fromString(topicId));
    if (topic.isEmpty()) {
      return ResponseEntity.notFound()
                           .build();
    } else {
      Page<Topic> progresses = topicRepository.findTopicsByParentsContainsAndTypeOrderByEventDateDesc(topic.get(), EntryType.RESPONSE,
                                                                                                      PageRequest.of(page, 10));
      return ResponseEntity.ok(progresses);
    }
  }


  @Operation(description = "get topics having a sequence code")
  @GetMapping("/sequenced")
  public ResponseEntity<List<Topic>> getSequencedTopic() {
    return ResponseEntity.ok(topicRepository.findAllBySeqIsNotNullOrderBySeqDesc());
  }


}

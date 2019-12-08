package io.littlegashk.webapp;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.google.common.collect.Lists;
import io.littlegashk.webapp.entity.ChildRelation;
import io.littlegashk.webapp.entity.EntryType;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import io.littlegashk.webapp.entity.UrlTopic;
import io.littlegashk.webapp.repository.ChildRelationRepository;
import io.littlegashk.webapp.repository.OldTagRepository;
import io.littlegashk.webapp.repository.OldTopicRepository;
import io.littlegashk.webapp.repository.SequencedTopicCache;
import io.littlegashk.webapp.repository.UrlRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
  OldTopicRepository repository;

  @Autowired
  OldTagRepository oldTagRepository;

  @Autowired
  UrlRepository urlRepository;

  @Autowired
  ChildRelationRepository childRelationRepository;

  @Autowired
  SequencedTopicCache sequencedTopicCache;


  @Operation(description = "get all topics, sorted by lastUpdated date desc")
  @GetMapping
  public ResponseEntity<Page<Topic>> getTopics(@Parameter(example = "1565883488250") @RequestParam(required = false) Long lastUpdated,
                                               @Parameter(example = "0") @RequestParam(required = false, defaultValue = "0") Integer page) {

    Page<Topic> allTopic = repository.getAllTopicUpdatedBefore(lastUpdated == null ? Long.MAX_VALUE : lastUpdated, page);
    return ResponseEntity.ok(allTopic);
  }

  @Operation(description = "get all topics with specified date, sorted by topicId desc")
  @GetMapping("/date/{date}")
  public ResponseEntity<Map<String, Object>> getTopicsByDate(@Parameter(example = "2019-08-01") @PathVariable String date) {
    Map<String, Object> result = new HashMap<>();
    result.put("date", date);
    Page<Topic> allTopic = repository.getAllTopicByEventDate(date.replaceAll("-",""));
    result.put("topics", allTopic.getContent());
    return ResponseEntity.ok(result);
  }


  @Operation(description = "get all topics/progresses/responses with specified url")
  @GetMapping("/url")
  public ResponseEntity<List<Topic>> getTopicsByUrl(@Parameter(example = "https://google.com") @RequestParam String url) {

    Page<UrlTopic> allTopic = urlRepository.findAllWithUrl(url.trim(), 0);
    Iterable<Topic> topics = repository.findAllById(allTopic.get()
                                                            .map(UrlTopic::getTopicId)
                                                            .flatMap(s -> Stream.of(TopicId.of(s, EntryType.TOPIC), TopicId.of(s, EntryType.PROGRESS),
                                                                                    TopicId.of(s, EntryType.RESPONSE)))
                                                            .collect(Collectors.toList()));
    return ResponseEntity.ok(Lists.newArrayList(topics));
  }

  @Autowired
  AmazonDynamoDB db;

  @Operation(description = "return all TOP LEVEL topics with children containing the given URL ")
  @GetMapping("/top-level-by-url")
  public ResponseEntity<List<Topic>> getTopLevelByUrl(@Parameter(example = "https://google.com") @RequestParam String url) {

    Page<UrlTopic> allTopic = urlRepository.findAllWithUrl(url.trim(), 0);
    Iterable<Topic> topics = repository.findAllById(allTopic.get()
                                                            .map(UrlTopic::getTopicId)
                                                            .flatMap(s -> Stream.of(TopicId.of(s, EntryType.TOPIC), TopicId.of(s, EntryType.PROGRESS),
                                                                                    TopicId.of(s, EntryType.RESPONSE)))
                                                            .collect(Collectors.toList()));
    Set<String> bulkFetchTopicIds = new HashSet<>();
    for (Topic topic : topics) {
      if (topic.getType() == EntryType.TOPIC) {
        bulkFetchTopicIds.add(topic.getTopicId());
      } else {
        String tid = topic.getTopicId();
        String sid = topic.getType()
                          .name() + "|" + tid;
        QueryResult result = db.query(new QueryRequest().withTableName(DynamoDbSchemaInitializer.TABLE_LITTLEGAS)
                                                        .withIndexName("sid-pid-index")
                                                        .withKeyConditionExpression("sid = :sid")
                                                        .withExpressionAttributeValues(Map.of(":sid", new AttributeValue().withS(sid))));
        bulkFetchTopicIds.addAll(result.getItems()
                                       .stream()
                                       .map(m -> m.get("pid")
                                                  .getS())
                                       .collect(Collectors.toSet()));
      }
    }
    Iterable<Topic> parents = repository.findAllById(bulkFetchTopicIds.stream()
                                                                      .map(TopicId::of)
                                                                      .collect(Collectors.toList()));

    return ResponseEntity.ok(Lists.newArrayList(parents));
  }

  @Operation(description = "get specific topic with topicId")
  @GetMapping("/{topicId}")
  public ResponseEntity<Topic> getTopicByTopicId(@Parameter(example = "2019-08-01|1565877016020") @PathVariable String topicId) {

    return ResponseEntity.ok(repository.findByTopicId(topicId));
  }

  @Operation(description = "get topic progresses")
  @GetMapping("/{topicId}/progress")
  public ResponseEntity<Page<Topic>> getAllTopicProgress(@Parameter(example = "2019-08-01|1565877016020") @PathVariable String topicId,
                                                         @RequestParam(required = false, defaultValue = "9999") String lastChildId,
                                                         @Parameter(example = "0") @RequestParam(required = false, defaultValue = "0") Integer page) {

    Page<ChildRelation> relations = childRelationRepository.getProgress(topicId, lastChildId, page);
    List<TopicId> childIds = relations.stream()
                                      .map(ChildRelation::getChildTopicId)
                                      .map(s -> TopicId.of(s, EntryType.PROGRESS))
                                      .collect(Collectors.toList());
    Iterable<Topic> topics = repository.findAllById(childIds);
    List<Topic> result = Lists.newArrayList(topics);
    result.sort(Comparator.comparing(Topic::getTopicId).reversed());
    return ResponseEntity.ok(new PageImpl<>(result, relations.getPageable(), relations.getTotalElements()));
  }

  @Operation(description = "get public responses on a topic")
  @GetMapping("/{topicId}/response")
  public ResponseEntity<Page<Topic>> getAllTopicResponse(@Parameter(example = "2019-08-01|1565877016020") @PathVariable String topicId,
                                                         @RequestParam(required = false, defaultValue = "9999") String lastChildId,
                                                         @Parameter(example = "0") @RequestParam(required = false, defaultValue = "0") Integer page) {

    Page<ChildRelation> relations = childRelationRepository.getResponse(topicId, lastChildId, page);
    List<TopicId> childIds = relations.stream()
                                      .map(ChildRelation::getChildTopicId)
                                      .map(s -> TopicId.of(s, EntryType.RESPONSE))
                                      .collect(Collectors.toList());
    Iterable<Topic> topics = repository.findAllById(childIds);
    List<Topic> result = Lists.newArrayList(topics);
    result.sort(Comparator.comparing(Topic::getTopicId).reversed());
    return ResponseEntity.ok(new PageImpl<>(result, relations.getPageable(), relations.getTotalElements()));
  }


  @Operation(description = "get public responses on a topic")
  @GetMapping("/sequenced")
  public ResponseEntity<List<Topic>> getSequencedTopic(){
    Set<String> topicIds = sequencedTopicCache.getSequencedTopicIds();
    Iterable<Topic> topics = repository.findAllById(topicIds.stream().map(TopicId::of).collect(Collectors.toList()));
    List<Topic> allTopics = Lists.newArrayList(topics);
    allTopics.sort(Comparator.comparing(Topic::getSeq).reversed());
    return ResponseEntity.ok(allTopics);

  }



}

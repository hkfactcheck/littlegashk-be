package io.littlegashk.webapp;

import static io.littlegashk.webapp.DynamoDbSchemaInitializer.TABLE_LITTLEGAS;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import io.littlegashk.webapp.entity.ChildRelation;
import io.littlegashk.webapp.entity.EntryType;
import io.littlegashk.webapp.entity.TagTopic;
import io.littlegashk.webapp.entity.TagTopicId;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import io.littlegashk.webapp.entity.UrlTopic;
import io.littlegashk.webapp.entity.UrlTopicId;
import io.littlegashk.webapp.repository.ChildRelationRepository;
import io.littlegashk.webapp.repository.OldTagRepository;
import io.littlegashk.webapp.repository.OldTopicRepository;
import io.littlegashk.webapp.repository.SequencedTopicCache;
import io.littlegashk.webapp.repository.UrlRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
  OldTopicRepository topicRepository;

  @Autowired
  OldTagRepository oldTagRepository;
  @Autowired
  ChildRelationRepository childRelationRepository;

  @Autowired
  AmazonDynamoDB db;

  @Autowired
  UrlRepository urlRepository;

  @Autowired
  SequencedTopicCache sequencedTopicCache;

  @Operation(description = "insert topic")
  @PostMapping("/topics")
  public ResponseEntity<Topic> addTopic(@RequestBody Topic topic) throws ParseException {
    String idDate = validateAndGetIdFormatDate(topic.getEventDate());
    TopicId id = TopicId.fromEventDate(idDate, EntryType.TOPIC.name());
    topic.setTopicId(id.getTopicId());
    topic.setSortKey(id.getSortKey());
    topic.setType(EntryType.TOPIC);
    topic.setLastUpdated(parseEventDate(topic.getEventDate()).getTime());
    findAndAddRelatedTopic(topic);
    final Topic savedTopic = topicRepository.save(topic);
    saveTags(savedTopic);
    saveUrls(savedTopic);
    if(topic.getSeq()!=null && topic.getSeq()>1){
      sequencedTopicCache.clear();
    }
    return ResponseEntity.ok(savedTopic);
  }

  void findAndAddRelatedTopic(Topic topic) {

    final Multimap<String, String> topicIdToTags = ArrayListMultimap.create();
    for (String t : topic.getTags()) {
      Page<TagTopic> page = null;
      do {
        page = oldTagRepository.findAllWithTag(t, "9999", page == null ? 0 : page.nextPageable()
                                                                                 .getPageNumber());
        page.stream()
            .filter(tt -> !tt.getTopicId()
                             .equals(topic.getTopicId()) && topicRepository.findById(TopicId.of(tt.getTopicId()))
                                                                           .isPresent())
            .forEach(tt -> {

              topicIdToTags.put(tt.getTopicId(), tt.getTagString());

            });
      } while (!page.isLast());
    }

    TreeMultimap<Integer, String> sorted = TreeMultimap.create(Comparator.reverseOrder(), Comparator.naturalOrder());
    for (Map.Entry<String, Collection<String>> entry : topicIdToTags.asMap()
                                                                    .entrySet()) {
      sorted.put(entry.getValue()
                      .size(), entry.getKey());
    }
    int i = 0;
    for (Map.Entry<Integer, String> entry : sorted.entries()) {
      if (i == 5) {
        break;
      }
      String topicId = entry.getValue();
      if (topic.getRelatedTopics() == null) {
        topic.setRelatedTopics(new HashSet<>());
      }
      topic.getRelatedTopics()
           .add(topicId);

      i++;
    }
  }


  @Operation(description = "delete a topic")
  @DeleteMapping("/topics/{topicId}")
  public ResponseEntity<?> deleteTopic(@PathVariable String topicId) {

    QueryResult queryResult = db.query(new QueryRequest().withTableName(TABLE_LITTLEGAS)
                                                         .withKeyConditionExpression("pid = :pid")
                                                         .withExpressionAttributeValues(
                                                             ImmutableMap.of(":pid", new AttributeValue().withS(topicId))));
    queryResult.getItems()
               .stream()
               .map(m -> TopicId.of(m.get("pid")
                                     .getS(), m.get("sid")
                                               .getS()))
               .forEach(topicRepository::deleteById);
    return new ResponseEntity<>(topicId, HttpStatus.OK);
  }


  @Operation(description = "edit topic")
  @PutMapping("/topics")
  public ResponseEntity<Topic> editTopic(@RequestBody Topic topic) {

    Topic db = Stream.of(topicRepository.findById(TopicId.of(topic.getTopicId(), EntryType.TOPIC)),
                         topicRepository.findById(TopicId.of(topic.getTopicId(), EntryType.PROGRESS)),
                         topicRepository.findById(TopicId.of(topic.getTopicId(), EntryType.RESPONSE)))
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .findFirst()
                     .get();

    deleteUrls(db);
    deleteTags(db);
    db.setTitle(topic.getTitle());
    db.setSummary(topic.getSummary());
    db.setTags(topic.getTags());
    db.setReferences(topic.getReferences());
    db.setRelatedTopics(topic.getRelatedTopics());
    db.setImageUrl(topic.getImageUrl());
    db.setUid(topic.getUid());
    findAndAddRelatedTopic(db);
    Topic savedTopic = topicRepository.save(db);
    saveTags(savedTopic);
    saveUrls(savedTopic);
    return ResponseEntity.ok(savedTopic);
  }

  @Operation(description = "insert a progress")
  @PostMapping("/topics/{parentTopicId}/progress")
  public ResponseEntity<Topic> addProgress(HttpServletRequest req, @PathVariable String parentTopicId, @RequestBody Topic topic)
      throws ParseException {
    String idDate = validateAndGetIdFormatDate(topic.getEventDate());
    TopicId id = TopicId.fromEventDate(idDate, EntryType.PROGRESS.name());
    topic.setTopicId(id.getTopicId());
    topic.setSortKey(id.getSortKey());
    topic.setType(EntryType.PROGRESS);
    topic.setLastUpdated(parseEventDate(topic.getEventDate()).getTime());
    Topic savedTopic = topicRepository.save(topic);
    saveTags(savedTopic);
    saveUrls(savedTopic);
    associatedParent(parentTopicId, savedTopic);
    return ResponseEntity.ok(savedTopic);
  }

  @Operation(description = "change topic sequence")
  @PutMapping("/topics/{topicId}/sequence")
  public ResponseEntity<?> changeSequence(HttpServletRequest req, @PathVariable String topicId, @RequestParam Integer sequence){

    Optional<Topic> optionalTopic = topicRepository.findById(TopicId.of(topicId));
    if (optionalTopic.isPresent()) {
      Topic target = optionalTopic.get();
      target.setSeq(sequence);
      sequencedTopicCache.clear();
      topicRepository.save(target);
      return ResponseEntity.ok(null);
    }else{
      return ResponseEntity.badRequest().build();
    }
  }

  private Date parseEventDate(String eventDate) throws ParseException {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return format.parse(eventDate);
  }

  private void associatedParent(@PathVariable String topicId, Topic savedTopic) throws ParseException {

    Optional<Topic> parent = topicRepository.findById(TopicId.of(topicId));
    if (parent.isPresent()) {
      Topic parentTopic = parent.get();
      long childDate = parseEventDate(savedTopic.getEventDate()).getTime();
      if(childDate > parentTopic.getLastUpdated()){
        parentTopic.setLastUpdated(childDate);
      }

      topicRepository.save(parentTopic);
      ChildRelation childRelation = new ChildRelation();
      childRelation.setTopicId(parentTopic.getTopicId());
      childRelation.setSortKey(savedTopic.getType()
                                         .name() + "|" + savedTopic.getTopicId());
      childRelationRepository.save(childRelation);
    }
  }

  @Operation(description = "insert a public response")
  @PostMapping("/topics/{parentTopicId}/response")
  public ResponseEntity<Topic> addResponse(HttpServletRequest req, @PathVariable String parentTopicId, @RequestBody Topic topic)
      throws ParseException {
    String idDate = validateAndGetIdFormatDate(topic.getEventDate());
    TopicId id = TopicId.fromEventDate(idDate, EntryType.RESPONSE.name());
    topic.setTopicId(id.getTopicId());
    topic.setSortKey(id.getSortKey());
    topic.setType(EntryType.RESPONSE);
    topic.setLastUpdated(parseEventDate(topic.getEventDate()).getTime());
    Topic savedTopic = topicRepository.save(topic);
    saveTags(savedTopic);
    saveUrls(savedTopic);
    associatedParent(parentTopicId, savedTopic);
    return ResponseEntity.ok(savedTopic);
  }

  @Operation(description = "update event date")
  @PostMapping("/topics/{oldTopicId}/changeDate")
  public ResponseEntity<Topic> changeDate(@PathVariable String oldTopicId, @RequestParam String newDate) throws ParseException {
    String idDate = validateAndGetIdFormatDate(newDate);
    //Change topic itself
    Topic topic = topicRepository.findByTopicId(oldTopicId);
    topicRepository.deleteById(TopicId.of(topic.getTopicId(), topic.getSortKey()));
    String newTopicId = TopicId.fromEventDate(idDate, topic.getSortKey()).getTopicId();
    topic.setTopicId(newTopicId);
    topic.setEventDate(newDate);
    long newTime = parseEventDate(newDate).getTime();
    if(newTime> topic.getLastUpdated()){
      topic.setLastUpdated(newTime);
    }
    topicRepository.save(topic);

    //Change all child relationships
    childRelationRepository.getProgress(oldTopicId, "9999",0, 1000).stream().forEach(cr->{
      childRelationRepository.delete(cr);
      cr.setTopicId(newTopicId);
      childRelationRepository.save(cr);
    });

    childRelationRepository.getResponse(oldTopicId, "9999",0, 1000).stream().forEach(cr->{
      childRelationRepository.delete(cr);
      cr.setTopicId(newTopicId);
      childRelationRepository.save(cr);
    });

    //Change all parent relationships
    childRelationRepository.getParents(oldTopicId).stream().forEach(cr->{
      childRelationRepository.delete(cr);
      cr.setSortKey(topic.getType().name() + "|" + newTopicId);
      childRelationRepository.save(cr);
    });

    //Change all related topic
    Page<Topic> topics = topicRepository.findTopicsBySortKeyIn(PageRequest.of(0, 100), "TOPIC", "PROGRESS", "RESPONSE");
    List<Topic> allTopics = new ArrayList<>(topics.getContent());
    while(topics.hasNext()){
      topics = topicRepository.findTopicsBySortKeyIn(topics.nextPageable(), "TOPIC", "PROGRESS", "RESPONSE");
      allTopics.addAll(topics.getContent());
    }

    allTopics.forEach(t->{

      if(t.getRelatedTopics()!=null && t.getRelatedTopics().contains(oldTopicId)){
        t.getRelatedTopics().remove(oldTopicId);
        t.getRelatedTopics().add(newTopicId);
        topicRepository.save(t);
      }
    });

    //Change all tag topic association
    oldTagRepository.findTopicTags(oldTopicId).stream().forEach(tt->{
      oldTagRepository.delete(tt);
      tt.setTopicId(newTopicId);
      oldTagRepository.save(tt);
    });

    //Change all url topic association

    urlRepository.findTopicUrls(oldTopicId).stream().forEach(tt->{
      urlRepository.delete(tt);
      tt.setTopicId(newTopicId);
      urlRepository.save(tt);
    });




    return ResponseEntity.ok(topic);
  }


  private void deleteUrls(Topic existing) {

    if (existing.getReferences() != null && existing.getReferences()
                                                    .size() > 0) {
      Set<UrlTopic> currentSet = existing.getReferences()
                                         .stream()
                                         .filter(r -> StringUtils.isNotBlank(r.getLink()))
                                         .map(r -> UrlTopicId.of(existing.getTopicId(), r.getLink()))
                                         .map(UrlTopic::new)
                                         .collect(Collectors.toSet());

      urlRepository.deleteAll(currentSet);
    }
  }


  private void saveUrls(Topic updated) {

    if (updated.getReferences() != null && updated.getReferences()
                                                  .size() > 0) {

      Set<UrlTopic> currentSet = updated.getReferences()
                                        .stream()
                                        .filter(r -> StringUtils.isNotBlank(r.getLink()))
                                        .map(r -> UrlTopicId.of(updated.getTopicId(), r.getLink()))
                                        .map(UrlTopic::new)
                                        .collect(Collectors.toSet());

      urlRepository.saveAll(currentSet);
    }
  }

  private void deleteTags(Topic existing) {

    if (existing.getTags() != null && existing.getTags()
                                              .size() > 0) {
      Set<TagTopic> currentSet = existing.getTags()
                                         .stream()
                                         .map(s -> TagTopicId.of(existing.getTopicId(), s))
                                         .map(TagTopic::new)
                                         .collect(Collectors.toSet());

      oldTagRepository.deleteAll(currentSet);
    }
  }


  private void saveTags(Topic updated) {

    if (updated.getTags() != null && updated.getTags()
                                            .size() > 0) {
      Set<TagTopic> newTagList = updated.getTags()
                                        .stream()
                                        .map(s -> TagTopicId.of(updated.getTopicId(), s))
                                        .map(TagTopic::new)
                                        .collect(Collectors.toSet());

      oldTagRepository.saveAll(newTagList);
    }
  }

  private String validateAndGetIdFormatDate(String date) throws ValidationException {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat idFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    try {
      Date parsed= format.parse(date);
      return idFormat.format(parsed);
    } catch (ParseException e) {
      throw new ValidationException("Topic must have a valid date yyyy-MM-dd HH:mm:ss");
    }
  }


}

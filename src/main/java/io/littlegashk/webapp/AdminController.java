package io.littlegashk.webapp;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.google.common.collect.ImmutableMap;
import io.littlegashk.webapp.entity.*;
import io.littlegashk.webapp.repository.ChildRelationRepository;
import io.littlegashk.webapp.repository.TagRepository;
import io.littlegashk.webapp.repository.TopicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.littlegashk.webapp.DynamoDbSchemaInitializer.TABLE_LITTLEGAS;

@RestController
@RequestMapping("/admin")
@Api(value = "Admin")
@Log4j2
public class AdminController {

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    TagRepository tagRepository;
    @Autowired
    ChildRelationRepository childRelationRepository;

    @Autowired
    AmazonDynamoDB db;

    @ApiOperation("insert topic")
    @PostMapping("/topics")
    public ResponseEntity<Topic> addTopic(@RequestBody Topic topic) {

        TopicId id = TopicId.fromEventDate(topic.getEventDate());
        topic.setTopicId(id.getTopicId());
        topic.setSortKey(id.getSortKey());
        topic.setType(EntryType.TOPIC);
        topic.setLastUpdated(System.currentTimeMillis());
        final Topic savedTopic = topicRepository.save(topic);
        saveTags(savedTopic);
        return ResponseEntity.ok(savedTopic);
    }

    @ApiOperation("delete a topic")
    @DeleteMapping("/{topicId}")
    public ResponseEntity<?> deleteTopic(@PathVariable String topicId) {

        QueryResult queryResult = db.query(new QueryRequest().withTableName(TABLE_LITTLEGAS)
                                                             .withKeyConditionExpression("pid = :pid")
                                                             .withExpressionAttributeValues(ImmutableMap.of(":pid",
                                                                                                            new AttributeValue().withS(topicId))));
        queryResult.getItems().stream().map(m -> TopicId.of(m.get("pid").getS(), m.get("sid").getS())).forEach(topicRepository::deleteById);
        return new ResponseEntity<>(topicId, HttpStatus.OK);
    }

    private void deleteTags(Topic existing) {

        if (existing.getTags() != null && existing.getTags().size() > 0) {
            Set<TagTopic> currentSet = existing.getTags()
                                               .stream()
                                               .map(s -> TagTopicId.of(existing.getTopicId(), s))
                                               .map(TagTopic::new)
                                               .collect(Collectors.toSet());

            tagRepository.deleteAll(currentSet);
        }
    }


    private void saveTags(Topic updated) {

        if (updated.getTags() != null && updated.getTags().size() > 0) {
            Set<TagTopic> newTagList = updated.getTags()
                                              .stream()
                                              .map(s -> TagTopicId.of(updated.getTopicId(), s))
                                              .map(TagTopic::new)
                                              .collect(Collectors.toSet());

            tagRepository.saveAll(newTagList);
        }
    }

    @ApiOperation("edit topic")
    @PutMapping("/topics")
    public ResponseEntity<Topic> editTopic(@RequestBody Topic topic) {

        Topic db = topicRepository.findById(TopicId.of(topic.getTopicId())).get();
        deleteTags(db);
        db.setTitle(topic.getTitle());
        db.setSummary(topic.getSummary());
        db.setTags(topic.getTags());
        db.setReferences(topic.getReferences());
        db.setRelatedTopics(topic.getRelatedTopics());
        Topic savedTopic = topicRepository.save(db);
        saveTags(savedTopic);
        return ResponseEntity.ok(savedTopic);
    }

    @ApiOperation("insert a progress")
    @PostMapping("/topics/{parentTopicId}/progress")
    public ResponseEntity<Topic> addProgress(HttpServletRequest req, @PathVariable String parentTopicId, @RequestBody Topic topic) {

        TopicId id = TopicId.fromEventDate(topic.getEventDate(), EntryType.PROGRESS.name());
        topic.setTopicId(id.getTopicId());
        topic.setSortKey(id.getSortKey());
        topic.setType(EntryType.PROGRESS);
        topic.setLastUpdated(System.currentTimeMillis());
        Topic savedTopic = topicRepository.save(topic);
        saveTags(savedTopic);

        associatedParent(parentTopicId, savedTopic);
        return ResponseEntity.ok(savedTopic);
    }

    private void associatedParent(@PathVariable String topicId, Topic savedTopic) {

        Optional<Topic> parent = topicRepository.findById(TopicId.of(topicId));
        if (parent.isPresent()) {
            Topic parentTopic = parent.get();
            parentTopic.setLastUpdated(System.currentTimeMillis());
            topicRepository.save(parentTopic);
            ChildRelation childRelation = new ChildRelation();
            childRelation.setTopicId(parentTopic.getTopicId());
            childRelation.setSortKey(savedTopic.getType().name() + "|" + savedTopic.getTopicId());
            childRelationRepository.save(childRelation);
        }
    }

    @ApiOperation("insert a public response")
    @PostMapping("/topics/{parentTopicId}/response")
    public ResponseEntity<Topic> addResponse(HttpServletRequest req, @PathVariable String parentTopicId, @RequestBody Topic topic) {

        TopicId id = TopicId.fromEventDate(topic.getEventDate(), EntryType.RESPONSE.name());
        topic.setTopicId(id.getTopicId());
        topic.setSortKey(id.getSortKey());
        topic.setType(EntryType.RESPONSE);
        topic.setLastUpdated(System.currentTimeMillis());
        Topic savedTopic = topicRepository.save(topic);
        saveTags(savedTopic);

        associatedParent(parentTopicId, savedTopic);
        return ResponseEntity.ok(savedTopic);
    }
}

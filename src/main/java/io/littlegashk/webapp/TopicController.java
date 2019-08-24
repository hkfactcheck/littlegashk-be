package io.littlegashk.webapp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.littlegashk.webapp.entity.*;
import io.littlegashk.webapp.repository.ChildRelationRepository;
import io.littlegashk.webapp.repository.TagRepository;
import io.littlegashk.webapp.repository.TopicRepository;
import io.littlegashk.webapp.repository.UrlRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/topics")
@Api(value = "Topics")
@Log4j2
public class TopicController {

    @Autowired
    TopicRepository repository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    UrlRepository urlRepository;

    @Autowired
    ChildRelationRepository childRelationRepository;


    @ApiOperation("get all topics, sorted by lastUpdated date desc")
    @GetMapping
    public ResponseEntity<Page<Topic>> getTopics(@ApiParam(example = "1565883488250") @RequestParam(required = false) Long lastUpdated,
                                                 @ApiParam(example = "0") @RequestParam(required = false, defaultValue = "0") Integer page) {

        Page<Topic> allTopic = repository.getAllTopicUpdatedBefore(lastUpdated == null ? Long.MAX_VALUE : lastUpdated, page);
        return ResponseEntity.ok(allTopic);
    }

    @ApiOperation("get all topics with specified date, sorted by topicId desc")
    @GetMapping("/date/{date}")
    public ResponseEntity<Page<Topic>> getTopicsByDate(@ApiParam(example = "2019-08-01") @PathVariable String date) {

        Page<Topic> allTopic = repository.getAllTopicByEventDate(date);
        return ResponseEntity.ok(allTopic);
    }


    @ApiOperation("get all topics with specified date, sorted by topicId desc")
    @GetMapping("/url")
    public ResponseEntity<List<Topic>> getTopicsByUrl(@ApiParam(example = "https://google.com") @RequestParam String url) {

        Page<UrlTopic> allTopic = urlRepository.findAllWithUrl(url.trim(), 0);
        Iterable<Topic> topics = repository.findAllById(allTopic.get()
                                       .map(UrlTopic::getTopicId)
                                       .flatMap(s -> Stream.of(TopicId.of(s, EntryType.TOPIC),
                                                               TopicId.of(s, EntryType.PROGRESS),
                                                               TopicId.of(s, EntryType.RESPONSE)))
                                       .collect(Collectors.toList()));
        return ResponseEntity.ok(Lists.newArrayList(topics));
    }

    @ApiOperation("get specific topic with topicId")
    @GetMapping("/{topicId}")
    public ResponseEntity<Topic> getTopicByTopicId(@ApiParam(example = "2019-08-01|1565877016020") @PathVariable String topicId) {

        return ResponseEntity.ok(repository.findById(TopicId.of(topicId)).get());
    }

    @ApiOperation("get topic progresses")
    @GetMapping("/{topicId}/progress")
    public ResponseEntity<Page<Topic>> getAllTopicProgress(@ApiParam(example = "2019-08-01|1565877016020") @PathVariable String topicId,
                                                           @RequestParam(required = false, defaultValue = "9999") String lastChildId,
                                                           @ApiParam(example = "0") @RequestParam(required = false, defaultValue = "0") Integer page) {

        Page<ChildRelation> relations = childRelationRepository.getProgress(topicId, lastChildId, page);
        List<TopicId> childIds = relations.stream()
                                          .map(ChildRelation::getChildTopicId)
                                          .map(s -> TopicId.of(s, EntryType.PROGRESS))
                                          .collect(Collectors.toList());
        Iterable<Topic> topics = repository.findAllById(childIds);
        return ResponseEntity.ok(new PageImpl<>(ImmutableList.copyOf(topics), relations.getPageable(), relations.getTotalElements()));
    }

    @ApiOperation("get public responses on a topic")
    @GetMapping("/{topicId}/response")
    public ResponseEntity<Page<Topic>> getAllTopicResponse(@ApiParam(example = "2019-08-01|1565877016020") @PathVariable String topicId,
                                                           @RequestParam(required = false, defaultValue = "9999") String lastChildId,
                                                           @ApiParam(example = "0") @RequestParam(required = false, defaultValue = "0") Integer page) {

        Page<ChildRelation> relations = childRelationRepository.getResponse(topicId, lastChildId, page);
        List<TopicId> childIds = relations.stream()
                                          .map(ChildRelation::getChildTopicId)
                                          .map(s -> TopicId.of(s, EntryType.RESPONSE))
                                          .collect(Collectors.toList());
        Iterable<Topic> topics = repository.findAllById(childIds);
        return ResponseEntity.ok(new PageImpl<>(ImmutableList.copyOf(topics), relations.getPageable(), relations.getTotalElements()));
    }

}

package io.littlegashk.webapp;

import io.littlegashk.webapp.entity.TagTopic;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import io.littlegashk.webapp.repository.TagRepository;
import io.littlegashk.webapp.repository.TopicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tags")
@Api(value = "Tags")
@Log4j2
public class TagController {

    @Autowired
    TopicRepository repository;

    @Autowired
    TagRepository tagRepository;

    @ApiOperation(value="get all tags in the DB", notes="TODO: expensive operation, find ways to optimize")
    @GetMapping
    public ResponseEntity<Set<String>> getTags() {
        Set<String> tags = new HashSet<>();
        tagRepository.findAllTags().iterator().forEachRemaining(t->tags.add(t.getTagId().replaceFirst("TAG\\|","" )));
        return ResponseEntity.ok(tags);
    }

    @ApiOperation("get all topics with specified tag")
    @GetMapping("/{tag}/topics")
    public ResponseEntity<List<Topic>> getTopicsByTag(@PathVariable String tag) {
        List<Topic> responseItems = new LinkedList<>();
        Set<TopicId> allTopic = tagRepository.findAllWithTag(tag)
                                             .stream()
                                             .map(tagTopic -> TopicId.of(tagTopic.getEventDate(), tagTopic.getRecordId()))
                                             .collect(Collectors.toSet());
        repository.findAllById(allTopic).iterator().forEachRemaining(responseItems::add);
        return ResponseEntity.ok(responseItems);
    }
}

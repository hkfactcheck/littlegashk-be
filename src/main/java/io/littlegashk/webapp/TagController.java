package io.littlegashk.webapp;

import com.google.common.collect.ImmutableList;
import io.littlegashk.webapp.entity.EntryType;
import io.littlegashk.webapp.entity.TagTopic;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import io.littlegashk.webapp.repository.TagRepository;
import io.littlegashk.webapp.repository.TopicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/tags")
@Api(value = "Tags")
@Log4j2
public class TagController {

    @Autowired
    TopicRepository repository;

    @Autowired
    TagRepository tagRepository;

    @ApiOperation(value = "get all tags in the DB", notes = "TODO: expensive operation, find ways to optimize")
    @GetMapping
    public ResponseEntity<Set<String>> getTags() {

        return ResponseEntity.ok(tagRepository.findAllTags());
    }

    @ApiOperation("get all topics with specified tag")
    @GetMapping("/{tag}/topics")
    public ResponseEntity<Page<Topic>> getTopicsByTag(@PathVariable String tag,
                                                      @RequestParam(required = false, defaultValue = "9999") String lastTopicId,
                                                      @ApiParam(example = "0") @RequestParam(required = false, defaultValue = "0") Integer page) {

        Page<TagTopic> allTagTopic = tagRepository.findAllWithTag(tag, lastTopicId, page);
        List<TopicId> allTopic = allTagTopic.stream()
                                            .map(TagTopic::getTopicId)
                                            .flatMap(topicId -> Stream.of(TopicId.of(topicId, EntryType.TOPIC),
                                                                          TopicId.of(topicId, EntryType.PROGRESS),
                                                                          TopicId.of(topicId, EntryType.RESPONSE)))
                                            .collect(Collectors.toList());

        Iterable<Topic> topics = repository.findAllById(allTopic);
        return ResponseEntity.ok(new PageImpl<>(ImmutableList.copyOf(topics), allTagTopic.getPageable(), allTagTopic.getTotalElements()));
    }
}

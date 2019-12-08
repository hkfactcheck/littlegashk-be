package io.littlegashk.webapp;

import com.google.common.collect.ImmutableList;
import io.littlegashk.webapp.entity.EntryType;
import io.littlegashk.webapp.entity.TagTopic;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import io.littlegashk.webapp.rentity.Tag;
import io.littlegashk.webapp.rentity.TagRepository;
import io.littlegashk.webapp.repository.OldTagRepository;
import io.littlegashk.webapp.repository.OldTopicRepository;
import io.littlegashk.webapp.util.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
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
@RequestMapping("/tags")
@Log4j2
public class TagController {

    @Autowired
    OldTopicRepository repository;

    @Autowired
    OldTagRepository oldTagRepository;

    @Autowired
    TagRepository tagRepository;

    @Operation(description = "get all tags in the DB")
    @GetMapping
    public ResponseEntity<Set<String>> getTags() {

        return ResponseEntity.ok(tagRepository.findAll()
                                              .stream()
                                              .map(Tag::getTag)
                                              .collect(Collectors.toSet()));
    }

    @Operation(description ="get all topics with specified tag")
    @GetMapping("/{tag}/topics")
    public ResponseEntity<Page<Topic>> getTopicsByTag(@PathVariable String tag,
                                                      @RequestParam(required = false, defaultValue = "9999") String lastTopicId,
                                                      @RequestParam(required = false, defaultValue = "0") Integer page) {

        Page<TagTopic> allTagTopic = oldTagRepository.findAllWithTag(tag, lastTopicId, page);
        Iterable<Topic> topics = resolveTopics(allTagTopic.stream());
        return ResponseEntity.ok(new PageImpl<>(ImmutableList.copyOf(topics), allTagTopic.getPageable(), allTagTopic.getTotalElements()));
    }

    private Iterable<Topic> resolveTopics(Stream<TagTopic> stream) {
        List<TopicId> allTopic = stream.map(TagTopic::getTopicId)
                                       .flatMap(topicId -> Stream.of(TopicId.of(topicId, EntryType.TOPIC), TopicId.of(topicId, EntryType.PROGRESS),
                                                                     TopicId.of(topicId, EntryType.RESPONSE)))
                                       .collect(Collectors.toList());

        return repository.findAllById(allTopic);
    }

    @Operation(description ="get all topics with multiple tags. (AND relation)")
    @GetMapping("/searchByTags")
    public ResponseEntity<List<Topic>> getTopicsByTag(@RequestParam List<String> tags){

        if(tags.isEmpty()){
            return ResponseEntity.badRequest().build();
        }else{
            List<TagTopic> allTagTopic=CommonUtils.readAllPages(p-> oldTagRepository.findAllWithTag(tags.get(0), "9999", p));
            Iterable<Topic> topics = resolveTopics(allTagTopic.stream());
            List<Topic> filtered = StreamSupport.stream(topics.spliterator(), true).filter(t-> t.getTags().containsAll(tags)).collect(Collectors.toList());
            return ResponseEntity.ok(filtered);
        }
    }
}

package io.littlegashk.webapp;

import io.littlegashk.webapp.entity.TagTopic;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.repository.TagRepository;
import io.littlegashk.webapp.repository.TopicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

//    @ApiOperation(value="get all tags in the DB", notes="TODO: expensive operation, find ways to optimize")
//    @GetMapping
//    public ResponseEntity<Set<String>> getTags() {
//        Set<String> tags = new HashSet<>();
//        tagRepository.findAllTags().iterator().forEachRemaining(t->tags.add(t.getTagId().replaceFirst("TAG\\|","" )));
//        return ResponseEntity.ok(tags);
//    }

    @ApiOperation("get all topics with specified tag")
    @GetMapping("/{tag}/topics")
    public ResponseEntity<List<Topic>> getTopicsByTag(@PathVariable String tag,
                                                      @RequestParam(required = false) String lastTopicId) {
        List<String> allTopic = tagRepository.findAllWithTag(tag, lastTopicId)
                                             .stream()
                                             .map(TagTopic::getTopicId)
                                             .collect(Collectors.toList());

        return ResponseEntity.ok(repository.findAllByTopicIdIn(allTopic));
    }
}

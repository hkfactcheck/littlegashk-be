package io.littlegashk.webapp;

import io.littlegashk.webapp.rentity.Tag;
import io.littlegashk.webapp.rentity.TagRepository;
import io.littlegashk.webapp.rentity.Topic;
import io.littlegashk.webapp.rentity.TopicRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
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
@RequestMapping("/tags")
@Log4j2
public class TagController {

    @Autowired
    TagRepository tagRepository;

    @Autowired
    TopicRepository topicRepository;

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
        Optional<Tag> tagRecord = tagRepository.findById(tag);
        if (tagRecord.isEmpty()) {
            return ResponseEntity.notFound()
                                 .build();
        } else {
            return ResponseEntity.ok(topicRepository.findByTagRecordContains(PageRequest.of(page, 10), tagRecord.get()));
        }
    }

    @Operation(description ="get all topics with multiple tags. (AND relation)")
    @GetMapping("/searchByTags")
    public ResponseEntity<List<Topic>> getTopicsByTag(@RequestParam List<String> tags){

        if(tags.isEmpty()){
            return ResponseEntity.badRequest().build();
        }else{
            List<Tag> requested = tags.stream()
                                      .map(tagRepository::getOne)
                                      .collect(Collectors.toList());
            return ResponseEntity.ok(topicRepository.findAllByTagRecordIn(requested));
        }
    }
}

package io.littlegashk.webapp;

import com.google.common.base.Strings;
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
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/topics")
@Api(value = "Topics")
@Log4j2
public class TopicController {

    @Autowired
    TopicRepository repository;

    @Autowired
    TagRepository tagRepository;

    @ApiOperation("get topic")
    @GetMapping("/{topicId}")
    public ResponseEntity<Topic> getTopic(@ApiParam(example = "2019-08-01|topic-[uuid]") @PathVariable String topicId) {

        return ResponseEntity.ok(repository.findById(TopicId.of(topicId)).get());
    }



    @ApiOperation("get all topics with specified date")
    @GetMapping
    public ResponseEntity<List<Topic>> getTopicsByDate(@ApiParam(example = "2019-08-01")@RequestParam String date) {

        List<Topic> allTopic = repository.getAllTopicByDate(date);
        return ResponseEntity.ok(allTopic);
    }

    @ApiOperation("get topic progresses")
    @GetMapping("/{topicId}/progress")
    public ResponseEntity<List<Topic>> getAllTopicProgress(@ApiParam(example = "2019-08-01|topic-[uuid]") @PathVariable String topicId) {

        return ResponseEntity.ok(repository.getProgressByTopic(TopicId.of(topicId)));
    }

    @ApiOperation("get public responses on a topic")
    @GetMapping("/{topicId}/response")
    public ResponseEntity<List<Topic>> getAllTopicResponse(@ApiParam(example = "2019-08-01|topic-[uuid]") @PathVariable String topicId) {

        return ResponseEntity.ok(repository.getPublicResponseByTopic(TopicId.of(topicId)));
    }

}

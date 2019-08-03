package io.littlegashk.webapp;

import com.google.common.base.Strings;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import io.littlegashk.webapp.repository.TopicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/topics")
@Api(value = "Topics")
@Log4j2
public class TopicController {

    @Autowired
    TopicRepository repository;

    @ApiOperation("get topic")
    @GetMapping("/{tid}")
    public ResponseEntity<Topic> getTopic(@PathVariable String tid){
        return ResponseEntity.ok(repository.findById(TopicId.of(tid)).get());
    }

    @ApiOperation("get all topics with specified group")
    @GetMapping
    public ResponseEntity<List<Topic>> getTopicsByGroup(@RequestParam String group){

        List<Topic> allTopic = repository.getAllInGroup(StringUtils.isEmpty(group)?"DEFAULT":group);
        return ResponseEntity.ok(allTopic);
    }

    @ApiOperation("get topic progresses")
    @GetMapping("/{tid}/progress")
    public ResponseEntity<List<Topic>> getAllTopicProgress(@PathVariable String tid){

        List<Topic> progressItems =  new LinkedList<>();
        repository.getProgressByTopicId(tid).iterator().forEachRemaining(progressItems::add);
        progressItems.sort(Comparator.comparing(Topic::getEventDateTime));

        return ResponseEntity.ok(progressItems);
    }

    @ApiOperation("get public responses on a topic")
    @GetMapping("/{tid}/response")
    public ResponseEntity<List<Topic>> getAllTopicResponse(@PathVariable String tid){

        List<Topic> responseItems =  new LinkedList<>();
        repository.getPublicResponseByTopicId(tid).iterator().forEachRemaining(responseItems::add);
        responseItems.sort(Comparator.comparing(Topic::getEventDateTime));

        return ResponseEntity.ok(responseItems);
    }

}

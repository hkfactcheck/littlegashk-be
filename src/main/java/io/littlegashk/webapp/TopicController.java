package io.littlegashk.webapp;

import io.littlegashk.webapp.entity.Event;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import io.littlegashk.webapp.repository.EventRepository;
import io.littlegashk.webapp.repository.TopicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/topics")
@Api(value = "Topics")
@Log4j2
public class TopicController {

    @Autowired
    TopicRepository repository;

    @ApiOperation("get topic meta")
    @GetMapping("/{tid}")
    public ResponseEntity<Topic> getTopic(@PathVariable String pid){
        return ResponseEntity.ok(repository.findById(TopicId.of(pid)).get());
    }

    @ApiOperation("get all topics with specified region")
    @GetMapping
    public ResponseEntity<List<Topic>> getTopicsByRegion(@RequestParam String region){
        List<Topic> allTopic = repository.getAllInGroup(region);
        allTopic = allTopic.stream().filter(p->!p.isHidden()).collect(Collectors.toList());
        return ResponseEntity.ok(allTopic);
    }

    @Autowired
    EventRepository eventRepository;

    @ApiOperation("get all topic events")
    @GetMapping("/{tid}/events")
    public ResponseEntity<List<Event>> getAllTopicEvent(@PathVariable String tid){

        List<Event> events = new LinkedList<>();
        eventRepository.getApprovedEventsByTopicId(tid).iterator().forEachRemaining(events::add);
        events.sort(Comparator.comparing(Event::getDate).reversed());

        return ResponseEntity.ok(events);
    }

}

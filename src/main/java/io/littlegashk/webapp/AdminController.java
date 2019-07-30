package io.littlegashk.webapp;

import io.littlegashk.webapp.entity.Event;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import io.littlegashk.webapp.repository.EventRepository;
import io.littlegashk.webapp.repository.TopicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/admin")
@Api(value = "Admin")
@Log4j2
public class AdminController {

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    EventRepository eventRepository;

    @ApiOperation("insert topic")
    @PostMapping("/topics")
    public ResponseEntity<?> addTopic(@RequestBody Topic topic){

        topicRepository.save(topic);
        return ResponseEntity.ok(null);
    }

    @ApiOperation("edit topic")
    @PutMapping("/topics")
    public ResponseEntity<?> editTopic(@RequestBody Topic topic){
        Topic db = topicRepository.findById(TopicId.of(topic.getTopicId())).get();
        db.setImageUrl(topic.getImageUrl());
        db.setSocialMedia(topic.getSocialMedia());
        db.setPoliticalFaction(topic.getPoliticalFaction());
        db.setPoliticalAffiliation(topic.getPoliticalAffiliation());
        db.setBackground(topic.getBackground());
        db.setTags(topic.getTags());
        topicRepository.save(db);
        return ResponseEntity.ok(null);
    }

    @ApiOperation("insert an event")
    @PostMapping("/events")
    public ResponseEntity<?> addEvent(HttpServletRequest req, @RequestBody Event event){
        String username = JwtUtils.getUsername(req);
        event.setDownRaters(new HashSet<>());
        event.setUpRaters(new HashSet<>());
        event.setRating(0);
        event.setCreatedBy(username);
        eventRepository.save(event);
        return ResponseEntity.ok(null);
    }

    @ApiOperation("get all topic events")
    @GetMapping("/topics/{pid}/events")
    public ResponseEntity<List<Event>> getAllTopicalEvent(@PathVariable String pid){

        List<Event> events = new LinkedList<>();
        eventRepository.getAllEventsByTopicId(pid).iterator().forEachRemaining(events::add);
        //Soft by rating?
        events.sort(Comparator.comparing(Event::getDate).reversed());

        return ResponseEntity.ok(events);
    }

    @ApiOperation("get all topics with specified region")
    @GetMapping("/topics")
    public ResponseEntity<List<Topic>> getTopicsByRegion(@RequestParam String group){
        return ResponseEntity.ok(topicRepository.getAllInGroup(group));
    }

    @ApiOperation("vote up event")
    @PutMapping("/topics/{pid}/events/{sid}/up")
    public ResponseEntity<?> voteUpEvent(HttpServletRequest req, @PathVariable String pid, @PathVariable String sid){
        String username = JwtUtils.getUsername(req);
        Event event = eventRepository.findEventByTopicIdAndEventId(pid, sid).get(0);
        if (StringUtils.equals(event.getCreatedBy(), username)){
            return ResponseEntity.badRequest().build();
        }
        if(!event.getUpRaters().contains(username)){
            event.getUpRaters().add(username);
            event.getDownRaters().remove(username);
            event.setRating(event.getUpRaters().size()-event.getDownRaters().size());
            eventRepository.save(event);
        }else{
            return ResponseEntity.badRequest().build();
        }


        return ResponseEntity.ok(null);
    }

    @ApiOperation("vote down event")
    @PutMapping("/topics/{pid}/events/{sid}/down")
    public ResponseEntity<?> voteDownEvent(HttpServletRequest req, @PathVariable String pid, @PathVariable String sid){
        String username = JwtUtils.getUsername(req);
        Event event = eventRepository.findEventByTopicIdAndEventId(pid, sid).get(0);

        if (StringUtils.equals(event.getCreatedBy(), username)){
            return ResponseEntity.badRequest().build();
        }
        if(!event.getDownRaters().contains(username)){
            event.getDownRaters().add(username);
            event.getUpRaters().remove(username);
            event.setRating(event.getUpRaters().size()-event.getDownRaters().size());
            eventRepository.save(event);
        }else{
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(null);
    }

    @ApiOperation("delete event")
    @PutMapping("/topics/{pid}/events/{sid}/delete")
    public ResponseEntity<?> deleteEvent(HttpServletRequest req, @PathVariable String pid, @PathVariable String sid){
        String username = JwtUtils.getUsername(req);
        Event event = eventRepository.findEventByTopicIdAndEventId(pid, sid).get(0);

        if (StringUtils.equals(event.getCreatedBy(), username)){
            eventRepository.delete(event);
            return ResponseEntity.ok(null);
        }else{
            return ResponseEntity.badRequest().build();
        }
    }

    @ApiOperation("hide topic")
    @PutMapping("/topics/{pid}/hide")
    public ResponseEntity<?> hideTopic(@PathVariable String pid){

        Topic topic = topicRepository.findById(TopicId.of(pid)).get();
        topic.setHidden(true);
        topicRepository.save(topic);
        return ResponseEntity.ok(null);
    }

    @ApiOperation("show topic")
    @PutMapping("/topics/{pid}/show")
    public ResponseEntity<?> showTopic(@PathVariable String pid){

        Topic topic = topicRepository.findById(TopicId.of(pid)).get();
        topic.setHidden(false);
        topicRepository.save(topic);
        return ResponseEntity.ok(null);
    }
}

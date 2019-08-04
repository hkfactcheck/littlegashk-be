package io.littlegashk.webapp;

import io.littlegashk.webapp.entity.*;
import io.littlegashk.webapp.repository.TagRepository;
import io.littlegashk.webapp.repository.TopicRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@Api(value = "Admin")
@Log4j2
public class AdminController {

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    TagRepository tagRepository;

    @ApiOperation("insert topic")
    @PostMapping("/topics")
    public ResponseEntity<?> addTopic(@RequestBody Topic topic) {

        if (topic.getGroup() == null) {
            topic.setGroup("DEFAULT");
        }
        topic.setType(EntryType.TOPIC);
        topic.setRecordId("TOP");
        final Topic savedTopic = topicRepository.save(topic);
        saveTags(savedTopic);
        return ResponseEntity.ok(null);
    }

    private void deleteTags(Topic existing) {

        Set<TagTopic> currentSet = existing.getTags()
                                             .stream()
                                             .map(s -> TagTopicId.of(s, existing.getTopicId(), existing.getRecordId()))
                                             .map(TagTopic::new)
                                             .collect(Collectors.toSet());

        tagRepository.deleteAll(currentSet);
    }


    private void saveTags(Topic updated) {

        Set<TagTopic> newTagList = updated.getTags()
                                           .stream()
                                           .map(s -> TagTopicId.of(s, updated.getTopicId(), updated.getRecordId()))
                                           .map(TagTopic::new)
                                           .collect(Collectors.toSet());

        tagRepository.saveAll(newTagList);
    }

    @ApiOperation("edit topic")
    @PutMapping("/topics")
    public ResponseEntity<?> editTopic(@RequestBody Topic topic) {

        Topic db = topicRepository.findById(TopicId.of(topic.getTopicId())).get();
        deleteTags(db);
        db.setTitle(topic.getTitle());
        db.setEventDateTime(topic.getEventDateTime());
        db.setSummary(topic.getSummary());
        db.setTags(topic.getTags());
        db.setReferences(topic.getReferences());
        db.setRelatedTopics(topic.getRelatedTopics());
        Topic savedTopic = topicRepository.save(db);
        saveTags(savedTopic);
        return ResponseEntity.ok(null);
    }

    @ApiOperation("insert a progress")
    @PostMapping("/progress")
    public ResponseEntity<?> addProgress(HttpServletRequest req, @RequestBody Topic topic) {

        topic.setGroup(null); //important
        topic.setRecordId("progress-" + UUID.randomUUID().toString());
        topic.setType(EntryType.PROGRESS);
        Topic savedTopic = topicRepository.save(topic);
        saveTags(savedTopic);
        return ResponseEntity.ok(null);
    }

    @ApiOperation("insert a public response")
    @PostMapping("/response")
    public ResponseEntity<?> addResponse(HttpServletRequest req, @RequestBody Topic topic) {

        topic.setGroup(null); //important
        topic.setRecordId("response-" + UUID.randomUUID().toString());
        topic.setType(EntryType.PUBLIC_RESPONSE);
        Topic savedTopic = topicRepository.save(topic);
        saveTags(savedTopic);
        return ResponseEntity.ok(null);
    }

    //
    //
    //    @ApiOperation("vote up event")
    //    @PutMapping("/topics/{pid}/events/{sid}/up")
    //    public ResponseEntity<?> voteUpEvent(HttpServletRequest req, @PathVariable String pid, @PathVariable String sid){
    //        String username = JwtUtils.getUsername(req);
    //        Event event = eventRepository.findEventByTopicIdAndEventId(pid, sid).get(0);
    //        if (StringUtils.equals(event.getCreatedBy(), username)){
    //            return ResponseEntity.badRequest().build();
    //        }
    //        if(!event.getUpRaters().contains(username)){
    //            event.getUpRaters().add(username);
    //            event.getDownRaters().remove(username);
    //            event.setRating(event.getUpRaters().size()-event.getDownRaters().size());
    //            eventRepository.save(event);
    //        }else{
    //            return ResponseEntity.badRequest().build();
    //        }
    //
    //
    //        return ResponseEntity.ok(null);
    //    }
    //
    //    @ApiOperation("vote down event")
    //    @PutMapping("/topics/{pid}/events/{sid}/down")
    //    public ResponseEntity<?> voteDownEvent(HttpServletRequest req, @PathVariable String pid, @PathVariable String sid){
    //        String username = JwtUtils.getUsername(req);
    //        Event event = eventRepository.findEventByTopicIdAndEventId(pid, sid).get(0);
    //
    //        if (StringUtils.equals(event.getCreatedBy(), username)){
    //            return ResponseEntity.badRequest().build();
    //        }
    //        if(!event.getDownRaters().contains(username)){
    //            event.getDownRaters().add(username);
    //            event.getUpRaters().remove(username);
    //            event.setRating(event.getUpRaters().size()-event.getDownRaters().size());
    //            eventRepository.save(event);
    //        }else{
    //            return ResponseEntity.badRequest().build();
    //        }
    //        return ResponseEntity.ok(null);
    //    }
    //
    //    @ApiOperation("delete event")
    //    @PutMapping("/topics/{pid}/events/{sid}/delete")
    //    public ResponseEntity<?> deleteEvent(HttpServletRequest req, @PathVariable String pid, @PathVariable String sid){
    //        String username = JwtUtils.getUsername(req);
    //        Event event = eventRepository.findEventByTopicIdAndEventId(pid, sid).get(0);
    //
    //        if (StringUtils.equals(event.getCreatedBy(), username)){
    //            eventRepository.delete(event);
    //            return ResponseEntity.ok(null);
    //        }else{
    //            return ResponseEntity.badRequest().build();
    //        }
    //    }
    //
    //    @ApiOperation("hide topic")
    //    @PutMapping("/topics/{pid}/hide")
    //    public ResponseEntity<?> hideTopic(@PathVariable String pid){
    //
    //        Topic topic = topicRepository.findById(TopicId.of(pid)).get();
    //        topic.setHidden(true);
    //        topicRepository.save(topic);
    //        return ResponseEntity.ok(null);
    //    }
    //
    //    @ApiOperation("show topic")
    //    @PutMapping("/topics/{pid}/show")
    //    public ResponseEntity<?> showTopic(@PathVariable String pid){
    //
    //        Topic topic = topicRepository.findById(TopicId.of(pid)).get();
    //        topic.setHidden(false);
    //        topicRepository.save(topic);
    //        return ResponseEntity.ok(null);
    //    }
}

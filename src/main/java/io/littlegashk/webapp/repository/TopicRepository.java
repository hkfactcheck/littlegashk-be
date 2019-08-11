package io.littlegashk.webapp.repository;

import com.google.common.collect.Lists;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@EnableScan
public interface TopicRepository extends CrudRepository<Topic, TopicId> {

    List<Topic> findAllByEventDateAndRecordIdStartsWith(String eventDate, String recordIdPrefix);

    default List<Topic> getAllTopicByDate(String date) {

        return findAllByEventDateAndRecordIdStartsWith(date, "topic-");
    }

    default List<Topic> getProgressByTopic(TopicId topicId) {

        Optional<Topic> topic = findById(topicId);
        if (topic.isEmpty()) {
            return List.of();
        }
        Set<String> children = topic.get().getChildren();
        List<Topic> progress = Lists.newArrayList(findAllById(children.stream()
                                                                      .map(TopicId::of)
                                                                      .filter(t->t.getRecordId().startsWith("progress-"))
                                                                      .collect(Collectors.toList())));

        return progress;
    }

    default List<Topic> getPublicResponseByTopic(TopicId topicId) {

        Optional<Topic> topic = findById(topicId);
        if (topic.isEmpty()) {
            return List.of();
        }
        Set<String> children = topic.get().getChildren();
        List<Topic> response = Lists.newArrayList(findAllById(children.stream()
                                                                      .map(TopicId::of)
                                                                      .filter(t->t.getRecordId().startsWith("response-"))
                                                                      .collect(Collectors.toList())));

        return response;
    }

}

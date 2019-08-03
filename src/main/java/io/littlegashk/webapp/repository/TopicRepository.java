package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.Event;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface TopicRepository extends CrudRepository<Topic, TopicId> {

    List<Topic> findAllByRecordIdAndGroupStartsWith(String recordId, String group);

    List<Topic> findAllByTopicIdAndRecordIdStartsWithAndGroupIsNull(String topicId, String recordId);

    default List<Topic> getAllInGroup(String group){

        return findAllByRecordIdAndGroupStartsWith("TOP", group);
    }

    default List<Topic> getProgressByTopicId(String topicId){

        return findAllByTopicIdAndRecordIdStartsWithAndGroupIsNull(topicId, "progress-");
    }

}

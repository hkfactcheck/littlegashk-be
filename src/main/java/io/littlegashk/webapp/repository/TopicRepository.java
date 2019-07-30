package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface TopicRepository extends CrudRepository<Topic, TopicId> {
    List<Topic> findAllByRecordId(String recordId);

    List<Topic> findAllByRecordIdAndGroupStartsWith(String recordId, String group);

    default List<Topic> getAllTopic() {

        return findAllByRecordId("META");
    }

    default List<Topic> getAllInGroup(String group){
        return findAllByRecordIdAndGroupStartsWith("META", group);
    }
}

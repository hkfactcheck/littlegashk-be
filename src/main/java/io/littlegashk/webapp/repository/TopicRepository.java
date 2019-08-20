package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.EntryType;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@EnableScan
public interface TopicRepository extends DynamoDBCrudRepository<Topic, TopicId> {

    Page<Topic> findTopicsBySortKeyAndLastUpdatedIsBefore(String sortKey, long lastUpdated, Pageable pageable);

    Page<Topic> findTopicsBySortKeyAndTopicIdStartsWith(String sortKey, String eventDate, Pageable pageable);

    default Page<Topic> getAllTopicUpdatedBefore(long lastUpdated, int page) {
        PageRequest pr = PageRequest.of(page, 10, Sort.Direction.DESC, "lastUpdated");
        return findTopicsBySortKeyAndLastUpdatedIsBefore(EntryType.TOPIC.name(), lastUpdated, pr);
    }

    default Page<Topic> getAllTopicByEventDate(String eventDate, int page) {
        PageRequest pr = PageRequest.of(page, 10, Sort.Direction.DESC, "topicId");
        return findTopicsBySortKeyAndTopicIdStartsWith(EntryType.TOPIC.name(), eventDate, pr);
    }

}

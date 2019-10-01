package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.EntryType;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import java.util.List;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBCrudRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.socialsignin.spring.data.dynamodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;


@EnableScan
public interface TopicRepository extends DynamoDBCrudRepository<Topic, TopicId> {

  Page<Topic> findTopicsBySortKeyAndLastUpdatedIsBefore(String sortKey, long lastUpdated, Pageable pageable);

  Page<Topic> findTopicsBySortKeyAndTopicIdStartsWith(String sortKey, String eventDate, Pageable pageable);

  default Page<Topic> getAllTopicUpdatedBefore(long lastUpdated, int page) {
    PageRequest pr = PageRequest.of(page, 10, Sort.Direction.DESC, "lastUpdated");
    return findTopicsBySortKeyAndLastUpdatedIsBefore(EntryType.TOPIC.name(), lastUpdated, pr);
  }

  default Page<Topic> getAllTopicByEventDate(String eventDate) {
    PageRequest pr = PageRequest.of(0, 10000, Sort.Direction.DESC, "topicId");
    return findTopicsBySortKeyAndTopicIdStartsWith(EntryType.TOPIC.name(), eventDate, pr);
  }

  Slice<Topic> findTopicsByTopicIdAndSortKeyIn(String topicId, List<String> sortKeys, Pageable pageable);

  default Topic findByTopicId(String topicId) {
    PageRequest pr = PageRequest.of(0, 1);
    List<String> allTypes = List.of(EntryType.TOPIC.name(), EntryType.PROGRESS.name(), EntryType.RESPONSE.name());
    return findTopicsByTopicIdAndSortKeyIn(topicId, allTypes, pr).get()
                                                                 .findFirst()
                                                                 .orElse(null);
  }


  @EnableScanCount
  Page<Topic> findTopicsBySortKeyIn(Pageable pageable, String... topicSortKeyValues);

}

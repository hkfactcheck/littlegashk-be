package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.TagTopic;
import io.littlegashk.webapp.entity.TagTopicId;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@EnableScan
public interface TagRepository extends DynamoDBPagingAndSortingRepository<TagTopic, TagTopicId> {

    Page<TagTopic> findTagTopicByTagKeyAndTopicIdBefore(String tagKey, String lastTopicId, Pageable pageable);

    default Page<TagTopic> findAllWithTag(String tag, String lastTopicId, int page) {
        PageRequest pr = PageRequest.of(page, 10, Sort.Direction.DESC, "topicId");
        return findTagTopicByTagKeyAndTopicIdBefore("TAG|" + tag, lastTopicId, pr);
    }

}

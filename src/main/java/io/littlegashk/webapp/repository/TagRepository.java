package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.TagTopic;
import io.littlegashk.webapp.entity.TagTopicId;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.socialsignin.spring.data.dynamodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@EnableScan
public interface TagRepository extends DynamoDBPagingAndSortingRepository<TagTopic, TagTopicId> {

    Page<TagTopic> findAllByTag(String tag, Pageable pageable);

    default Set<String> findAllTags(){
        boolean last = false;
        Set<String> tags  = new HashSet<>();
        while(!last) {
            Pageable pr = PageRequest.of(0, 1000);
            Page<TagTopic> someTags = findAllByTag("TAG", pr);
            last = someTags.isLast();
            tags.addAll(someTags.get().map(TagTopic::getTagString).collect(Collectors.toSet()));
        }
        return tags;
    }

    @EnableScanCount
    @Query
    Page<TagTopic> findTagTopicsByTopicIdAndTagKeyStartsWithAndTag(String topicId, String tagKey, String tag, Pageable pageable);

    default Page<TagTopic> findTopicTags(String topicId){
        PageRequest pr = PageRequest.of(0, 1000);
        return findTagTopicsByTopicIdAndTagKeyStartsWithAndTag(topicId, "TAG|", "TAG", pr);
    }

    Page<TagTopic> findTagTopicByTagKeyAndTopicIdBefore(String tagKey, String lastTopicId, Pageable pageable);

    default Page<TagTopic> findAllWithTag(String tag, String lastTopicId, int page) {
        PageRequest pr = PageRequest.of(page, 10, Sort.Direction.DESC, "topicId");
        return findTagTopicByTagKeyAndTopicIdBefore("TAG|" + tag, lastTopicId, pr);
    }

}

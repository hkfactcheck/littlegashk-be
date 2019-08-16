package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.TagTopic;
import io.littlegashk.webapp.entity.TagTopicId;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface TagRepository extends CrudRepository<TagTopic, TagTopicId> {

    List<TagTopic> findTagTopicByTagKeyAndTopicIdBefore(String tagKey, String lastTopicId);

    default List<TagTopic> findAllWithTag(String tag, String lastTopicId) {

        return findTagTopicByTagKeyAndTopicIdBefore("TAG|" + tag, lastTopicId);
    }

}

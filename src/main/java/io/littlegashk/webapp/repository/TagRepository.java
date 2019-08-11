package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.TagTopic;
import io.littlegashk.webapp.entity.TagTopicId;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.TopicId;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface TagRepository extends CrudRepository<TagTopic, TagTopicId> {

    List<TagTopic> findAllByTagAndTagIdStartsWith(String tag, String tagIdPrefix);

    List<TagTopic> findAllByTag(String group);

    default List<TagTopic> findAllWithTag(String tag){
        return findAllByTagAndTagIdStartsWith("TAG", "TAG|"+ tag);
    }

    default List<TagTopic> findAllTags(){
        return findAllByTag("TAG");
    }
}

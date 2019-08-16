package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.*;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface ChildRelationRepository extends CrudRepository<ChildRelation, ChildRelationId> {

    Page<ChildRelation> findAllByTopicIdAndChildRelationStartsWithAndChildRelationBefore(String topicId, String prefix, String lastTopicId,
                                                                                         Pageable pageable);

    default Page<ChildRelation> getProgress(String topicId, String lastChildId, int page) {
        PageRequest pr = PageRequest.of(page, 10, Sort.Direction.DESC, "childRelation");
        return findAllByTopicIdAndChildRelationStartsWithAndChildRelationBefore(topicId, EntryType.PROGRESS.name()+"|", EntryType.PROGRESS.name()+
                                                                                                                        "|" +lastChildId, pr);
    }

    default Page<ChildRelation> getResponse(String topicId, String lastChildId, int page) {
        PageRequest pr = PageRequest.of(page, 10, Sort.Direction.DESC, "childRelation");
        return findAllByTopicIdAndChildRelationStartsWithAndChildRelationBefore(topicId, EntryType.RESPONSE.name()+"|",
                                                                                EntryType.RESPONSE.name()+"|" +lastChildId, pr);
    }

}

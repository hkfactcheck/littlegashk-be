package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.ChildRelation;
import io.littlegashk.webapp.entity.ChildRelationId;
import io.littlegashk.webapp.entity.EntryType;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface ChildRelationRepository extends DynamoDBPagingAndSortingRepository<ChildRelation, ChildRelationId> {
    @Query
    Page<ChildRelation> findChildRelationsByTopicIdAndSortKeyBetween(String topicId, String sortKeyStart, String sortKeyEnd, Pageable pageable);

    default Page<ChildRelation> getProgress(String topicId, String lastChildId, int page) {

        PageRequest pr = PageRequest.of(page, 10, Sort.Direction.DESC, "sortKey");
        return findChildRelationsByTopicIdAndSortKeyBetween(topicId,

                                                            EntryType.PROGRESS.name() + "|0",
                                                            EntryType.PROGRESS.name() + "|" + lastChildId.substring(0, lastChildId.length()-1),
                                                            pr);
    }

    default Page<ChildRelation> getResponse(String topicId, String lastChildId, int page) {

        PageRequest pr = PageRequest.of(page, 10, Sort.Direction.DESC, "sortKey");
        return findChildRelationsByTopicIdAndSortKeyBetween(topicId,

                                                            EntryType.RESPONSE.name() + "|0",
                                                            EntryType.RESPONSE.name() + "|" + lastChildId.substring(0, lastChildId.length()-1),
                                                            pr);
    }

}

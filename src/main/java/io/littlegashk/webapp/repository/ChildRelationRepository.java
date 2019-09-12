package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.ChildRelation;
import io.littlegashk.webapp.entity.ChildRelationId;
import io.littlegashk.webapp.entity.EntryType;
import java.util.List;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.socialsignin.spring.data.dynamodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@EnableScan
public interface ChildRelationRepository extends DynamoDBPagingAndSortingRepository<ChildRelation, ChildRelationId> {

  @Query
  Page<ChildRelation> findChildRelationsByTopicIdAndSortKeyBetween(String topicId, String sortKeyStart, String sortKeyEnd, Pageable pageable);

  @EnableScanCount
  @Query
  Page<ChildRelation> findChildRelationsBySortKeyIn(List<String> sortKeys, Pageable pageable);

  default Page<ChildRelation> getProgress(String topicId, String lastChildId, int page) {
    return getProgress(topicId, lastChildId, page, 10);
  }

  default Page<ChildRelation> getResponse(String topicId, String lastChildId, int page) {
    return getResponse(topicId, lastChildId, page, 10);
  }

  default Page<ChildRelation> getProgress(String topicId, String lastChildId, int page, int size) {

    PageRequest pr = PageRequest.of(page, size, Sort.Direction.DESC, "sortKey");
    return findChildRelationsByTopicIdAndSortKeyBetween(topicId,

                                                        EntryType.PROGRESS.name() + "|0",
                                                        EntryType.PROGRESS.name() + "|" + lastChildId.substring(0, lastChildId.length() - 1), pr);
  }

  default Page<ChildRelation> getResponse(String topicId, String lastChildId, int page, int size) {

    PageRequest pr = PageRequest.of(page, size, Sort.Direction.DESC, "sortKey");
    return findChildRelationsByTopicIdAndSortKeyBetween(topicId,

                                                        EntryType.RESPONSE.name() + "|0",
                                                        EntryType.RESPONSE.name() + "|" + lastChildId.substring(0, lastChildId.length() - 1), pr);
  }


  default Page<ChildRelation> getParents(String childTopicId){
    PageRequest pr = PageRequest.of(0, 1000);
    return findChildRelationsBySortKeyIn(List.of("PROGRESS|"+childTopicId, "RESPONSE|"+ childTopicId), pr);
  }


}

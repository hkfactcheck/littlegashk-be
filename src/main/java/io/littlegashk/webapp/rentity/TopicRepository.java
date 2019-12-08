package io.littlegashk.webapp.rentity;

import io.littlegashk.webapp.entity.EntryType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID>, QuerydslPredicateExecutor<Topic> {

  Page<Topic> findByTypeAndLastUpdatedBeforeOrderByLastUpdatedDesc(EntryType type, long lastUpdated, Pageable pageable);

  List<Topic> findAllByEventDateBetweenOrderByEventDateDesc(LocalDateTime start, LocalDateTime end);

  Page<Topic> findTopicsByParentsContainsAndTypeOrderByEventDateDesc(Topic parent, EntryType type, Pageable pageable);

  List<Topic> findAllBySeqIsNotNullOrderBySeqDesc();

  Page<Topic> findByTagRecordContains(Pageable pageable, Tag tagRecord);

  List<Topic> findAllByTagRecordIn(List<Tag> tagRecords);
}

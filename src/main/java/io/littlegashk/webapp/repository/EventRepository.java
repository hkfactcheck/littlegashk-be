package io.littlegashk.webapp.repository;

import io.littlegashk.webapp.entity.Event;
import io.littlegashk.webapp.entity.EventId;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableScan
public interface EventRepository extends CrudRepository<Event, EventId> {

    List<Event> findEventsByTopicIdAndEventIdStartingWithAndRatingGreaterThanEqual(String topicId, String eventIdPrefix, int ratingMin);

    List<Event> findEventByTopicIdAndEventId(String topicId, String eventId);

    default List<Event> getApprovedEventsByTopicId(String topicId){
        return findEventsByTopicIdAndEventIdStartingWithAndRatingGreaterThanEqual(topicId, "event-", 1);
    }

    default List<Event> getAllEventsByTopicId(String topicId){
        //super admin can "soft delete" event by setting rating < -100
        return findEventsByTopicIdAndEventIdStartingWithAndRatingGreaterThanEqual(topicId, "event-", -100);
    }

}

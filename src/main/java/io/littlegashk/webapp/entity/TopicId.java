package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicId implements Serializable {

    @DynamoDBHashKey
    private String topicId;
    @DynamoDBRangeKey
    private String sortKey;

    public static TopicId fromEventDate (String eventDate) {
        return new TopicId(eventDate + "|" + System.currentTimeMillis(), EntryType.TOPIC.name());
    }

    public static TopicId fromEventDate (String eventDate, String sortKey) {
        return new TopicId(eventDate + "|" + System.currentTimeMillis(), sortKey);
    }

    public static TopicId of(String topicId){
        return new TopicId(topicId, EntryType.TOPIC.name());
    }
}

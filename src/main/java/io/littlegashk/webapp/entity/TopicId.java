package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicId implements Serializable {

    @DynamoDBHashKey
    private String id;
    @DynamoDBRangeKey
    private String recordId;

    public static TopicId of(String topicId){
        return new TopicId(topicId, "TOP");
    }

    public static TopicId of(String topicId, String recordId){
        return new TopicId(topicId, recordId);
    }

    @Override
    public String toString(){
        return id;
    }

}

package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
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
    private String eventDate;
    @DynamoDBRangeKey
    private String recordId;

    public static TopicId of(String eventDate, String recordId){
        return new TopicId(eventDate, recordId);
    }

    public static TopicId of(String delimited){
        String[] split = delimited.split("\\|");
        return TopicId.of(split[0], split[1]);
    }
    @Override
    public String toString(){
        return eventDate+"|"+recordId;
    }
}

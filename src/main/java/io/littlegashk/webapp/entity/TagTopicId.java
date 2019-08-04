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
public class TagTopicId implements Serializable {

    @DynamoDBHashKey
    private String tag;
    @DynamoDBRangeKey
    private String topicRecordId;

    public static TagTopicId of(String tag, String topicId, String recordId){

        return new TagTopicId(tag, topicId+ "|" + recordId);
    }
}

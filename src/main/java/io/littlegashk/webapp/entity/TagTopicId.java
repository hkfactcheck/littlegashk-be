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
    private String topicId;
    @DynamoDBRangeKey
    private String tagKey;

    public static TagTopicId of(String topicId, String tag) {

        return new TagTopicId(topicId, "TAG|" + tag);
    }
}

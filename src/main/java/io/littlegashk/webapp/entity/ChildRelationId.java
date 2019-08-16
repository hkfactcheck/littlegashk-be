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
public class ChildRelationId implements Serializable {

    @DynamoDBHashKey
    private String topicId;
    @DynamoDBRangeKey
    private String childRelation;

    public static ChildRelationId of(String topicId, EntryType type, String childId) {

        return new ChildRelationId(topicId, type.name() + "|" + childId);
    }
}

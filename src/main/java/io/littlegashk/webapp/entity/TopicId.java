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

    public static TopicId of(String id){
        return new TopicId(id, "TOP");
    }

    @Override
    public String toString(){
        return id;
    }

}

package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedJson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlegashk.webapp.DynamoDbSchemaInitializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@DynamoDBTable(tableName = DynamoDbSchemaInitializer.TABLE_LITTLEGAS)
@Data
public class Topic {

    @Id
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TopicId id;

    @DynamoDBHashKey(attributeName = "pid")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName ="sid-pid-index")
    public String getTopicId() {

        return id == null ? null : id.getTopicId();
    }

    public void setTopicId(String topicId) {

        if (this.id == null) {
            this.id = new TopicId();
        }
        this.id.setTopicId(topicId);
    }

    @DynamoDBRangeKey(attributeName = "sid")
    @JsonIgnore
    @DynamoDBIndexHashKey(globalSecondaryIndexNames ={"sid-pid-index", "sid-last-updated-index"})
    public String getSortKey() {

        return id == null ? null : id.getSortKey();
    }

    public void setSortKey(String sortKey) {

        if (this.id == null) {
            this.id = new TopicId();
        }
        this.id.setSortKey(sortKey);
    }



    // Arbitrary fields below, change as you want :)
    @Schema(example="This is topic/progress title")
    private String title;

    @Schema(example="This is summary")
    private String summary;

    @DynamoDBTypeConvertedEnum
    @Schema(example="TOPIC", description = "Not required when add or edit", allowableValues = "{TOPIC,PROGRESS,RESPONSE}")
    private EntryType type;

    @DynamoDBTypeConvertedJson
    private List<String> relatedFiles;

    @DynamoDBTypeConvertedJson
    private Set<String> relatedTopics;

    @DynamoDbTypeConvertedReferenceList
    private List<Reference> references;

    @DynamoDBTypeConvertedJson
    private Set<String> tags;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName ="sid-last-updated-index")
    @Schema(description="Last updated timestamp")
    private long lastUpdated;


    private String eventDate;

    private String imageUrl;

    private String uid;

    private Integer seq;


}

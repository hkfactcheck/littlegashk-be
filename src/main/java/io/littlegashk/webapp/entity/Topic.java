package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.littlegashk.webapp.DynamoDbSchemaInitializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jdk.jfr.EventType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import springfox.documentation.annotations.ApiIgnore;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@DynamoDBTable(tableName = DynamoDbSchemaInitializer.TABLE_LITTLEGAS)
@Data
public class Topic {

    @Id
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TopicId id;

    @DynamoDBHashKey(attributeName = "pid")
    @ApiModelProperty(position = 1, example = "2019-08-01|1565877016020",
            notes = "Treat as unique record Id. Contains event date and record creation timestamp")
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
    @ApiModelProperty(position = 2, example="This is topic/progress title")
    private String title;

    @ApiModelProperty(position = 3, example="This is summary")
    private String summary;

    @DynamoDBTypeConvertedEnum
    @ApiModelProperty(position = 5, example="TOPIC", notes = "Not required when add or edit", allowableValues = "{TOPIC,PROGRESS,PUBLIC_RESPONSE}")
    private EntryType type;

    @DynamoDBTypeConvertedJson
    @ApiModelProperty(position = 6)
    private List<String> relatedFiles;

    @DynamoDBTypeConvertedJson
    @ApiModelProperty(position = 7)
    private List<String> relatedTopics;

    @DynamoDBTypeConvertedJson
    @ApiModelProperty(position = 8)
    private List<String> references;

    @DynamoDBTypeConvertedJson
    @ApiModelProperty(position = 9)
    private Set<String> tags;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName ="sid-last-updated-index")
    @ApiModelProperty(position = 11, example="Last updated timestamp")
    private long lastUpdated;


    @ApiModelProperty(position = 12)
    private String eventDate;


}

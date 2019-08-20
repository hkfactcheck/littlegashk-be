package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import io.littlegashk.webapp.DynamoDbSchemaInitializer;
import lombok.*;
import org.springframework.data.annotation.Id;

@DynamoDBTable(tableName = DynamoDbSchemaInitializer.TABLE_LITTLEGAS)
@Data
@NoArgsConstructor
public class UrlTopic {

    public UrlTopic(UrlTopicId id){
        this.id=id;
    }

    @Id
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UrlTopicId id;

    @DynamoDBHashKey(attributeName = "pid")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName ="sid-pid-index")
    public String getTopicId(){
        return id == null?null: id.getTopicId();
    }

    public void setTopicId(String topicId){
        if(this.id == null){
            this.id =new UrlTopicId();
        }
        this.id.setTopicId(topicId);
    }

    @DynamoDBRangeKey(attributeName = "sid")
    @DynamoDBIndexHashKey(globalSecondaryIndexName ="sid-pid-index")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "tag-sid-index")
    public String getUrl(){
        return id == null?null: id.getUrl();
    }

    public void setUrl(String tagKey){
        if(this.id == null){
            this.id =new UrlTopicId();
        }
        this.id.setUrl(tagKey);
    }

    @DynamoDBIgnore
    public String getUrlString(){
        return id.getUrl().split("\\|", 2)[1];
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "tag-sid-index")
    @DynamoDBAutoGeneratedDefault("URL")
    private String tag;
}

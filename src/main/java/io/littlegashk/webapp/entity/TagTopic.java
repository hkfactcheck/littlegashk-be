package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import io.littlegashk.webapp.DynamoDbSchemaInitializer;
import lombok.*;
import org.springframework.data.annotation.Id;

@DynamoDBTable(tableName = DynamoDbSchemaInitializer.TABLE_LITTLEGAS)
@Data
@NoArgsConstructor
public class TagTopic {

    public TagTopic(TagTopicId id){
        this.id=id;
    }

    @Id
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private TagTopicId id;

    @DynamoDBHashKey(attributeName = "pid")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName ="sid-pid-index")
    public String getTopicId(){
        return id == null?null: id.getTopicId();
    }

    public void setTopicId(String topicId){
        if(this.id == null){
            this.id =new TagTopicId();
        }
        this.id.setTopicId(topicId);
    }

    @DynamoDBRangeKey(attributeName = "sid")
    @DynamoDBIndexHashKey(globalSecondaryIndexName ="sid-pid-index")
    public String getTagKey(){
        return id == null?null: id.getTagKey();
    }

    public void setTagKey(String tagKey){
        if(this.id == null){
            this.id =new TagTopicId();
        }
        this.id.setTagKey(tagKey);
    }

    @DynamoDBIgnore
    public String getTag(){
        return id.getTagKey().split("\\|", 2)[1];
    }
}

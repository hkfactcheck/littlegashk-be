package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import io.littlegashk.webapp.DynamoDbSchemaInitializer;
import lombok.*;
import org.springframework.data.annotation.Id;

@DynamoDBTable(tableName = DynamoDbSchemaInitializer.TABLE_LITTLEGAS)
@Data
@NoArgsConstructor
public class ChildRelation {

    public ChildRelation(ChildRelationId id){
        this.id=id;
    }

    @Id
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private ChildRelationId id;

    @DynamoDBHashKey(attributeName = "pid")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName ="sid-pid-index")
    public String getTopicId(){
        return id == null?null: id.getTopicId();
    }

    public void setTopicId(String topicId){
        if(this.id == null){
            this.id =new ChildRelationId();
        }
        this.id.setTopicId(topicId);
    }

    @DynamoDBRangeKey(attributeName = "sid")
    @DynamoDBIndexHashKey(globalSecondaryIndexName ="sid-pid-index")
    public String getChildRelation(){
        return id == null?null: id.getChildRelation();
    }

    public void setChildRelation(String childRelation){
        if(this.id == null){
            this.id =new ChildRelationId();
        }
        this.id.setChildRelation(childRelation);
    }

    @DynamoDBIgnore
    public String getChildTopicId(){
        return id.getChildRelation().split("\\|", 2)[1];
    }
}

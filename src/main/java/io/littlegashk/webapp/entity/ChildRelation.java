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
    public String getSortKey(){
        return id == null?null: id.getSortKey();
    }

    public void setSortKey(String sortKey){
        if(this.id == null){
            this.id =new ChildRelationId();
        }
        this.id.setSortKey(sortKey);
    }

    @DynamoDBIgnore
    public String getChildTopicId(){
        return id.getSortKey().split("\\|", 2)[1];
    }
}

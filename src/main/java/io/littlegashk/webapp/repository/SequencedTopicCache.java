package io.littlegashk.webapp.repository;

import static io.littlegashk.webapp.DynamoDbSchemaInitializer.TABLE_LITTLEGAS;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SequencedTopicCache {

  @Autowired
  AmazonDynamoDB db;

  private final Set<String> TOPIC_IDS = new HashSet<>();

  public void clear() {
    TOPIC_IDS.clear();
  }

  public synchronized Set<String> getSequencedTopicIds() {
    if (TOPIC_IDS.isEmpty()) {
      ScanResult queryResult = db.scan(new ScanRequest().withTableName(TABLE_LITTLEGAS)
                                                        .withFilterExpression("seq > :val")
                                                        .withProjectionExpression("pid")
                                                        .withExpressionAttributeValues(Map.of(":val", new AttributeValue().withN("0"))));

      queryResult.getItems()
                 .stream()
                 .map(m -> m.get("pid")
                            .getS())
                 .forEach(TOPIC_IDS::add);
    } return TOPIC_IDS;
  }

}

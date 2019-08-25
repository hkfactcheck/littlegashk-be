package io.littlegashk.webapp;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import io.littlegashk.webapp.entity.Reference;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.UrlTopic;
import io.littlegashk.webapp.repository.TopicRepository;
import io.littlegashk.webapp.repository.UrlRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class DynamoDbSchemaInitializer implements ApplicationListener<ContextRefreshedEvent> {

    public static final String TABLE_LITTLEGAS = "littlegas";
    @Autowired
    AmazonDynamoDB db;

    @Autowired
    TopicRepository topicRepository;

    @Autowired
    UrlRepository urlRepository;

    @Autowired
    AdminController controller;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        log.info("Running dynamo init....");
        ListTablesResult listTablesResult = db.listTables();
        if (!listTablesResult.getTableNames().contains(TABLE_LITTLEGAS)) {
            log.info("Table not found, creating....");
            GlobalSecondaryIndex sidPidIndex = new GlobalSecondaryIndex().withIndexName("sid-pid-index")
                                                                         .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                              .withAttributeName("sid"),
                                                                                        new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                              .withAttributeName("pid"))
                                                                         .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(
                                                                                 5L).withWriteCapacityUnits(3L))
                                                                         .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
            GlobalSecondaryIndex sidLastUpdatedIndex = new GlobalSecondaryIndex().withIndexName("sid-last-updated-index")
                                                                                 .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                                      .withAttributeName("sid"),
                                                                                                new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                                      .withAttributeName("lastUpdated"))
                                                                                 .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(
                                                                                         5L).withWriteCapacityUnits(3L))
                                                                                 .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
            GlobalSecondaryIndex tagSidIndex = new GlobalSecondaryIndex().withIndexName("tag-sid-index")
                                                                         .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                              .withAttributeName("tag"),
                                                                                        new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                              .withAttributeName("sid"))
                                                                         .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(
                                                                                 1L).withWriteCapacityUnits(1L))
                                                                         .withProjection(new Projection().withProjectionType(ProjectionType.KEYS_ONLY));

            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(TABLE_LITTLEGAS)
                                                                            .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                                 .withAttributeName("pid"),
                                                                                           new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                                 .withAttributeName("sid"))
                                                                            .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(
                                                                                    15L).withWriteCapacityUnits(10L))
                                                                            .withGlobalSecondaryIndexes(sidPidIndex, sidLastUpdatedIndex, tagSidIndex)
                                                                            .withAttributeDefinitions(new AttributeDefinition().withAttributeName(
                                                                                    "pid").withAttributeType(ScalarAttributeType.S),
                                                                                                      new AttributeDefinition().withAttributeName(
                                                                                                              "sid")
                                                                                                                               .withAttributeType(
                                                                                                                                       ScalarAttributeType.S),
                                                                                                      new AttributeDefinition().withAttributeName(
                                                                                                              "lastUpdated")
                                                                                                                               .withAttributeType(
                                                                                                                                       ScalarAttributeType.N),
                                                                                                      new AttributeDefinition().withAttributeName(
                                                                                                              "tag")
                                                                                                                               .withAttributeType(
                                                                                                                                       ScalarAttributeType.S));
            db.createTable(createTableRequest);
            log.info("Table creation done");
        }

        migrate("M1", ()->{
            List<Topic> allTopics = new ArrayList<>();
            Page<Topic> topics = topicRepository.findTopicsBySortKeyIn(PageRequest.of(0,10), "TOPIC", "PROGRESS", "RESPONSE");
            allTopics.addAll(topics.getContent());
            while(topics.hasNext()){
                topics = topicRepository.findTopicsBySortKeyIn(topics.nextPageable(), "TOPIC", "PROGRESS", "RESPONSE");
                allTopics.addAll(topics.getContent());
            }

            for(Topic topic: allTopics){
                List<Reference> references = topic.getReferences();
                if(references!=null) {
                    for (Reference r : references) {
                        if (StringUtils.isNotBlank(r.getLink())) {
                            UrlTopic urlTopic = new UrlTopic();
                            urlTopic.setUrl("URL|" + r.getLink().trim());
                            urlTopic.setTopicId(topic.getTopicId());
                            urlRepository.save(urlTopic);
                        }
                    }
                }
            }
        });

        migrate("M2", ()->{
            List<Topic> allTopics = new ArrayList<>();
            Page<Topic> topics = topicRepository.findTopicsBySortKeyIn(PageRequest.of(0,10), "TOPIC");
            allTopics.addAll(topics.getContent());
            while(topics.hasNext()){
                topics = topicRepository.findTopicsBySortKeyIn(topics.nextPageable(), "TOPIC");
                allTopics.addAll(topics.getContent());
            }

            for(Topic topic: allTopics){
                controller.findAndAddRelatedTopic(topic);
                topicRepository.save(topic);
            }
        });


    }

    private void migrate(String mid, Runnable perform){
        List<Map<String, AttributeValue>> migrationRecord = db.query(new QueryRequest().withTableName(TABLE_LITTLEGAS)
                                                                                       .withKeyConditionExpression("pid = :pid AND sid = :sid")
                                                                                       .withExpressionAttributeValues(Map.of(":pid",
                                                                                                                             new AttributeValue().withS(
                                                                                                                                     "migration"),
                                                                                                                             ":sid",
                                                                                                                             new AttributeValue().withS(
                                                                                                                                     mid))))
                                                              .getItems();
        if (migrationRecord.size()==0){
            perform.run();;
        }

        db.putItem(TABLE_LITTLEGAS, Map.of("pid", new AttributeValue().withS("migration"), "sid", new AttributeValue().withS(mid)));
    }
}

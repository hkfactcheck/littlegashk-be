package io.littlegashk.webapp;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.google.common.collect.Maps;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import io.littlegashk.webapp.entity.ChildRelation;
import io.littlegashk.webapp.entity.EntryType;
import io.littlegashk.webapp.entity.Reference;
import io.littlegashk.webapp.entity.Topic;
import io.littlegashk.webapp.entity.UrlTopic;
import io.littlegashk.webapp.rentity.TopicMapper;
import io.littlegashk.webapp.repository.ChildRelationRepository;
import io.littlegashk.webapp.repository.OldTopicRepository;
import io.littlegashk.webapp.repository.UrlRepository;
import io.littlegashk.webapp.service.MigrationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class DynamoDbSchemaInitializer implements ApplicationListener<ContextRefreshedEvent> {

  public static final String TABLE_LITTLEGAS = "littlegas";
  @Autowired
  AmazonDynamoDB db;

  @Autowired
  OldTopicRepository topicRepository;

  @Autowired
  ChildRelationRepository childRelationRepository;

  @Autowired
  io.littlegashk.webapp.rentity.TopicRepository rTopicRepo;

  @Autowired
  io.littlegashk.webapp.rentity.TagRepository rTagRepo;

  @Autowired
  UrlRepository urlRepository;

  @Autowired
  AdminController controller;

  @Autowired
  TopicMapper mapper;

  @Autowired
  MigrationService migrationService;


  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {

    log.info("Running dynamo init....");
    ListTablesResult listTablesResult = db.listTables();
    if (!listTablesResult.getTableNames()
                         .contains(TABLE_LITTLEGAS)) {
      log.info("Table not found, creating....");
      GlobalSecondaryIndex sidPidIndex = new GlobalSecondaryIndex().withIndexName("sid-pid-index")
                                                                   .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                        .withAttributeName("sid"),
                                                                                  new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                        .withAttributeName("pid"))
                                                                   .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(5L)
                                                                                                                         .withWriteCapacityUnits(3L))
                                                                   .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
      GlobalSecondaryIndex sidLastUpdatedIndex = new GlobalSecondaryIndex().withIndexName("sid-last-updated-index")
                                                                           .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                                .withAttributeName("sid"),
                                                                                          new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                                .withAttributeName("lastUpdated"))
                                                                           .withProvisionedThroughput(
                                                                               new ProvisionedThroughput().withReadCapacityUnits(5L)
                                                                                                          .withWriteCapacityUnits(3L))
                                                                           .withProjection(new Projection().withProjectionType(ProjectionType.ALL));
      GlobalSecondaryIndex tagSidIndex = new GlobalSecondaryIndex().withIndexName("tag-sid-index")
                                                                   .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                        .withAttributeName("tag"),
                                                                                  new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                        .withAttributeName("sid"))
                                                                   .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L)
                                                                                                                         .withWriteCapacityUnits(1L))
                                                                   .withProjection(new Projection().withProjectionType(ProjectionType.KEYS_ONLY));

      CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(TABLE_LITTLEGAS)
                                                                      .withKeySchema(new KeySchemaElement().withKeyType(KeyType.HASH)
                                                                                                           .withAttributeName("pid"),
                                                                                     new KeySchemaElement().withKeyType(KeyType.RANGE)
                                                                                                           .withAttributeName("sid"))
                                                                      .withProvisionedThroughput(
                                                                          new ProvisionedThroughput().withReadCapacityUnits(15L)
                                                                                                     .withWriteCapacityUnits(10L))
                                                                      .withGlobalSecondaryIndexes(sidPidIndex, sidLastUpdatedIndex, tagSidIndex)
                                                                      .withAttributeDefinitions(new AttributeDefinition().withAttributeName("pid")
                                                                                                                         .withAttributeType(
                                                                                                                             ScalarAttributeType.S),
                                                                                                new AttributeDefinition().withAttributeName("sid")
                                                                                                                         .withAttributeType(
                                                                                                                             ScalarAttributeType.S),
                                                                                                new AttributeDefinition().withAttributeName(
                                                                                                    "lastUpdated")
                                                                                                                         .withAttributeType(
                                                                                                                             ScalarAttributeType.N),
                                                                                                new AttributeDefinition().withAttributeName("tag")
                                                                                                                         .withAttributeType(
                                                                                                                             ScalarAttributeType.S));
      db.createTable(createTableRequest);
      log.info("Table creation done");
    }

    migrate("M1", () -> {
      List<Topic> allTopics = new ArrayList<>();
      Page<Topic> topics = topicRepository.findTopicsBySortKeyIn(PageRequest.of(0, 10), "TOPIC", "PROGRESS", "RESPONSE");
      allTopics.addAll(topics.getContent());
      while (topics.hasNext()) {
        topics = topicRepository.findTopicsBySortKeyIn(topics.nextPageable(), "TOPIC", "PROGRESS", "RESPONSE");
        allTopics.addAll(topics.getContent());
      }

      for (Topic topic : allTopics) {
        List<Reference> references = topic.getReferences();
        if (references != null) {
          for (Reference r : references) {
            if (StringUtils.isNotBlank(r.getLink())) {
              UrlTopic urlTopic = new UrlTopic();
              urlTopic.setUrl("URL|" + r.getLink()
                                        .trim());
              urlTopic.setTopicId(topic.getTopicId());
              urlRepository.save(urlTopic);
            }
          }
        }
      }
    });

    migrate("M3", () -> {
      Page<Topic> topics = topicRepository.findTopicsBySortKeyIn(PageRequest.of(0, 10), "TOPIC", "PROGRESS", "RESPONSE");
      List<Topic> allTopics = new ArrayList<>(topics.getContent());
      while (topics.hasNext()) {
        topics = topicRepository.findTopicsBySortKeyIn(topics.nextPageable(), "TOPIC", "PROGRESS", "RESPONSE");
        allTopics.addAll(topics.getContent());
      }

      List<io.littlegashk.webapp.rentity.Topic> list = allTopics.stream()
                                                                .map(mapper::migrateToNewTopic)
                                                                .collect(Collectors.toList());
      Map<String, io.littlegashk.webapp.rentity.Reference> referenceMap = new HashMap<>();
      Map<String, io.littlegashk.webapp.rentity.Topic> topicMap = Maps.uniqueIndex(list, io.littlegashk.webapp.rentity.Topic::getOldId);
      topicMap.values()
              .forEach(t -> {
                if (t.getType() == EntryType.TOPIC) {
                  //find children
                  List<ChildRelation> children = new ArrayList<>();
                  children.addAll(childRelationRepository.getProgress(t.getOldId(), "9999", 0)
                                                         .getContent());
                  children.addAll(childRelationRepository.getResponse(t.getOldId(), "9999", 0)
                                                         .getContent());
                  Set<io.littlegashk.webapp.rentity.Topic> topicSet = children.stream()
                                                                              .map(ChildRelation::getChildTopicId)
                                                                              .map(topicMap::get)
                                                                              .filter(Objects::nonNull)
                                                                              .collect(Collectors.toSet());
                  t.setChildren(topicSet);

                }
                if(null!=t.getReferences()){
                  t.setReferences(t.getReferences().stream().map(r->{
                    if(referenceMap.containsKey(r.getHash())){
                      return referenceMap.get(r.getHash());
                    }else{
                      referenceMap.put(r.getHash(), r);
                      return r;
                    }
                  }).collect(Collectors.toSet()));
                }
              });

      rTopicRepo.saveAll(list);
    });

    migrate("M4", () -> {
      migrationService.migrateTags();
    });
  }

  private void migrate(String mid, Runnable perform) {
    List<Map<String, AttributeValue>> migrationRecord = db.query(new QueryRequest().withTableName(TABLE_LITTLEGAS)
                                                                                   .withKeyConditionExpression("pid = :pid AND sid = :sid")
                                                                                   .withExpressionAttributeValues(
                                                                                       Map.of(":pid", new AttributeValue().withS("migration"), ":sid",
                                                                                              new AttributeValue().withS(mid))))
                                                          .getItems();
    if (migrationRecord.size() == 0) {
      perform.run();
      ;
    }

    db.putItem(TABLE_LITTLEGAS, Map.of("pid", new AttributeValue().withS("migration"), "sid", new AttributeValue().withS(mid)));
  }
}

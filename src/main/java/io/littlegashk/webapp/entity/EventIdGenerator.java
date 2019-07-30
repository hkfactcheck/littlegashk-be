package io.littlegashk.webapp.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerateStrategy;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGenerator;

import java.util.UUID;

public class EventIdGenerator implements DynamoDBAutoGenerator<String> {
    @Override
    public DynamoDBAutoGenerateStrategy getGenerateStrategy() {

        return DynamoDBAutoGenerateStrategy.CREATE;
    }

    @Override
    public String generate(String currentValue) {

        return "event-" + UUID.randomUUID().toString();
    }
}

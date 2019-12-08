package io.littlegashk.webapp.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverterFactory;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableDynamoDBRepositories(basePackages = "io.littlegashk.webapp.repository")
@EnableJpaRepositories(basePackages = "io.littlegashk.webapp.rentity")
@EnableTransactionManagement
public class DynamoDBConfig {

    @Value("${dynamodb.endpoint:#{null}}")
    private String endpoint;

    @Bean
    @Primary
    public DynamoDBMapperConfig dynamoDBMapperConfig() {

        return new DynamoDBMapperConfig.Builder()
                .withTableNameResolver(DynamoDBMapperConfig.DefaultTableNameResolver.INSTANCE)
                .withTypeConverterFactory(DynamoDBTypeConverterFactory.standard())
                .withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.ITERATION_ONLY).build();
    }

    @Bean
    @Primary
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB, DynamoDBMapperConfig config) {

        return new DynamoDBMapper(amazonDynamoDB, config);
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {

        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
        if (StringUtils.isNotBlank(endpoint)) {
            //Local dynamo db
            builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy", "dummy")));
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, ""));
        }
        return builder.enableEndpointDiscovery().build();
    }
}

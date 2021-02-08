package by.mjc.utils;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

public class AwsClientFactory {
    private static AwsClientFactory awsClientFactory;
    private AmazonDynamoDB dynamoDBClient;

    private AwsClientFactory() {
    }

    public static AwsClientFactory getInstance() {
        if (awsClientFactory == null) {
            awsClientFactory = new AwsClientFactory();
        }

        return awsClientFactory;
    }

    public AmazonDynamoDB getDynamoDBClient() {
        if (dynamoDBClient == null) {
            dynamoDBClient = AmazonDynamoDBClientBuilder
                    .standard()
                    .withRegion(Regions.fromName(System.getenv("AWS_REGION")))
                    .build();
        }

        return dynamoDBClient;
    }
}

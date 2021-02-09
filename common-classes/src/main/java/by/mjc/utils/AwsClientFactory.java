package by.mjc.utils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
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

    public AWSCredentialsProvider getCredentials() {
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    }

    public Regions getRegion() {
        return Regions.fromName(System.getenv("AWS_DEFAULT_REGION"));
    }

    public AmazonDynamoDB getDynamoDBClient() {
        if (dynamoDBClient == null) {
            dynamoDBClient = AmazonDynamoDBClientBuilder
                    .standard()
                    .withCredentials(getCredentials())
                    .withRegion(getRegion())
                    .build();
        }

        return dynamoDBClient;
    }
}

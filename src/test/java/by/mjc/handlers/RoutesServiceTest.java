package by.mjc.handlers;

import by.mjc.entities.Route;
import by.mjc.entities.SearchRoutesRequest;
import by.mjc.services.RoutesService;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.*;
import init.AwsDynamoDbLocalTestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoutesServiceTest {
    private static AmazonDynamoDBLocal amazonDynamoDBLocal;

    @BeforeAll
    public static void init() {
        AwsDynamoDbLocalTestUtils.initSqLite();
        amazonDynamoDBLocal = DynamoDBEmbedded.create();
        AmazonDynamoDB amazonDynamoDB = amazonDynamoDBLocal.amazonDynamoDB();
        createTable(amazonDynamoDB, "Routes", "id");
    }

    @AfterAll
    public static void tearDownClass() {
        amazonDynamoDBLocal.shutdown();
    }

    @Test
    public void test() {
        RoutesService routesService = new RoutesService(amazonDynamoDBLocal.amazonDynamoDB());
        Route route = new Route();
        route.setKml("kml");
        Set<String> tags = new HashSet<>();
        tags.add("tag1");
        route.setTags(tags);
        routesService.saveRoute(route);

        SearchRoutesRequest request = new SearchRoutesRequest();
        request.setTags(List.of("tag1"));
        List<Route> routes = routesService.getRoutesByTags(request);
        Assertions.assertEquals(routes.size(), 1);
    }

    private static void createTable(AmazonDynamoDB ddb, String tableName, String hashKeyName) {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition(hashKeyName, ScalarAttributeType.S));

        List<KeySchemaElement> ks = new ArrayList<>();
        ks.add(new KeySchemaElement(hashKeyName, KeyType.HASH));

        ProvisionedThroughput provisionedthroughput = new ProvisionedThroughput(1000L, 1000L);

        CreateTableRequest request =
                new CreateTableRequest()
                        .withTableName(tableName)
                        .withAttributeDefinitions(attributeDefinitions)
                        .withKeySchema(ks)
                        .withProvisionedThroughput(provisionedthroughput);

        ddb.createTable(request);
    }
}

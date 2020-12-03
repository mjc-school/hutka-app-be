package by.mjc.handlers;

import by.mjc.entities.Route;
import by.mjc.entities.SearchRoutesRequest;
import by.mjc.services.RoutesService;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.*;
import init.AwsDynamoDbLocalTestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RoutesServiceTest {
    private static AmazonDynamoDBLocal amazonDynamoDBLocal;

    @Before
    public void init() {
        AwsDynamoDbLocalTestUtils.initSqLite();
        amazonDynamoDBLocal = DynamoDBEmbedded.create();
        AmazonDynamoDB amazonDynamoDB = amazonDynamoDBLocal.amazonDynamoDB();
        createTable(amazonDynamoDB, "Routes", "id");
    }

    @AfterClass
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
        assertThat(routes.size(), is(1));
    }

    private static CreateTableResult createTable(AmazonDynamoDB ddb, String tableName, String hashKeyName) {
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

        return ddb.createTable(request);
    }
}

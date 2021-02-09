package dao;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Point;
import by.mjc.entities.Route;
import utils.AwsDynamoDbLocalTestUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoutesDaoTest {
    private static AmazonDynamoDBLocal amazonDynamoDBLocal;
    private static AmazonDynamoDB dynamoDB;
    private static RoutesDao routesDao;
    private static List<Route> sampleRoutes;

    @BeforeAll
    public static void init() {
        AwsDynamoDbLocalTestUtils.initSqLite();
        amazonDynamoDBLocal = DynamoDBEmbedded.create();
        dynamoDB = DynamoDBEmbedded.create().amazonDynamoDB();
        routesDao = new RoutesDao(dynamoDB);
        createTable(dynamoDB);
        sampleRoutes = getSampleData();
        saveData(sampleRoutes);
    }

    @AfterAll
    public static void tearDownClass() {
        amazonDynamoDBLocal.shutdown();
    }

    @Test
    public void test() {
        List<Route> routesFromDb = routesDao.getAll();
        assertEquals(routesFromDb.size(), sampleRoutes.size());
    }

    private static void createTable(AmazonDynamoDB ddb) {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition("id", ScalarAttributeType.S));

        List<KeySchemaElement> ks = new ArrayList<>();
        ks.add(new KeySchemaElement("id", KeyType.HASH));

        ProvisionedThroughput provisionedthroughput = new ProvisionedThroughput(1000L, 1000L);

        CreateTableRequest request =
                new CreateTableRequest()
                        .withTableName("Routes")
                        .withAttributeDefinitions(attributeDefinitions)
                        .withKeySchema(ks)
                        .withProvisionedThroughput(provisionedthroughput);

        ddb.createTable(request);
    }

    private static List<Route> getSampleData() {
        Point point1 = Point.builder()
                .id("1")
                .build();
        Point point2 = Point.builder()
                .id("2")
                .build();
        Point point3 = Point.builder()
                .id("3")
                .build();
        Point point4 = Point.builder()
                .id("4")
                .build();
        Route route1 = Route.builder()
                .name("route1")
                .points(Arrays.asList(point1, point2))
                .build();
        Route route2 = Route.builder()
                .name("route2")
                .points(Arrays.asList(point3, point4))
                .build();

        return Arrays.asList(route1, route2);
    }

    private static void saveData(List<Route> routes) {
        routes.forEach(route -> routesDao.save(route));
    }
}

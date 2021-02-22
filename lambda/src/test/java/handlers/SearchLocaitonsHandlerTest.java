package handlers;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Location;
import by.mjc.entities.Route;
import by.mjc.services.RoutesService;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.AwsDynamoDbLocalTestUtils;
import utils.TestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SearchLocaitonsHandlerTest {
    private static AmazonDynamoDBLocal amazonDynamoDBLocal;
    private static RoutesService routesService;
    private static RoutesDao routesDao;

    @BeforeAll
    public static void init() throws IOException, URISyntaxException {
        AwsDynamoDbLocalTestUtils.initSqLite();
        amazonDynamoDBLocal = DynamoDBEmbedded.create();
        AmazonDynamoDB dynamoDB = amazonDynamoDBLocal.amazonDynamoDB();
        routesDao = new RoutesDao(dynamoDB);
        routesService = new RoutesService(routesDao);
        createTable(dynamoDB);
        Path placesCsvPath = Path.of(TestUtils.getUriForResourcePath(SearchLocaitonsHandlerTest.class, "sample-points.csv"));
        Path routesCsvPath = Path.of(TestUtils.getUriForResourcePath(SearchLocaitonsHandlerTest.class, "sample-routes.csv"));
        saveData(TestUtils.parseRoutesFromCsv(placesCsvPath, routesCsvPath));
    }

    @AfterAll
    public static void tearDownClass() {
        amazonDynamoDBLocal.shutdown();
    }

    @Test
    public void testSearchPlaces() {
        List<Location> places = routesService.getAllLocations();
        assertFalse(places.isEmpty());
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

    private static void saveData(List<Route> routes) {
        routes.forEach(route -> routesDao.save(route));
    }
}

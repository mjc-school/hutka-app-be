package dao;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Place;
import by.mjc.entities.Route;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import utils.AwsDynamoDbLocalTestUtils;
import utils.TestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoutesDaoTest {
    private static AmazonDynamoDBLocal amazonDynamoDBLocal;
    private static RoutesDao routesDao;

    @BeforeAll
    public static void init() throws IOException, URISyntaxException {
        AwsDynamoDbLocalTestUtils.initSqLite();
        amazonDynamoDBLocal = DynamoDBEmbedded.create();
        AmazonDynamoDB dynamoDB = amazonDynamoDBLocal.amazonDynamoDB();
        routesDao = new RoutesDao(dynamoDB);
        createTable(dynamoDB);
        Path placesCsvPath = Path.of(TestUtils.getUriForResourcePath(RoutesDaoTest.class, "sample-points.csv"));
        Path routesCsvPath = Path.of(TestUtils.getUriForResourcePath(RoutesDaoTest.class, "sample-routes.csv"));
        saveData(TestUtils.parseRoutesFromCsv(placesCsvPath, routesCsvPath));
    }

    @AfterAll
    public static void tearDownClass() {
        amazonDynamoDBLocal.shutdown();
    }

    @ParameterizedTest(name = "#{index} - match search?")
    @MethodSource("getTagsSearchScenariosFromCsv")
    public void testGetByTagsAndCity(List<String> csvValues) {
        List<String> routeIds = getExpectedRouteIdsForTagsSearchFromCsv(csvValues);
        List<Route> foundRoutes = routesDao.getByTagsAndCity(getTagsForTagsSearchFromCsv(csvValues), getCityForTagsSearchFromCsv(csvValues));
        boolean allMatch = foundRoutes.stream()
                .map(Route::getId)
                .allMatch(routeIds::contains);
        assertTrue(allMatch);
    }

    @Test
    public void testGetAll() {
        List<Route> routes = routesDao.getAll();
        assertFalse(routes.isEmpty());
    }

    @Test
    public void testGetAllPlaces() {
        List<Place> places = routesDao.getAllPlaces();
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

    private static List<String> getTagsForTagsSearchFromCsv(List<String> values) {
        return TestUtils.splitBySemicolon(values.get(0));
    }

    private static String getCityForTagsSearchFromCsv(List<String> values) {
        return values.get(1);
    }

    private static List<String> getExpectedRouteIdsForTagsSearchFromCsv(List<String> values) {
        return TestUtils.splitBySemicolon(values.get(2));
    }

    private static List<List<String>> getTagsSearchScenariosFromCsv() throws URISyntaxException, IOException {
        return Files.lines(Path.of(TestUtils.getUriForResourcePath(RoutesDaoTest.class, "tags-search-scenarios.csv")))
                .skip(1)
                .map(TestUtils::splitByComma)
                .collect(Collectors.toList());
    }
}

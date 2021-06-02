package handlers;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Route;
import by.mjc.entities.SearchRoutesRequest;
import by.mjc.services.RoutesService;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchRoutesHandlerTest {
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
        Path placesCsvPath = Path.of(TestUtils.getUriForResourcePath(SearchRoutesHandlerTest.class, "sample-points.csv"));
        Path routesCsvPath = Path.of(TestUtils.getUriForResourcePath(SearchRoutesHandlerTest.class, "sample-routes.csv"));
        saveData(TestUtils.parseRoutesFromCsv(placesCsvPath, routesCsvPath));
    }

    @AfterAll
    public static void tearDownClass() {
        amazonDynamoDBLocal.shutdown();
    }

    @ParameterizedTest(name = "#{index} - match search?")
    @MethodSource("getTagsSearchScenariosFromCsv")
    public void testSearchByTagsAndCity(List<String> csvValues) {
        List<String> routeIds = getExpectedRouteIdsForTagsSearchFromCsv(csvValues);
        SearchRoutesRequest request = new SearchRoutesRequest(getTagsForTagsSearchFromCsv(csvValues), getCityFromCsv(csvValues));
        List<Route> foundRoutes = routesService.getRoutesByTagsAndCity(request);
        boolean allMatch = foundRoutes.stream()
                .map(Route::getId)
                .allMatch(routeIds::contains) && routeIds.size() == foundRoutes.size();
        assertTrue(allMatch);
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

    private static String getCityFromCsv(List<String> values) {
        return values.get(1).strip();
    }

    private static List<String> getExpectedRouteIdsForTagsSearchFromCsv(List<String> values) {
        return TestUtils.splitBySemicolon(values.get(2));
    }

    private static List<List<String>> getTagsSearchScenariosFromCsv() throws URISyntaxException, IOException {
        return Files.lines(Path.of(TestUtils.getUriForResourcePath(SearchRoutesHandlerTest.class, "tags-search-scenarios.csv")))
                .skip(1)
                .map(TestUtils::splitByComma)
                .collect(Collectors.toList());
    }
}

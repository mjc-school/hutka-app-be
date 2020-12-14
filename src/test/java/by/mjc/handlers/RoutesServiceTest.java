package by.mjc.handlers;

import by.mjc.entities.Route;
import by.mjc.entities.SearchRoutesRequest;
import by.mjc.services.RoutesService;
import by.mjc.utils.GeoParser;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.*;
import init.AwsDynamoDbLocalTestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoutesServiceTest {
    private static AmazonDynamoDBLocal amazonDynamoDBLocal;

    @BeforeAll
    public static void init() throws FileNotFoundException, URISyntaxException {
        AwsDynamoDbLocalTestUtils.initSqLite();
        amazonDynamoDBLocal = DynamoDBEmbedded.create();
        AmazonDynamoDB amazonDynamoDB = amazonDynamoDBLocal.amazonDynamoDB();
        createTable(amazonDynamoDB, "Routes", "id");
        List<List<String>> records = loadData("./data.csv");
        saveData(records);
    }

    @AfterAll
    public static void tearDownClass() {
        amazonDynamoDBLocal.shutdown();
    }

    private String getFileAsString(String fileName) throws IOException, URISyntaxException {
        URI uri = getClass().getClassLoader().getResource(fileName).toURI();
         return Files.readString(Paths.get(uri), StandardCharsets.UTF_8);
    }

    // Parser adds an id, doesn't add style. Incorrectly parses Folder tag
    @Test
    public void testGeoJsonParser() throws IOException, URISyntaxException {
        RoutesService routesService = new RoutesService(amazonDynamoDBLocal.amazonDynamoDB());
        String kml = getFileAsString("route.kml");
        routesService.saveRoute(Route.builder()
                .id("4815")
                .kml(kml)
                .build());

        Route route = routesService.getRoute("4815");
        String expectedGeoJson = getFileAsString("route.geojson")
                .replaceAll("[\n\r]", "")
                .replaceAll("\\s+", "");

        assertEquals(expectedGeoJson, route.getGeoJson());
    }

    @ParameterizedTest(name = "#{index} - match search?")
    @MethodSource("loadScenarios")
    public void test(List<String> scenario) {

        RoutesService routesService = new RoutesService(amazonDynamoDBLocal.amazonDynamoDB());
        SearchRoutesRequest request = new SearchRoutesRequest();
        request.setTags(scenario.subList(0, scenario.size() - 1));
        List<Route> routes = routesService.getRoutesByTags(request);
        List<String> idsToExpect = Arrays.stream(scenario.get(scenario.size() - 1).split(" "))
                .filter(id -> !id.equals("null")).collect(Collectors.toList());
        assertEquals(new HashSet<>(idsToExpect), new HashSet<>(routes.stream().map(Route::getId).collect(Collectors.toList())));
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

    private static List<List<String>> loadScenarios() throws FileNotFoundException, URISyntaxException {
        return loadData("./scenarios.csv");
    }

    private static List<List<String>> loadData(String path) throws FileNotFoundException, URISyntaxException {
        List<List<String>> rows = new ArrayList<>();
        URL resource = RoutesServiceTest.class.getClassLoader().getResource(path);
        try (Scanner scanner = new Scanner(new File(resource.toURI()))) {
            while (scanner.hasNextLine()) {
                rows.add(getRecordFromLine(scanner.nextLine()));
            }
        }
        return rows;
    }

    private static List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }


    private static void saveData(List<List<String>> records) {
        RoutesService routesService = new RoutesService(amazonDynamoDBLocal.amazonDynamoDB());
        records.forEach(record -> {
            Route route = new Route();
            route.setId(record.get(0));
            route.setKml("kml");
            route.setTags(new HashSet<>(record.subList(1, record.size())));
            routesService.saveRoute(route);
        });
    }

}

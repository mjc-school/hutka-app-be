package services;

import by.mjc.entities.Point;
import by.mjc.entities.Route;
import by.mjc.services.XlsRoutesParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XlsRoutesParserTest {
    private static XlsRoutesParser routesParser;

    @BeforeAll
    public static void init() {
        routesParser = new XlsRoutesParser();
    }

    @Test
    public void test() throws IOException, URISyntaxException {
        List<Route> routes = getSampleData();
        List<Route> parsedRoutes = routesParser.parseXls(new FileInputStream(new File(getUriForResourcePath("test-data.xlsx"))));
        routes.sort(Comparator.comparing(Route::getId));
        parsedRoutes.sort(Comparator.comparing(Route::getId));
        assertEquals(routes, parsedRoutes);
    }

    private static URI getUriForResourcePath(String path) throws URISyntaxException {
        return Objects.requireNonNull(XlsRoutesParserTest.class.getClassLoader().getResource(path)).toURI();
    }

    private static List<String> splitByComma(String values) {
        return Arrays.stream(values.split(",", -1))
                .map(String::strip)
                .collect(Collectors.toList());
    }

    private static List<String> splitBySemicolon(String values) {
        return Arrays.stream(values.split(";", -1))
                .map(String::strip)
                .collect(Collectors.toList());
    }

    private static Point getPointFromCsv(List<String> values) {
        Double latitude = null, longitude = null;

        try {
            latitude = Double.valueOf(values.get(5));
            longitude = Double.valueOf(values.get(6));
        } catch (NumberFormatException e) {
            //
        }

        return Point.builder()
                .id(values.get(0))
                .region(values.get(1))
                .sight(values.get(2))
                .description(values.get(3))
                .imageUrl(values.get(4))
                .latitude(latitude)
                .longitude(longitude)
                .tags(splitBySemicolon(values.get(7)))
                .build();
    }

    private static Route getRouteFromCsv(List<String> values) {
        return Route.builder()
                .id(values.get(0))
                .name(values.get(1))
                .description(values.get(2))
                .length(values.get(3))
                .time(values.get(4))
                .imageUrl(values.get(5))
                .points(new ArrayList<>())
                .build();
    }

    private static List<String> getPointIdsFromCsv(List<String> values) {
        return splitBySemicolon(values.get(6));
    }

    private static List<Route> getSampleData() throws IOException, URISyntaxException {
        Map<String, Point> pointsMap = new HashMap<>();
        List<Route> routes = new ArrayList<>();
        Files.lines(Path.of(getUriForResourcePath("sample-points.csv")))
                .skip(1)
                .map(XlsRoutesParserTest::splitByComma)
                .forEach(values -> {
                    Point point = getPointFromCsv(values);
                    pointsMap.put(point.getId(), point);
                });

        Files.lines(Path.of(getUriForResourcePath("sample-routes.csv")))
                .skip(1)
                .map(XlsRoutesParserTest::splitByComma)
                .forEach(values -> {
                    Route route = getRouteFromCsv(values);
                    routes.add(route);
                    getPointIdsFromCsv(values)
                            .forEach(pointId -> {
                                Point point = pointsMap.get(pointId);
                                if (point != null) {
                                    route.getPoints().add(point);
                                }
                            });
                });

        return routes;
    }
}

package services;

import by.mjc.entities.Route;
import by.mjc.services.XlsRoutesParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XlsRoutesParserTest {
    private static XlsRoutesParser routesParser;

    @BeforeAll
    public static void init() {
        routesParser = new XlsRoutesParser();
    }

    @Test
    public void test() throws IOException, URISyntaxException {
        Path placesCsvPath = Path.of(TestUtils.getUriForResourcePath(XlsRoutesParserTest.class, "sample-points.csv"));
        Path routesCsvPath = Path.of(TestUtils.getUriForResourcePath(XlsRoutesParserTest.class, "sample-routes.csv"));
        List<Route> routes = TestUtils.parseRoutesFromCsv(placesCsvPath, routesCsvPath);
        List<Route> parsedRoutes = routesParser.parseXls(new FileInputStream(new File(TestUtils.getUriForResourcePath(XlsRoutesParserTest.class, "test-data.xlsx"))));
        routes.sort(Comparator.comparing(Route::getId));
        parsedRoutes.sort(Comparator.comparing(Route::getId));
        assertEquals(routes, parsedRoutes);
    }
}

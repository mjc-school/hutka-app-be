package utils;

import by.mjc.entities.Location;
import by.mjc.entities.Place;
import by.mjc.entities.Position;
import by.mjc.entities.Route;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TestUtils {
    public static List<Route> parseRoutesFromCsv(Path placesCsvPath ,Path routesCsvPath) throws IOException {
        Map<String, Place> pointsMap = new HashMap<>();
        List<Route> routes = new ArrayList<>();
        Files.lines(placesCsvPath)
                .skip(1)
                .map(TestUtils::splitByComma)
                .forEach(values -> {
                    Place place = getPlaceFromCsv(values);
                    pointsMap.put(place.getId(), place);
                });

        Files.lines(routesCsvPath)
                .skip(1)
                .map(TestUtils::splitByComma)
                .forEach(values -> {
                    Route route = getRouteFromCsv(values);
                    routes.add(route);
                    getPlaceIdsFromCsv(values).forEach(placeId -> {
                        Place place = pointsMap.get(placeId);
                        addPlaceToRoute(route, place);
                    });
                });

        return routes;
    }

    public static void addPlaceToRoute(Route route, Place place) {
        if (place != null) {
            route.getPoints().add(place);
            addPlaceTagsToRoute(route, place);
        }
    }

    public static void addPlaceTagsToRoute(Route route, Place place) {
        if (place.getTags() != null) {
            place.getTags().forEach(tag -> {
                if (!route.getTags().contains(tag)) {
                    route.getTags().add(tag);
                }
            });
        }
    }

    public static Place getPlaceFromCsv(List<String> values) {
        Double lat = getDouble(values.get(2));
        Double lng = getDouble(values.get(3));
        String locationName = values.get(5);
        Double locationLat = getDouble(values.get(6));
        Double locationLng = getDouble(values.get(7));

        return Place.builder()
                .id(values.get(0))
                .name(values.get(1))
                .coords(new Position(lat, lng))
                .imgUrl(values.get(4))
                .location(new Location(locationName, new Position(locationLat, locationLng)))
                .tags(splitBySemicolon(values.get(8)))
                .description(values.get(9))
                .build();
    }

    public static Route getRouteFromCsv(List<String> values) {
        Double lat = getDouble(values.get(3));
        Double lng = getDouble(values.get(4));

        return Route.builder()
                .id(values.get(0))
                .name(values.get(1))
                .points(new ArrayList<>())
                .coords(new Position(lat, lng))
                .imgUrl(values.get(5))
                .tags(new ArrayList<>())
                .description(values.get(6))
                .build();
    }

    public static List<String> getPlaceIdsFromCsv(List<String> values) {
        return splitBySemicolon(values.get(2));
    }

    public static List<String> splitByComma(String values) {
        if (values.isEmpty()) return Collections.emptyList();

        return Arrays.stream(values.split(",", -1))
                .map(String::strip)
                .collect(Collectors.toList());
    }

    public static List<String> splitBySemicolon(String values) {
        if (values.isEmpty()) return Collections.emptyList();

        return Arrays.stream(values.split(";", -1))
                .map(String::strip)
                .collect(Collectors.toList());
    }

    public static Double getDouble(String str) {
        Double number = null;
        try {
            number = Double.valueOf(str);
        } catch (NumberFormatException e) {
            //
        }
        return number;
    }

    public static <T> URI getUriForResourcePath(Class<T> cls, String path) throws URISyntaxException {
        return Objects.requireNonNull(cls.getClassLoader().getResource(path)).toURI();
    }
}

package by.mjc.dao;

import by.mjc.entities.Place;
import by.mjc.entities.Route;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.*;
import java.util.stream.Collectors;

public class RoutesDao {
    private final AmazonDynamoDB dynamoDBClient;

    public RoutesDao(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    public void save(Route route) {
        Route routeToSave = cloneRoute(route);
        // Cannot add empty collection or collection that contains empty string to DynamoDB
        fixCollections(routeToSave);
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        mapper.save(routeToSave);
    }

    private Route cloneRoute(Route route) {
        return Route.builder()
                .id(route.getId())
                .name(route.getName())
                .points(route.getPoints())
                .coords(route.getCoords())
                .imgUrl(route.getImgUrl())
                .tags(route.getTags())
                .description(route.getDescription())
                .cities(route.getCities())
                .build();
    }

    private void fixCollections(Route route) {
        route.setTags(getFixedCollection(route.getTags()));
        route.setCities(getFixedCollection(route.getCities()));

        if (route.getPoints().isEmpty()) {
            route.setPoints(null);
        } else {
            route.getPoints().forEach(point -> {
                point.setTags(getFixedCollection(point.getTags()));
            });
        }
    }

    private List<String> getFixedCollection(List<String> list) {
        if (isEmptyCollection(list)) {
            return null;
        }

        return getListWithoutEmptyStrings(list);
    }

    private List<String> getListWithoutEmptyStrings(List<String> list) {
        return list.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private <T> boolean isEmptyCollection(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public List<Route> getAll() {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        return mapper.scan(Route.class, scanExpression);
    }

    public List<Route> getByTagsAndCity(List<String> tags, String city) {
        if (isEmptyCollection(tags) || city == null || city.isEmpty()) {
            return getAll();
        }

        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        // In DynamoDB "contains" function accepts single values or string sets
        List<String> uniqueTags = tags.stream()
                .distinct()
                .collect(Collectors.toList());
        Map<String, AttributeValue> expressionAttributes = Map.of(
                ":tags", new AttributeValue().withSS(uniqueTags),
                ":city", new AttributeValue().withS(city));
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("contains(tags, :tags) AND contains(cities, :city)")
                .withExpressionAttributeValues(expressionAttributes);

        return mapper.scan(Route.class, scanExpression);
    }

    public List<Place> getAllPlaces() {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.withProjectionExpression("points");
        List<Route> routes = mapper.scan(Route.class, scanExpression);
        Set<Place> places = new HashSet<>();
        routes.forEach(route -> {
            if (route.getPoints() != null) {
                places.addAll(route.getPoints());
            }
        });
        return new ArrayList<>(places);
    }

    public void delete(Route route) {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        mapper.delete(route);
    }
}

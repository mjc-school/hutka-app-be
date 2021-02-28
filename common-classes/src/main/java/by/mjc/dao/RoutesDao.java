package by.mjc.dao;

import by.mjc.entities.Location;
import by.mjc.entities.Place;
import by.mjc.entities.Route;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
        if (isEmptyCollection(tags) && (city == null || city.isEmpty())) {
            return getAll();
        }

        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);

        Map<String, AttributeValue> tagsExpressionAttributes = getExpressionAttributes(":tag", tags);
        Map<String, AttributeValue> cityExpressionAttributes = getExpressionAttributes(":city", List.of(city));
        Map<String, AttributeValue> allExpressionAttributes = getAllExpressionAttributes(tagsExpressionAttributes, cityExpressionAttributes);

        String tagsContainsExpression = getContainsCollectionExpression("tags", tagsExpressionAttributes);
        String cityContainsExpression = getContainsCollectionExpression("cities", cityExpressionAttributes);
        String allExpression = buildExpressionFromSubexpressions("AND", tagsContainsExpression, cityContainsExpression);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression(allExpression)
                .withExpressionAttributeValues(allExpressionAttributes);

        return mapper.scan(Route.class, scanExpression);

    }

    private Map<String, AttributeValue> getExpressionAttributes(String field, List<String> list) {
        AtomicInteger i = new AtomicInteger(1);
        return list.stream()
                .collect(Collectors.toMap(
                        tag -> String.format(field + "%d", i.getAndIncrement()),
                        tag -> new AttributeValue().withS(tag)));
    }

    private String getContainsExpression(String field, String attributeAlias) {
        return String.format("contains(%s, %s)", field, attributeAlias);
    }

    private String getContainsCollectionExpression(String field, Map<String, AttributeValue> expressionAttributes) {
        return expressionAttributes.keySet().stream()
                .map(item -> this.getContainsExpression(field, item))
                .reduce((s1, s2) -> s1 + " OR " + s2)
                .orElse("");
    }

    @SafeVarargs
    private Map<String, AttributeValue> getAllExpressionAttributes(Map<String, AttributeValue>... expressionAttributes) {
        Map<String, AttributeValue> allExpressionAttributes = new HashMap<>();

        for (var expressionAttribute : expressionAttributes) {
            allExpressionAttributes.putAll(expressionAttribute);
        }

        return allExpressionAttributes;
    }

    private String buildExpressionFromSubexpressions(String operator, String... expressions) {
        return Arrays.stream(expressions)
                .map(expression -> expression.isEmpty() ? "" : String.format("(%s)", expression))
                .reduce((expression1, expression2) -> {
                    if (expression1.isEmpty() || expression2.isEmpty()) {
                        if (expression1.isEmpty() && expression2.isEmpty()) return "";
                        return expression1.isEmpty() ? expression2 : expression1;
                    } else {
                        return String.format("%s %s %s", expression1, operator, expression2);
                    }
                })
                .orElse("");
    }

    public List<Location> getAllLocations() {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        scanExpression.withProjectionExpression("points");
        List<Route> routes = mapper.scan(Route.class, scanExpression);
        Set<Location> locations = new HashSet<>();
        routes.forEach(route -> {
            if (route.getPoints() != null) {
                locations.addAll(route.getPoints().stream().map(Place::getLocation).collect(Collectors.toList()));
            }
        });
        return new ArrayList<>(locations);
    }

    public void delete(Route route) {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        mapper.delete(route);
    }
}

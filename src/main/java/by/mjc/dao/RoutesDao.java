package by.mjc.dao;

import by.mjc.entities.Route;
import by.mjc.utils.GeoParser;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RoutesDao {
    private final AmazonDynamoDB dynamoDBClient;

    public RoutesDao(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    public void save(Route route) {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        try {
            route.setGeoJson(GeoParser.kmlToGeoJson(route.getKml()));
        } catch (XMLStreamException | SAXException | IOException e) {
            //
        }
        mapper.save(route);
    }

    public Route get(String id) {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        return mapper.load(Route.class, id);
    }

    public List<Route> getAll() {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        return mapper.scan(Route.class, scanExpression);
    }

    private Map<String, AttributeValue> getExpressionAttributes(List<String> tags) {
        AtomicInteger i = new AtomicInteger(1);
        return tags.stream()
                .collect(Collectors.toMap(
                        tag -> String.format(":tag%d", i.getAndIncrement()),
                        tag -> new AttributeValue().withS(tag)));
    }

    private String getTagFilterExpression(String attributeAlias) {
        return String.format("contains(tags, %s)", attributeAlias);
    }

    private String getTagsFilterExpression(Map<String, AttributeValue> expressionAttributes) {
        return expressionAttributes.keySet().stream()
                .map(this::getTagFilterExpression)
                .reduce((s1, s2) -> s1 + " OR " + s2)
                .orElse("");
    }

    public List<Route> getByTags(List<String> tags) {
        if (tags.isEmpty()) {
            return getAll();
        }

        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        Map<String, AttributeValue> expressionAttributes = getExpressionAttributes(tags);
        String filterExpression = getTagsFilterExpression(expressionAttributes);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression(filterExpression)
                .withExpressionAttributeValues(expressionAttributes);

        return mapper.scan(Route.class, scanExpression);
    }

    public void delete(Route route) {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        mapper.delete(route);
    }
}

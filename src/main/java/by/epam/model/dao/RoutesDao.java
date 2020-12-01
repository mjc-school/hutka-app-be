package by.epam.model.dao;

import by.epam.model.entities.Route;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutesDao
{
    private AmazonDynamoDB dynamoDBClient;

    public RoutesDao(AmazonDynamoDB dynamoDBClient)
    {
        this.dynamoDBClient = dynamoDBClient;
    }

    public void save(Route route)
    {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        mapper.save(route);
    }

    public List<Route> getAll()
    {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        return mapper.scan(Route.class, scanExpression);
    }

    private Map<String, AttributeValue> getExpressionAttributes(List<String> tags)
    {
        Map<String, AttributeValue> expressionAttributes = new HashMap<>();

        for(int i = 0; i < tags.size(); i++)
        {
            String attributeAlias = String.format(":tag%d", i + 1);
            expressionAttributes.put(attributeAlias, new AttributeValue().withS(tags.get(i)));
        }

        return expressionAttributes;
    }

    private String getTagFilterExpression(String attributeAlias)
    {
        return String.format("contains(tags, %s)", attributeAlias);
    }

    private String getTagsFilterExpression(Map<String, AttributeValue> expressionAttributes)
    {
        return expressionAttributes.keySet().stream()
                .map(this::getTagFilterExpression)
                .reduce((s1, s2) -> s1 + " AND " + s2)
                .orElse("");
    }

    public List<Route> getByTags(List<String> tags)
    {
        if(tags.size() == 0)
        {
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

    public void delete(Route route)
    {
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient);
        mapper.delete(route);
    }
}

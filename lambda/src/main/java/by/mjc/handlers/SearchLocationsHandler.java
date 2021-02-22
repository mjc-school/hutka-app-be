package by.mjc.handlers;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Location;
import by.mjc.services.RoutesService;
import by.mjc.utils.AwsClientFactory;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.List;
import java.util.Map;

public class SearchLocationsHandler implements RequestHandler<Map<String, String>, List<Location>> {
    private final AmazonDynamoDB dynamoDB = AwsClientFactory.getInstance().getDynamoDBClient();
    private final RoutesService routesService = new RoutesService(new RoutesDao(dynamoDB));

    @Override
    public List<Location> handleRequest(Map<String, String> input, Context context) {
        return routesService.getAllLocations();
    }
}
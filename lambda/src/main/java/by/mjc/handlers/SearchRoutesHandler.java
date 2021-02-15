package by.mjc.handlers;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Route;
import by.mjc.entities.SearchRoutesRequest;
import by.mjc.services.RoutesService;
import by.mjc.utils.AwsClientFactory;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.List;

public class SearchRoutesHandler implements RequestHandler<SearchRoutesRequest, List<Route>> {
    private final AmazonDynamoDB dynamoDB = AwsClientFactory.getInstance().getDynamoDBClient();
    private final RoutesService routesService = new RoutesService(new RoutesDao(dynamoDB));

    @Override
    public List<Route> handleRequest(SearchRoutesRequest request, Context context) {
        return routesService.getRoutesByTagsAndCity(request);
    }
}
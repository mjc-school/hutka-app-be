package by.mjc.handlers;

import by.mjc.entities.Route;
import by.mjc.entities.SearchRoutesRequest;
import by.mjc.services.RoutesService;
import by.mjc.utils.AwsClientFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.List;

public class SearchRoutesHandler implements RequestHandler<SearchRoutesRequest, List<Route>> {

    private final RoutesService routesService = new RoutesService(AwsClientFactory.getInstance().getDynamoDBClient());

    @Override
    public List<Route> handleRequest(SearchRoutesRequest request, Context context) {
        return routesService.getRoutesByTags(request);
    }
}
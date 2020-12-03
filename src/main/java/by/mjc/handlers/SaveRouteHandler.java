package by.mjc.handlers;

import by.mjc.entities.Route;
import by.mjc.services.RoutesService;
import by.mjc.utils.AwsClientFactory;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class SaveRouteHandler implements RequestHandler<Route, String> {

    private final RoutesService routesService = new RoutesService(AwsClientFactory.getInstance().getDynamoDBClient());

    @Override
    public String handleRequest(Route route, Context context) {
        routesService.saveRoute(route);
        return "Route saved";
    }
}
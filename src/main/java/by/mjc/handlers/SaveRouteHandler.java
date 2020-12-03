package by.mjc.handlers;

import by.mjc.model.entities.Route;
import by.mjc.model.services.RoutesService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class SaveRouteHandler implements RequestHandler<Route, String> {

    private final RoutesService routesService = new RoutesService();

    @Override
    public String handleRequest(Route route, Context context) {
        routesService.saveRoute(route);
        return "Route saved";
    }
}
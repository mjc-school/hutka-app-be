package by.mjc.handlers;

import by.mjc.model.entities.Route;
import by.mjc.model.services.RoutesService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SaveRouteHandler implements RequestHandler<SaveRouteHandler.SaveRouteRequest, String> {
    @Data
    @NoArgsConstructor
    public static class SaveRouteRequest {
        private Route route;
    }

    private RoutesService routesService = new RoutesService();

    @Override
    public String handleRequest(SaveRouteRequest request, Context context) {
        routesService.saveRoute(request.getRoute());
        return "Route saved";
    }
}
package by.mjc.handlers;

import java.util.List;

import by.mjc.model.entities.Route;
import by.mjc.model.services.RoutesService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.Data;
import lombok.NoArgsConstructor;

public class GetRoutesHandler implements RequestHandler<GetRoutesHandler.GetRoutesRequest, List<Route>> {

    @Data
    @NoArgsConstructor
    public static class GetRoutesRequest {
        private List<String> tags;
    }

    private RoutesService routesService = new RoutesService();

    @Override
    public List<Route> handleRequest(GetRoutesRequest request, Context context) {
        return routesService.getRoutesByTags(request.getTags());
    }
}
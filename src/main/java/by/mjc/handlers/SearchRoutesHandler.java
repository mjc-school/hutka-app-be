package by.mjc.handlers;

import by.mjc.model.entities.Route;
import by.mjc.model.services.RoutesService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class SearchRoutesHandler implements RequestHandler<SearchRoutesHandler.SearchRoutesRequest, List<Route>> {

    @Data
    @NoArgsConstructor
    public static class SearchRoutesRequest {
        private List<String> tags;
    }

    private final RoutesService routesService = new RoutesService();

    @Override
    public List<Route> handleRequest(SearchRoutesRequest request, Context context) {
        return routesService.getRoutesByTags(request.getTags());
    }
}
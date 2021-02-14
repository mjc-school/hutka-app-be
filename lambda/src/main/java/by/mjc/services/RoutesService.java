package by.mjc.services;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Place;
import by.mjc.entities.Route;
import by.mjc.entities.SearchRoutesRequest;

import java.util.List;
import java.util.stream.Collectors;

public class RoutesService {
    private final RoutesDao routesDao;

    public RoutesService(RoutesDao routesDao) {
        this.routesDao = routesDao;
    }

    public List<Place> getAllPlaces() {
        return routesDao.getAllPlaces();
    }

    public List<Route> getRoutesByTags(SearchRoutesRequest request) {
        List<String> tags = request.getTags();
        List<Route> routes = routesDao.getByTags(tags);

        if(!tags.isEmpty()) {
            return routes.stream()
                    .filter(route -> !route.getPoints().isEmpty())
                    .filter(route -> hasMoreThanTwoTags(route, tags))
                    .collect(Collectors.toList());
        }

        return routes;
    }

    private boolean hasMoreThanTwoTags(Route route, List<String> tags) {
        long numberOfTags = route.getTags().stream()
                .filter(tags::contains)
                .limit(2)
                .count();

        return numberOfTags >= 2;
    }
}

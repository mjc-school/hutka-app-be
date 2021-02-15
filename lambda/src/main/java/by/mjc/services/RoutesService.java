package by.mjc.services;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Place;
import by.mjc.entities.Route;
import by.mjc.entities.SearchRoutesRequest;

import java.util.Collection;
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

    public List<Route> getRoutesByTagsAndCity(SearchRoutesRequest request) {
        List<String> tags = request.getTags();
        String city = request.getCity();
        List<Route> routes = routesDao.getByTags(tags);

        if (isEmptyCollection(tags) && !city.isEmpty()) {
            return routes;
        }

        return routes.stream()
                .filter(route -> !isEmptyCollection(route.getPoints()))
                .filter(route -> hasMoreThanTwoTags(route, tags))
                .filter(route -> containsRequestedCity(route, city))
                .collect(Collectors.toList());
    }

    private <T> boolean isEmptyCollection(Collection<T> collection) {
        return collection != null && collection.isEmpty();
    }

    private boolean containsRequestedCity(Route route, String city) {
        return route.getPoints().stream()
                .anyMatch(place -> place.getLocation().getName().equals(city));
    }

    private boolean hasMoreThanTwoTags(Route route, List<String> tags) {
        long numberOfTags = route.getTags().stream()
                .filter(tags::contains)
                .limit(2)
                .count();

        return numberOfTags == 2;
    }
}

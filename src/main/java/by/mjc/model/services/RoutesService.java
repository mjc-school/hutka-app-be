package by.mjc.model.services;

import by.mjc.model.dao.RoutesDao;
import by.mjc.model.entities.Route;
import by.mjc.utils.AwsClientFactory;

import java.util.List;

public class RoutesService {
    private final RoutesDao routesDao = new RoutesDao(AwsClientFactory.getInstance().getDynamoDBClient());

    public void saveRoute(Route route) {
        routesDao.save(route);
    }

    public List<Route> getRoutesByTags(List<String> tags) {
        return routesDao.getByTags(tags);
    }
}

package by.mjc.services;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Route;
import by.mjc.entities.SearchRoutesRequest;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import java.util.List;

public class RoutesService {
    private final RoutesDao routesDao;

    public RoutesService(AmazonDynamoDB dynamoDBClient) {
        this.routesDao = new RoutesDao(dynamoDBClient);
    }

    public void saveRoute(Route route) {
        routesDao.save(route);
    }

    public List<Route> getRoutesByTags(SearchRoutesRequest request) {
        return routesDao.getByTags(request.getTags());
    }
}

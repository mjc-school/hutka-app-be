package by.epam.model.services;

import by.epam.model.dao.RoutesDao;
import by.epam.model.entities.Route;
import by.epam.utils.AwsClientFactory;

import java.util.List;

public class RoutesService
{
    private final RoutesDao routesDao = new RoutesDao(AwsClientFactory.getInstance().getDynamoDBClient());

    public void saveRoute(Route route)
    {
        routesDao.save(route);
    }

    public List<Route> getRoutesByTags(List<String> tags)
    {
        return routesDao.getByTags(tags);
    }
}

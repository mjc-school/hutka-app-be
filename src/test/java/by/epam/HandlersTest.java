package by.epam;

import by.epam.handlers.GetRoutesHandler;
import by.epam.handlers.SaveRouteHandler;
import by.epam.model.entities.Route;
import by.epam.model.services.RoutesService;
import by.epam.utils.JsonParser;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HandlersTest
{
    @Mock
    private RoutesService routesService;

    @InjectMocks
    private GetRoutesHandler getRoutesHandler;

    @InjectMocks
    private SaveRouteHandler saveRouteHandler;

    @Test
    public void getRoutesHandlerShouldReturnSuccessfulResponse()
    {
        Route route = new Route("kml", Set.of("tag1", "tag2", "tag3", "tag4"), "description", "name", "startLocation");
        Route route2 = new Route("kml2", Set.of("tag1", "tag2", "tag3", "tag5"), "description2", "name2", "startLocation2");
        List<Route> expectedRoutes = Arrays.asList(route, route2);

        AwsProxyRequest request = new AwsProxyRequest();
        request.setBody("[]");

        when(routesService.getRoutesByTags(any())).thenReturn(expectedRoutes);

        int statusCode = getRoutesHandler.handleRequest(request, null).getStatusCode();
        assertEquals(200, statusCode);
    }

    @Test
    public void saveRouteHandlerShouldReturnSuccessfulResponse()
    {
        Route route = new Route("kml", Set.of("tag1", "tag2", "tag3", "tag4"), "description", "name", "startLocation");
        AwsProxyRequest request = new AwsProxyRequest();
        request.setBody(JsonParser.jsonFromObject(route));

        int statusCode = saveRouteHandler.handleRequest(request, null).getStatusCode();
        assertEquals(200, statusCode);
    }
}

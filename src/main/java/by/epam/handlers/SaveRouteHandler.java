package by.epam.handlers;

import by.epam.model.entities.Route;
import by.epam.model.services.RoutesService;
import by.epam.utils.JsonParser;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class SaveRouteHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse>
{
    private RoutesService routesService = new RoutesService();

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest request, Context context)
    {
        AwsProxyResponse response = new AwsProxyResponse();

        try
        {
            Route route = JsonParser.objectFromJson(request.getBody(), Route.class);

            routesService.saveRoute(route);

            response.setBody("Route saved");
            response.setStatusCode(200);
        }
        catch (RuntimeException e)
        {
            response.setBody("Bad request");
            response.setStatusCode(400);
        }

        return response;
    }
}
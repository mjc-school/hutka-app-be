package by.mjc.app;

import by.mjc.dao.RoutesDao;
import by.mjc.entities.Route;
import by.mjc.services.XlsRoutesParser;
import by.mjc.utils.AwsClientFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class Application {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please specify the path to the file as an argument");
            return;
        }

        FileInputStream fis;

        try {
            fis = new FileInputStream(args[0]);
        } catch (FileNotFoundException e) {
            System.out.println("Please check the path is correct");
            return;
        }

        RoutesDao routesDao = new RoutesDao(AwsClientFactory.getInstance().getDynamoDBClient());
        XlsRoutesParser routesParser = new XlsRoutesParser();
        List<Route> routes;

        try {
            routes = routesParser.parseXls(fis);
        } catch (IOException e) {
            System.out.println("Parsing error");
            e.printStackTrace();
            return;
        }

        routes.forEach(routesDao::save);
        System.out.println("Routes saved");
    }
}

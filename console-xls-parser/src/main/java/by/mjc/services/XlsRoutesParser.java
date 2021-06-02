package by.mjc.services;

import by.mjc.entities.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class XlsRoutesParser {
    public List<Route> parseXls(FileInputStream fis) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet placesSheet = workbook.getSheet("Points");
        XSSFSheet routesSheet = workbook.getSheet("routes");
        DataFormatter formatter = new DataFormatter();
        Map<String, Place> pointsMap = new HashMap<>();
        List<Route> routes = new ArrayList<>();

        for (Row placesRow : placesSheet) {
            if (isEmptyRow(placesRow, formatter) || placesRow.getRowNum() == 0) {
                continue;
            }

            Place place = getPlaceFromRow(placesRow, formatter);
            if (place.getId() != null && !place.getId().equals("") && place.getName() != null && !place.getName().equals("")) {
                pointsMap.put(place.getId(), place);
            }
        }

        for (Row routesRow : routesSheet) {
            if (isEmptyRow(routesRow, formatter) || routesRow.getRowNum() == 0) {
                continue;
            }
            routesSheet.getMergedRegions();
            Route route = getRouteFromRow(routesRow, formatter);
            if (route.getId() != null && !route.getId().equals("")) {
                routes.add(route);
            }
            List<String> placeIds = getPlaceIdsFromRoutesRow(routesRow, formatter);

            placeIds.forEach(placeId -> {
                Place place = pointsMap.get(placeId);
                if (route.getId() != null && !route.getId().equals("")) {
                    addPlaceToRoute(route, place);
                } else if (!routes.isEmpty()) {
                    addPlaceToRoute(routes.get(routes.size() - 1), place);
                }
            });
        }
        routes.forEach(Route::fillDenormalizedFields);
        return routes;
    }

    private void addPlaceToRoute(Route route, Place place) {
        if (place != null) {
            route.getPoints().add(place);
        }
    }

    private boolean isEmptyRow(Row row, DataFormatter formatter) {
        boolean isEmpty = true;

        if (row != null) {
            for (Cell cell : row) {
                if (formatter.formatCellValue(cell).trim().length() > 0) {
                    isEmpty = false;
                    break;
                }
            }
        }

        return isEmpty;
    }

    private Place getPlaceFromRow(Row placesRow, DataFormatter formatter) {
//        Double lat = getDouble(formatter.formatCellValue(placesRow.getCell(PlaceColumns.LAT.getIndex())));
//        Double lng = getDouble(formatter.formatCellValue(placesRow.getCell(PlaceColumns.LNG.getIndex())));
        String locationName = formatter.formatCellValue(placesRow.getCell(PlaceColumns.LOCATION_NAME.getIndex()));
        Double locationLat = getDouble(formatter.formatCellValue(placesRow.getCell(PlaceColumns.LOCATION_LAT.getIndex())));
        Double locationLng = getDouble(formatter.formatCellValue(placesRow.getCell(PlaceColumns.LOCATION_LNG.getIndex())));

        return Place.builder()
                .id(formatter.formatCellValue(placesRow.getCell(PlaceColumns.ID.getIndex())))
                .name(formatter.formatCellValue(placesRow.getCell(PlaceColumns.NAME.getIndex())))
//                .coords(new Position(lat, lng))
                .coords(new Position(locationLat, locationLng))
                .imgUrl(formatter.formatCellValue(placesRow.getCell(PlaceColumns.IMG_URL.getIndex())))
//                .location(new Location(locationName, new Position(locationLat, locationLng)))
                .location(Location.builder().name(locationName).build())
                .tags(splitByComma(formatter.formatCellValue(placesRow.getCell(PlaceColumns.TAGS.getIndex()))))
                .description(formatter.formatCellValue(placesRow.getCell(PlaceColumns.DESCRIPTION.getIndex())))
                .build();
    }

    private Route getRouteFromRow(Row routesRow, DataFormatter formatter) {
//        Double lat = getDouble(formatter.formatCellValue(routesRow.getCell(RouteColumns.LAT.getIndex())));
//        Double lng = getDouble(formatter.formatCellValue(routesRow.getCell(RouteColumns.LNG.getIndex())));
        return Route.builder()
                .id(formatter.formatCellValue(routesRow.getCell(RouteColumns.ID.getIndex())))
                .name(formatter.formatCellValue(routesRow.getCell(RouteColumns.NAME.getIndex())))
                .points(new ArrayList<>())
                .time(formatter.formatCellValue(routesRow.getCell(RouteColumns.TIME.getIndex())))
                .length(formatter.formatCellValue(routesRow.getCell(RouteColumns.LENGTH.getIndex())))
//                .coords(new Position(lat, lng))
                .imgUrl(formatter.formatCellValue(routesRow.getCell(RouteColumns.IMG_URL.getIndex())))
                .description(formatter.formatCellValue(routesRow.getCell(RouteColumns.DESCRIPTION.getIndex())))
                .build();
    }

    private List<String> getPlaceIdsFromRoutesRow(Row routesRow, DataFormatter formatter) {
        String placeIds = formatter.formatCellValue(routesRow.getCell(RouteColumns.POINTS.getIndex()));
        return splitByComma(placeIds);
    }

    private static Double getDouble(String str) {
        Double number = null;
        try {
            number = Double.valueOf(str);
        } catch (NumberFormatException e) {
            //
        }
        return number;
    }

    private List<String> splitByComma(String values) {
        return Arrays.stream(values.split(","))
                .map(String::strip)
                .collect(Collectors.toList());
    }
}

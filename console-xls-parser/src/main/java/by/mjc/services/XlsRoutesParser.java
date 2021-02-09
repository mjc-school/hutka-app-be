package by.mjc.services;

import by.mjc.entities.Point;
import by.mjc.entities.Route;
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
    private enum RouteColumns {
        ID(0), NAME(1), DESCRIPTION(2),
        LENGTH(3), TIME(4), IMAGE_URL(5), POINT_ID(6);

        private final int index;

        RouteColumns(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private enum PointColumns {
        ID(0), REGION(1), SIGHT(2),
        DESCRIPTION(3), IMAGE_URL(4), LATITUDE(5),
        LONGITUDE(6), TAGS(7);

        private final int index;

        PointColumns(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    private List<String> splitByComma(String values) {
        return Arrays.stream(values.split(","))
                .map(String::strip)
                .collect(Collectors.toList());
    }

    private Point getPointFromRow(Row pointsRow, DataFormatter formatter) {
        Double latitude = pointsRow.getCell(PointColumns.LATITUDE.getIndex()).getNumericCellValue();
        Double longitude = pointsRow.getCell(PointColumns.LONGITUDE.getIndex()).getNumericCellValue();

        if (latitude == 0.0) latitude = null;
        if (longitude == 0.0) longitude = null;

        return Point.builder()
                .id(formatter.formatCellValue(pointsRow.getCell(PointColumns.ID.getIndex())))
                .region(formatter.formatCellValue(pointsRow.getCell(PointColumns.REGION.getIndex())))
                .sight(formatter.formatCellValue(pointsRow.getCell(PointColumns.SIGHT.getIndex())))
                .description(formatter.formatCellValue(pointsRow.getCell(PointColumns.DESCRIPTION.getIndex())))
                .imageUrl(formatter.formatCellValue(pointsRow.getCell(PointColumns.IMAGE_URL.getIndex())))
                .latitude(latitude)
                .longitude(longitude)
                .tags(splitByComma(formatter.formatCellValue(pointsRow.getCell(PointColumns.TAGS.getIndex()))))
                .build();
    }

    private Route getRouteFromRow(Row routesRow, DataFormatter formatter) {
        return Route.builder()
                .id(formatter.formatCellValue(routesRow.getCell(RouteColumns.ID.getIndex())))
                .name(formatter.formatCellValue(routesRow.getCell(RouteColumns.NAME.getIndex())))
                .description(formatter.formatCellValue(routesRow.getCell(RouteColumns.DESCRIPTION.getIndex())))
                .length(formatter.formatCellValue(routesRow.getCell(RouteColumns.LENGTH.getIndex())))
                .time(formatter.formatCellValue(routesRow.getCell(RouteColumns.TIME.getIndex())))
                .imageUrl(formatter.formatCellValue(routesRow.getCell(RouteColumns.IMAGE_URL.getIndex())))
                .points(new ArrayList<>())
                .build();
    }

    private List<String> getPointIdsFromRoutesRow(Row routesRow, DataFormatter formatter) {
        String pointIds = formatter.formatCellValue(routesRow.getCell(RouteColumns.POINT_ID.getIndex()));
        return splitByComma(pointIds);
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

    public List<Route> parseXls(FileInputStream fis) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet pointsSheet = workbook.getSheet("Points");
        XSSFSheet routesSheet = workbook.getSheet("Routes");
        DataFormatter formatter = new DataFormatter();
        Map<String, Point> pointsMap = new HashMap<>();
        List<Route> routes = new ArrayList<>();

        for (Row pointsRow : pointsSheet) {
            if (isEmptyRow(pointsRow, formatter) || pointsRow.getRowNum() == 0) {
                continue;
            }

            Point point = getPointFromRow(pointsRow, formatter);
            pointsMap.put(point.getId(), point);
        }

        for (Row routesRow : routesSheet) {
            if (isEmptyRow(routesRow, formatter) || routesRow.getRowNum() == 0) {
                continue;
            }

            Route route = getRouteFromRow(routesRow, formatter);
            routes.add(route);
            List<String> pointIds = getPointIdsFromRoutesRow(routesRow, formatter);

            pointIds.forEach(pointId -> {
                Point point = pointsMap.get(pointId);
                if (point != null) {
                    route.getPoints().add(point);
                }
            });
        }

        return routes;
    }
}

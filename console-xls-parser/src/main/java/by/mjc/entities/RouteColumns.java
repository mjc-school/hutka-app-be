package by.mjc.entities;

public enum RouteColumns {
    ID(0), NAME(1),DESCRIPTION(2),
//    LAT(3), LNG(4),
    LENGTH(3), TIME(4),
    IMG_URL(5),  POINTS(6);

    private final int index;

    RouteColumns(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

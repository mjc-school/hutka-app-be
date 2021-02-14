package by.mjc.entities;

public enum PlaceColumns {
    ID(0), NAME(1), LAT(2),
    LNG(3), IMG_URL(4), LOCATION_NAME(5),
    LOCATION_LAT(6), LOCATION_LNG(7), TAGS(8),
    DESCRIPTION(9);

    private final int index;

    PlaceColumns(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

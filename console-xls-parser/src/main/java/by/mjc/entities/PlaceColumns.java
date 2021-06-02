package by.mjc.entities;

public enum PlaceColumns {
    ID(0),LOCATION_NAME(1), NAME(2), DESCRIPTION(3), IMG_URL(4) ,
    LOCATION_LAT(5), LOCATION_LNG(6), TAGS(8),
    ;

    private final int index;

    PlaceColumns(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

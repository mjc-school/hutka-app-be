package by.mjc.utils;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xsd.PullParser;
import org.opengis.feature.simple.SimpleFeature;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GeoParser {
    private static InputStream inputStreamFromString(String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }

    private static String stringFromOutputStream(OutputStream outputStream) {
        return outputStream.toString();
    }

    public static String kmlToGeoJson(String kml) throws XMLStreamException, IOException, SAXException {
        PullParser parser = new PullParser(new KMLConfiguration(), inputStreamFromString(kml), SimpleFeature.class);
        FeatureJSON featureJson = new FeatureJSON();
        ArrayList<SimpleFeature> features = new ArrayList<>();
        SimpleFeature simpleFeature = (SimpleFeature) parser.parse();

        while (simpleFeature != null) {
            features.add(simpleFeature);
            simpleFeature = (SimpleFeature) parser.parse();
        }

        SimpleFeatureCollection featuresCollection = DataUtilities.collection(features);
        OutputStream outputStream = new ByteArrayOutputStream();
        featureJson.writeFeatureCollection(featuresCollection, outputStream);

        return stringFromOutputStream(outputStream);
    }
}

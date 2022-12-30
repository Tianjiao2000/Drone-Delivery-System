package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Load details of file from server.
 */
public class LoadInfo {

    // private variables
    private WebServer nowServer;
    private String jdbcString;
    // List of menu details from the menus.json
    private ArrayList<MenusDetails> menusDet;
    // longitude and latitude of given WhatThreeWord
    private double lat;
    private double lng;

    /**
     * LoadInfo constructor
     * @param s - The WebServer
     */
    public LoadInfo(WebServer s) {
        nowServer = s;
    }

    /**
     * load content in no-fly-zone
     * @return features in file no-fly-zone
     */
    public ArrayList<Geometry> getNoFlyZone() {
        String urlZone = "http://" + nowServer.getServerURL() + "/buildings/no-fly-zones.geojson";
        // List of Feature from content of no fly zone.
        List<Feature> noFlyZone = FeatureCollection.fromJson(nowServer.getResponse(urlZone)).features();
        ArrayList<Geometry> noFlyZoneGeo = new ArrayList<Geometry>();
        for (int i = 0; i < noFlyZone.size(); i++) {
            noFlyZoneGeo.add(noFlyZone.get(i).geometry());
        }
        return noFlyZoneGeo;
    }

    /**
     * load content in landmarks
     * @return array of geometry in file landmarks
     */
    public ArrayList<Geometry> getLandmarks() {
        String urlLandmarks = "http://" + nowServer.getServerURL() + "/buildings/landmarks.geojson";
        // List of Feature from content of landmark.
        List<Feature> landMarks = FeatureCollection.fromJson(nowServer.getResponse(urlLandmarks)).features();
        ArrayList<Geometry> landmarkGeo = new ArrayList<Geometry>();
        for (int i = 0; i < landMarks.size(); i++) {
            landmarkGeo.add(landMarks.get(i).geometry());
        }
        return landmarkGeo;
    }

    /**
     * load details.json, convert the WhatThreeWord to latitude and longitude
     * @param w1 - the first word
     * @param w2 - the second word
     * @param w3 - the third word
     */
    public void loadWords(String w1, String w2, String w3) {
        // url for the detail file define the given WhatThreeWord
        String urlWords = "http://" + nowServer.getServerURL() + "/words/" + w1 + '/' +
                w2 + '/' + w3 + "/details.json";
        WordsLocation WordsLoc = new Gson().fromJson(nowServer.getResponse(urlWords), WordsLocation.class);
        // get longitude and latitude of the given WhatThreeWord
        lng = WordsLoc.coordinates.lng;
        lat = WordsLoc.coordinates.lat;
    }

    /**
     * longitude getter
     * @return longitude of the location expressed in WhatThreeWord
     */
    public Double getLng() { return lng; }

    /**
     * latitude getter
     * @return latitude of the location expressed in WhatThreeWord
     */
    public Double getLat() { return lat;}

    /**
     * load the menus.json
     */
    private void loadMenus() {
        String urlMenus = "http://" + nowServer.getServerURL() + "/menus/menus.json";
        Type listType = new TypeToken<ArrayList<MenusDetails>>() {}.getType();
        menusDet = new Gson().fromJson(nowServer.getResponse(urlMenus), listType);
    }

    /**
     * Find the location of the shop sells given item.
     * @param itemName - An array that content list of item name
     * @return List of locations of corresponding shop sells item
     */
    public ArrayList<String> getMenuLoc(ArrayList<String> itemName) {
        loadMenus();
        ArrayList<String> menuLoc = new ArrayList<String>();
        // loop the item names
        for (String n : itemName) {
            for (MenusDetails m : menusDet) {
                // loop the item details in menus
                for (MenusDetails.ItemsDetails i : m.menu) {
                    // find the item and get the location of shop sells this item
                    // avoid repeat location in the list
                    if (i.item.equals(n) & menuLoc.contains(m.location) == false) {
                        menuLoc.add(m.location);
                    }
                }
            }
        }
        return menuLoc;
    }

    /**
     * Find the total price of a list of item.
     * @param itemName - An array that content list of item name.
     * @return total price of all items
     */
    public int getOrderPrice(ArrayList<String> itemName) {
        int totalPrice = 50;
        if (itemName.size() > 4) {
            throw new IllegalArgumentException("Maximum number of items drone can carry 4.");
        }
        loadMenus();
        for (String n : itemName) {
            for (MenusDetails m : menusDet) {
                for (MenusDetails.ItemsDetails i : m.menu) {
                    if (i.item.equals(n)) {
                        totalPrice = totalPrice + i.pence;
                    }
                }
            }
        }
        return totalPrice;
    }

    /**
     * Get order number in given date from derby database orders
     * @param orderDate - Given date to find order number
     * @return List of order number in given date
     */
    public ArrayList<String> getOrderNo(String orderDate) {
        jdbcString = "jdbc:derby://" + nowServer.getServerURL() + "/derbyDB";
        return nowServer.getOrderNoByDate(jdbcString, orderDate);
    }

    /**
     * Get location of delivery in given date from derby database orders
     * @param orderNo - Given order number to find delivery location
     * @return List of locations to be delivered in given date
     */
    public String getOrderDeliver(String orderNo) {
        jdbcString = "jdbc:derby://" + nowServer.getServerURL() + "/derbyDB";
        return nowServer.getDeliverToByNo(jdbcString, orderNo);
    }

    /**
     * Get item ordered in given date from derby database orderDetails.
     * @param orderNo Order number
     * @return List of name of items to be delivered in given order number.
     */
    public ArrayList<String> getDetailsDerby(String orderNo) {
        String jdbcString = "jdbc:derby://" + nowServer.getServerURL() + "/derbyDB";
        return nowServer.getItemByOrderNo(jdbcString, orderNo);
    }

    /**
     * Generate a GeoJson file with given list of longitude and latitude.
     * @param passLng - list of longitude need to write in the geoJson file
     * @param passLat - list of latitude need to write in the geoJson file
     * @return content of featureCollection
     */
    public String generateGeoJson(ArrayList<Double> passLng, ArrayList<Double> passLat) {
        var currFeature = new ArrayList<Feature>();
        ArrayList<Point> points = new ArrayList<Point>();
        // add longitude and latitude one by one, correspondingly into a Point
        for (int i = 0; i < passLat.size(); i++) {
            points.add(Point.fromLngLat(passLng.get(i), passLat.get(i)));
        }
        var path = LineString.fromLngLats(points);
        currFeature.add(Feature.fromGeometry(path));
        var featureCollection = FeatureCollection.fromFeatures(currFeature);
        return featureCollection.toJson();
    }

    /**
     * Derby URL getter
     * @return URL for derby database
     */
    public String getDerbyURL() {
        String derbyURL = "jdbc:derby://" + nowServer.getServerURL() + "/derbyDB";
        return derbyURL;
    }
}

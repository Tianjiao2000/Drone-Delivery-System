package uk.ac.ed.inf;

import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Details and small component in drone flight, preparation for plan the whole route.
 */
public class DeliveryDetails {
    // private variables
    // LoadInfo for derby database
    private LoadInfo infoDerby;
    // LoadInfo for web server
    private LoadInfo infoJson;
    // location to deliver the order
    private String deliverTo;
    // price of delivery
    private int deliverPrice;
    // list of longitude of picking up
    private ArrayList<Double> pickUpLng = new ArrayList<>();
    // list of latitude of picking up
    private ArrayList<Double> pickUpLat = new ArrayList<>();
    // list of longitude of landmarks
    private ArrayList<Double> landmarksLng = new ArrayList<>();
    // list of latitude of landmarks
    private ArrayList<Double> landmarksLat = new ArrayList<>();

    /**
     * DeliveryDetails constructor
     * @param serverName - IP of the server
     * @param derbyPort - port to connect to derby database
     * @param webPort - port to connect to web server
     */
    public DeliveryDetails(String serverName, String derbyPort, String webPort) {
        infoDerby  = new LoadInfo(new WebServer(serverName, derbyPort));
        infoJson = new LoadInfo(new WebServer(serverName, webPort));
    }

    /**
     * Load deliver to, list of items, deliver price, location of shops.
     * From WhatThreeWord location of shops, convert it to longitude and latitude and save it in lists.
     * @param orderNo
     */
    public void deliverInfo(String orderNo) {
        deliverTo = infoDerby.getOrderDeliver(orderNo);
        ArrayList<String> items = infoDerby.getDetailsDerby(orderNo);
        //deliverList.add(items);
        deliverPrice = infoJson.getOrderPrice(items);
        ArrayList<String> pickUp = infoJson.getMenuLoc(items);
        pickUpLng = new ArrayList<>();
        pickUpLat = new ArrayList<>();
        for (String s : pickUp) {
            String[] splitWord = s.split("\\.");
            // convert format of WhatThreeWord to longitude and latitude
            infoJson.loadWords(splitWord[0], splitWord[1], splitWord[2]);
            // list of longitude of shops
            pickUpLng.add(infoJson.getLng());
            // list of latitude of shops
            pickUpLat.add(infoJson.getLat());
        }
    }

    /**
     * Getter of list of longitude of shops.
     * @return list of longitude of shops
     */
    public ArrayList<Double> getPickUpLng() { return pickUpLng; }
    /**
     * Getter of list of latitude of shops.
     * @return list of latitude of shops
     */
    public ArrayList<Double> getPickUpLat() { return pickUpLat; }

    /**
     * Getter of deliver to in given order.
     * @param orderNo - order number
     * @return deliver to location
     */
    public String getDeliverTo(String orderNo) {
        String deliverTo;
        deliverTo = infoDerby.getOrderDeliver(orderNo);
        return deliverTo;
    }

    /**
     * Convert format of deliver to from WhatThreeWord to longitude and latitude and save in a list.
     * @return list express location of deliver to
     */
    public ArrayList<Double> getDeliverCoordinate() {
        String[] splitWord = deliverTo.split("\\.");
        // convert from WhatThreeWord to longitude and latitude
        infoJson.loadWords(splitWord[0], splitWord[1], splitWord[2]);
        // since only one location to deliver,
        // write in a list in the format: {longitude, latitude}
        ArrayList<Double> deliverCoordinate = new ArrayList<>();
        deliverCoordinate.add(infoJson.getLng());
        deliverCoordinate.add(infoJson.getLat());
        return deliverCoordinate;
    }

    /**
     * Getter of delivery price.
     * @return delivery price
     */
    public int getDeliverPrice() { return deliverPrice; }

    /**
     * Load location of landmarks.
     */
    public void landMarkLoc() {
        ArrayList<Geometry> landmarkGeo = infoJson.getLandmarks();
        for (int i = 0; i < landmarkGeo.size(); i++) {
            var temp = (Point) landmarkGeo.get(i);
            // longitude of landmarks
            landmarksLng.add(temp.coordinates().get(0));
            // latitude of landmarks
            landmarksLat.add(temp.coordinates().get(1));
        }
    }

    /**
     * Getter of longitude of all landmarks
     * @return list of longitude of all landmarks
     */
    public ArrayList<Double> getLandmarksLng() { return landmarksLng; }
    /**
     * Getter of latitude of all landmarks
     * @return list of latitude of all landmarks
     */
    public ArrayList<Double> getLandmarksLat() { return landmarksLat; }


    /**
     * Shortest distance from current position to the next position.
     * @param currLng - longitude of the current position
     * @param currLat - latitude of the current position
     * @param nextLng - longitude of the next position
     * @param nextLat - latitude of the next position
     * @return Shortest move distance
     */
    public double getMoveValue(double currLng, double currLat, double nextLng, double nextLat) {
        ArrayList<Double> moveValue = new ArrayList<>();
        // if drone can go straight forward
        if (validMove(currLat, currLng, nextLat, nextLng)) {
            moveValue.add(Math.sqrt(Math.pow(currLat - nextLat, 2) + Math.pow(currLng - nextLng, 2)));
        } else {
            // if cannot go straight line, it must pass through landmarks
            // loop the landmark
            for (int j = 0; j < landmarksLat.size(); j++) {
                double transLat = landmarksLat.get(j);
                double transLng = landmarksLng.get(j);
                // if valid move from current position - landmark - next position
                if (validMove(currLat, currLng, transLat, transLng) & validMove(transLat, transLng, nextLat, nextLng)) {
                    // calculate the distance: current position to landmark + landmark to next position
                    moveValue.add(Math.sqrt(Math.pow(currLat - transLat, 2) + Math.pow(currLng - transLng, 2)) +
                            Math.sqrt(Math.pow(transLat - nextLat, 2) + Math.pow(transLng - nextLng, 2)));
                }
            }
        }
        // return the shortest distance
        return Collections.min(moveValue);
    }

    /**
     * Find the landmark let the drone move shortest distance.
     * @param currLng - longitude of the current position
     * @param currLat - latitude of the current position
     * @param nextLng - longitude of the next position
     * @param nextLat - latitude of the next position
     * @return Closest landmark in pair of longitude and latitude
     */
    public ArrayList<Double> getClosestLandmark(double currLng, double currLat, double nextLng, double nextLat) {
        ArrayList<Double> closestLandmark = new ArrayList<>(); // lng, lat
        ArrayList<Double> landmarkDistance = new ArrayList<>();
        ArrayList<Double> tempLat = new ArrayList<>();
        ArrayList<Double> tempLng = new ArrayList<>();
        // loop all landmarks
        for (int i = 0; i < landmarksLat.size(); i++) {
            double transLat = landmarksLat.get(i);
            double transLng = landmarksLng.get(i);
            // if it is a valid move through landmark
            if (validMove(currLat, currLng, transLat, transLng) && validMove(transLat, transLng, nextLat, nextLng)) {
                // calculate the distance
                landmarkDistance.add(Math.sqrt(Math.pow(currLat - transLat, 2) + Math.pow(currLng - transLng, 2)) +
                        Math.sqrt(Math.pow(transLat - nextLat, 2) + Math.pow(transLng - nextLng, 2)));
                // store the valid landmark
                tempLat.add(transLat);
                tempLng.add(transLng);
            }
        }
        // loop the distance to find the landmark with shortest move distance
        for (int j = 0; j < landmarkDistance.size(); j++) {
            // use the index to find the landmark has shortest distance
            if (Collections.min(landmarkDistance).equals(landmarkDistance.get(j))) {
                // save it in a longitude latitude pair
                closestLandmark.add(tempLng.get(j));
                closestLandmark.add(tempLat.get(j));
            }
        }
        return closestLandmark;
    }

    /**
     * Determine whether it is a valid movement from current position to the next position
     * @param currLat - latitude of the current position
     * @param currLng - longitude of the current position
     * @param nextLat - latitude of the next position
     * @param nextLng - longitude of the next position
     * @return whether it is a valid move
     */
    public Boolean validMove(double currLat, double currLng, double nextLat, double nextLng) {
        // line of current position to next position
        Line2D expectPath = new Line2D.Double(currLat, currLng, nextLat, nextLng);
        // load details of no fly zone
        ArrayList<Geometry> noFlyZoneGeo = infoJson.getNoFlyZone();
        Boolean validFlag = true;
        // make sure current position and next position are in confinement area
        validFlag = validFlag && isConfined(currLng, currLat);
        validFlag = validFlag && isConfined(nextLng, nextLat);
        // loop each part of no fly zone
        for (int i = 0; i < noFlyZoneGeo.size(); i++) {
            var tempPolygon = (Polygon) noFlyZoneGeo.get(i);
            var tempPoints = tempPolygon.coordinates().get(0);
            // loop the vertex which consist the no fly zone
            for (int j = 0; j < tempPoints.size() - 1; j++) {
                // index of the next vertex
                int para = (j + 1) % tempPoints.size();
                // line of current vertex to next vertex
                Line2D noFlyBarrier = new Line2D.Double(tempPoints.get(j).latitude(), tempPoints.get(j).longitude(),
                        tempPoints.get(para).latitude(), tempPoints.get(para).longitude());
                // if line of current position and next position intersect
                // line of current vertex to next vertex, is pass through the no fly zone
                if (expectPath.intersectsLine(noFlyBarrier)) {
                    // not a valid move if intersection happens
                    validFlag = validFlag && false;
                    break;
                }
            }
            // as long as it is not a valid move, break the loop to same running time
            if (validFlag.equals(false)) {
                break;
            }
        }
        return validFlag;
    }

    /**
     * Check if the point is within the drone confinement area.
     * @return whether the point is within the drone confinement area
     */
    private boolean isConfined(double currLng, double currLat) {
        // Make sure the given point's latitude lies between 55.942617 and 55.946233
        // and longitude lies between −3.184319 and −3.192473, which is the confinement area
        if (currLng >= -3.192473 & currLng <= -3.184319
                & currLat >= 55.942617 & currLat <= 55.946233) {
            return true;
        } else {
            return false;
        }
    }
}

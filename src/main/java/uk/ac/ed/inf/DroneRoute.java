package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.DeflaterOutputStream;

/**
 * Plan the route of the flight.
 */
public class DroneRoute {
    // private variables
    private LoadInfo infoDerby;
    // longitude of start of the flight
    private double lng;
    // latitude of start of the flight
    private double lat;
    private DeliveryDetails deliveryDet;
    private String currDate;
    private ArrayList<Integer> priceList = new ArrayList<>();
    private ArrayList<String> allOrderNo = new ArrayList<>();
    private ArrayList<String> deliverToList = new ArrayList<>();
    // longitude of start point, each shop, landmark, deliver to that are travel in a flight
    private ArrayList<Double> passLng = new ArrayList<>();
    // latitude of start point, each shop, landmark, deliver to that are travel in a flight
    private ArrayList<Double> passLat = new ArrayList<>();
    // longitude of each move in a flight
    private ArrayList<Double> moveLng = new ArrayList<>();
    // latitude of each move in a flight
    private ArrayList<Double> moveLat = new ArrayList<>();
    // save order temporarily in a flight
    private ArrayList<String> orderTemp = new ArrayList<>();
    // order number corresponding to every move
    private ArrayList<String> orderMove = new ArrayList<>();
    private ArrayList<Integer> moveAngle = new ArrayList<>();
    private int stepOfMove = 0;
    // if step of move greater than 1500, use it to determine the moves when it go back to start point
    private ArrayList<Double> stepLng = new ArrayList<>();
    private ArrayList<Double> stepLat = new ArrayList<>();
    private ArrayList<Integer> stepAngle = new ArrayList<>();
    private ArrayList<String> sortOrder = new ArrayList<>();
    // if step of move greater than 1500, return different result
    ArrayList<Double> newMoveLng = new ArrayList<>();
    ArrayList<Double> newMoveLat = new ArrayList<>();
    ArrayList<String> newOrderMove = new ArrayList<>();
    ArrayList<Integer> newMoveAngle = new ArrayList<>();
    private ArrayList<Integer> newPriceList = new ArrayList<>();
    private ArrayList<String> newAllOrderNo = new ArrayList<>();
    private ArrayList<String> newDeliverToList = new ArrayList<>();


    /**
     * Constructor of DroneRoute
     * @param serverName - IP for connection
     * @param derbyPort - port number to connect to derby database
     * @param webPort - port number to connect to web server
     * @param ln - longitude of the starting point
     * @param la - latitude of the starting point
     * @param date - date of flight
     */
    public DroneRoute(String serverName, String derbyPort, String webPort, Double ln, Double la, String date) { //9898
        infoDerby  = new LoadInfo(new WebServer(serverName, derbyPort));
        lng = ln;
        lat = la;
        deliveryDet = new DeliveryDetails(serverName, derbyPort, webPort);
        currDate = date;
    }

    /**
     * Find the sequence of deliver orders
     * @return sequence of order numbers
     */
    public ArrayList<String> planRoute() {
        // get all order numbers in given date
        allOrderNo = infoDerby.getOrderNo(currDate);
        ArrayList<Double> valuePerMove = new ArrayList<>();
        deliveryDet.landMarkLoc();
        // find move distance for all orders
        for (int i = 0; i < allOrderNo.size(); i++) {
            // load information of current order
            deliveryDet.deliverInfo(allOrderNo.get(i));
            deliverToList.add(deliveryDet.getDeliverTo(allOrderNo.get(i)));
            ArrayList<Double> deliverTo = deliveryDet.getDeliverCoordinate(); // lng, lat
            int orderPrice = deliveryDet.getDeliverPrice();
            priceList.add(orderPrice);
            ArrayList<Double> pickUpLng = deliveryDet.getPickUpLng();
            ArrayList<Double> pickUpLat = deliveryDet.getPickUpLat();
            // if the order need to pick up item from 2 shops,
            // first shop - second shop - deliver to, second shop - first shop - deliver to
            // loop to determine which path is the shortest and calculate its value per move
            if (pickUpLat.size() > 1) {
                ArrayList<Double> tempValue = new ArrayList<>();
                tempValue.add(
                        // 1st shop to 2nd
                        deliveryDet.getMoveValue(pickUpLng.get(0), pickUpLat.get(0),
                                pickUpLng.get(1), pickUpLat.get(1)) +
                                // 2nd to deliver
                                deliveryDet.getMoveValue(pickUpLng.get(1), pickUpLat.get(1),
                                        deliverTo.get(0), deliverTo.get(1)));
                tempValue.add(
                        deliveryDet.getMoveValue(pickUpLng.get(1), pickUpLat.get(1),
                                pickUpLng.get(0), pickUpLat.get(0)) +
                                deliveryDet.getMoveValue(pickUpLng.get(0), pickUpLat.get(0),
                                        deliverTo.get(0), deliverTo.get(1)));
                valuePerMove.add(orderPrice / Collections.min(tempValue));
            } else {
                // if 1 shop calculate value per move directly
                valuePerMove.add(orderPrice / (
                        // shop to deliver
                        deliveryDet.getMoveValue(pickUpLng.get(0), pickUpLat.get(0),
                                deliverTo.get(0), deliverTo.get(1))));
            }
        }
        ArrayList<String> sortedOrder = new ArrayList<>();
        ArrayList<Double> tempValuePerMove = new ArrayList<>();
        for (double v : valuePerMove) {
            tempValuePerMove.add(v);
        }
        for (int i = 0; i < allOrderNo.size(); i++) {
            // greatest value per move
            double temp = Collections.max(tempValuePerMove);
            for (int j = 0; j < valuePerMove.size(); j++) {
                // find index of greatest value per move
                if (temp == valuePerMove.get(j)) {
                    // add corresponding order number by index
                    sortedOrder.add(allOrderNo.get(j));
                    // remove the greatest value to find the second greatest
                    tempValuePerMove.remove(temp);
                    break;
                }
            }
        }
        return sortedOrder;
    }

    /**
     * Go through flight of the drone to plan the delivery route
     * @param sortedOrder - order number in sequence of delivery
     */
    public void droneDelivery(ArrayList<String> sortedOrder) {
        // first current position is start point
        sortOrder = sortedOrder;
        double currLng = lng;
        double currLat = lat;
        // add start point into flight route
        passLng.add(currLng);
        passLat.add(currLat);
        for (String currOrder : sortedOrder) {
            deliveryDet.deliverInfo(currOrder);
            ArrayList<Double> deliverTo = deliveryDet.getDeliverCoordinate(); // lng, lat
            ArrayList<Double> pickUpLng = deliveryDet.getPickUpLng();
            ArrayList<Double> pickUpLat = deliveryDet.getPickUpLat();
            // from start to shops
            // if there are more than one shops need to pick up from
            if (pickUpLat.size() > 1) {
                // determine which shop visit first for shorter moves
                int firstShop = getClosestDistance(currLng, currLat, pickUpLng, pickUpLat, deliverTo);
                if (firstShop == 0) {
                    // go to the first shop first
                    addLandmark(currOrder, currLng, currLat, pickUpLng.get(0), pickUpLat.get(0));
                    // go to the second shop from first shop
                    addLandmark(currOrder, pickUpLng.get(0), pickUpLat.get(0), pickUpLng.get(1), pickUpLat.get(1));
                    // go to delivery
                    addLandmark(currOrder, pickUpLng.get(1), pickUpLat.get(1), deliverTo.get(0), deliverTo.get(1));
                } else {
                    // go to the second shop first
                    addLandmark(currOrder, currLng, currLat, pickUpLng.get(1), pickUpLat.get(1));
                    // go to the first shop from second shop
                    addLandmark(currOrder, pickUpLng.get(1), pickUpLat.get(1), pickUpLng.get(0), pickUpLat.get(0));
                    // go to delivery
                    addLandmark(currOrder, pickUpLng.get(0), pickUpLat.get(0), deliverTo.get(0), deliverTo.get(1));
                }
            } else {
                // only 1 shop to pick up
                addLandmark(currOrder, currLng, currLat, pickUpLng.get(0), pickUpLat.get(0));
                // from shop to deliver
                addLandmark(currOrder, pickUpLng.get(0), pickUpLat.get(0), deliverTo.get(0), deliverTo.get(1));
            }
            // current position becomes location of deliver to
            // start sending next order from this location
            currLng = deliverTo.get(0);
            currLat = deliverTo.get(1);
        }
        // after all order delivered go back to the start point
        addLandmark(sortedOrder.get(sortedOrder.size() - 1), currLng, currLat, lng, lat);
    }

    /**
     * If the movement need go through the landmark, add it into flight route
     * Also add the next position into the flight route
     * @param currOrder - current order number
     * @param currLng - longitude of the current position
     * @param currLat - latitude of the current position
     * @param nextLng - longitude of the next position
     * @param nextLat - latitude of the next position
     */
    private void addLandmark(String currOrder, double currLng, double currLat, double nextLng, double nextLat) {
        if (!deliveryDet.validMove(currLat, currLng, nextLat, nextLng)) {
            // closest landmark to finish the step of movement
            ArrayList<Double> landmarkValue = // lng, lat
                    deliveryDet.getClosestLandmark(currLng, currLat, nextLng, nextLat);
            // add the landmark location into the flight route
            passLng.add(landmarkValue.get(0));
            passLat.add(landmarkValue.get(1));
            // if the flight go through the landmark,
            // add the corresponding order number to this step of movement
            orderTemp.add(currOrder);
        }
        // add the next position into the flight route
        passLng.add(nextLng);
        passLat.add(nextLat);
        // add the corresponding order number to this step of movement
        orderTemp.add(currOrder);
    }

    /**
     * When there are two shops need to pick up items,
     * determine which shop visit first to have the shortest whole distance
     * @param currLng - longitude of the current position
     * @param currLat - latitude of the current position
     * @param pickUpLng - list of longitude of shops
     * @param pickUpLat - list of latitude of shops
     * @param deliverTo - pair of longitude and latitude of deliver to
     * @return index of shop to visit first
     */
    private Integer getClosestDistance(double currLng, double currLat, ArrayList<Double> pickUpLng,
                                       ArrayList<Double> pickUpLat, ArrayList<Double> deliverTo) {
        // move value of two different flight
        ArrayList<Double> tempV = new ArrayList<>();
        tempV.add(
                // current position to the first shop
                deliveryDet.getMoveValue(currLng, currLat, pickUpLng.get(0), pickUpLat.get(0)) +
                        // 1st shop to 2nd shop
                        deliveryDet.getMoveValue(pickUpLng.get(0), pickUpLat.get(0), pickUpLng.get(1), pickUpLat.get(1)) +
                        // 2nd to deliver
                        deliveryDet.getMoveValue(pickUpLng.get(1), pickUpLat.get(1), deliverTo.get(0), deliverTo.get(1)));
        tempV.add(deliveryDet.getMoveValue(currLng, currLat, pickUpLng.get(1), pickUpLat.get(1)) +
                deliveryDet.getMoveValue(pickUpLng.get(1), pickUpLat.get(1), pickUpLng.get(0), pickUpLat.get(0)) +
                deliveryDet.getMoveValue(pickUpLng.get(0), pickUpLat.get(0), deliverTo.get(0), deliverTo.get(1)));
        // return index of shop visit first to have the shortest whole distance
        if (tempV.get(0) <= tempV.get(1)) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Each step has distance of 0.00015, determine each step of movement from general flight path.
     */
    public void moveStep() {
        double currLng = passLng.get(0);
        double currLat = passLat.get(0);
        // add the start point to flight path
        moveLng.add(currLng);
        moveLat.add(currLat);
        // loop the general flight path
        for (int i = 0; i < passLat.size() - 1; i++) {
            // until drone close to the next position, add each step of move to flight path
            while (true) {
                // calculate the radian angle of the flight
                var angle = Math.atan2((passLat.get(i+1) - currLat), (passLng.get(i+1) - currLng));
                // change radians to degree
                var theta = angle * (180 / Math.PI);
                // angle cannot be negative
                if (theta < 0) {
                    theta = 360 + theta;
                }
                // angle need to be multiple of 10, round the angle
                if (theta % 10 < 5) {
                    theta = Math.floor(theta / 10) * 10;
                } else {
                    theta = Math.floor(theta / 10) * 10 + 10;
                }
                // angle range is 0 to 350, change it to 0 if it is 360
                if (theta == 360) { theta = 0; }
                // convert angle from degree to radian for calculation
                Double radAngle = (theta * Math.PI) / 180;
                // whether step fly in this angle is a valid move
                if (!deliveryDet.validMove(currLat, currLng, currLat + 0.00015 * Math.sin(radAngle),
                        currLng + 0.00015 * Math.cos(radAngle))) {
                    // try theta add 10
                    theta = theta + 10;
                    radAngle = (theta * Math.PI) / 180;
                    if (!deliveryDet.validMove(currLat, currLng, currLat + 0.00015 * Math.sin(radAngle),
                            currLng + 0.00015 * Math.cos(radAngle))) {
                        // if not work then try minus 10
                        theta = theta - 20;
                        radAngle = (theta * Math.PI) / 180;
                    }
                }
                // move the current position
                currLng = currLng + 0.00015 * Math.cos(radAngle);
                currLat = currLat + 0.00015 * Math.sin(radAngle);
                // add the new position
                moveLng.add(currLng);
                moveLat.add(currLat);
                stepOfMove = stepOfMove + 1;
                // add the corresponding order number
                orderMove.add(orderTemp.get(i));
                // add angle of movement
                moveAngle.add((int) theta);
                // whether is close to the shop, deliver to and destination
                if (Math.sqrt(Math.pow(currLng - passLng.get(i+1), 2) +
                        Math.pow(currLat - passLat.get(i+1), 2)) < 0.00015) {
                    // if it is a landmark or destination not hovering
                    if (!(deliveryDet.getLandmarksLat().contains(passLat.get(i+1)) &&
                            deliveryDet.getLandmarksLng().contains(passLng.get(i+1))) &&
                            !(Math.sqrt(Math.pow(currLng - lng, 2) + Math.pow(currLat - lat, 2)) < 0.00015)) {
                        // else hover one more move
                        moveLng.add(currLng);
                        moveLat.add(currLat);
                        stepOfMove = stepOfMove + 1;
                        orderMove.add(orderTemp.get(i));
                        // angle is -999
                        moveAngle.add(-999);
                    }
                    break;
                }
            }
        }
        if (stepOfMove > 1500) {
            outOfMoves();
        }
    }

    /**
     * If the drone cannot finish flight within 1500 moves,
     * flight before its battery runs out need to be reschedule.
     */
    private void outOfMoves() {
        ArrayList<String> orderSave = new ArrayList<>();
        double deliverToLng = 0;
        double deliverToLat = 0;
        int step = 0;
        for (int i = 0; i < sortOrder.size(); i++) {
            for (int j = 0; j < orderMove.size(); j++) {
                if (sortOrder.get(i).equals(orderMove.get(j))) {
                    deliverToLng = moveLng.get(j);
                    deliverToLat = moveLat.get(j);
                    step = j;
                }
            }
            if (step + runOutOfBattery(deliverToLng, deliverToLat) <= 1500) {
                orderSave.add(sortOrder.get(i));
            }
        }
        // find index of point drone needs to return
        int removeIndex = 0;
        for (int k = 0; k < orderMove.size(); k++) {
            if (orderSave.get(orderSave.size() - 1).equals(orderMove.get(k))) {
                removeIndex = k;
                break;
            }
        }
        for (int i = 0; i < removeIndex; i++) {
            newMoveLng.add(moveLng.get(i));
            newMoveLat.add(moveLat.get(i));
            newOrderMove.add(orderMove.get(i));
            newMoveAngle.add(moveAngle.get(i));
        }
        // add new flight from current position to start point
        runOutOfBattery(newMoveLng.get(newMoveLng.size() - 1), newMoveLat.get(newMoveLat.size() -1));
        for (int i = 0; i < stepLng.size(); i++) {
            newMoveLng.add(stepLng.get(i));
            newMoveLat.add(stepLat.get(i));
            newOrderMove.add(orderMove.get(removeIndex));
            newMoveAngle.add(stepAngle.get(i));
        }
    }

    /**
     * Step from current position back to start point
     * @param currLng - longitude of current position
     * @param currLat - latitude of current position
     * @return step of move from current position back to start point
     */
    private Integer runOutOfBattery(double currLng, double currLat) {
        ArrayList<Double> toDestinationLng = new ArrayList<>();
        ArrayList<Double> toDestinationLat = new ArrayList<>();
        stepLng = new ArrayList<>();
        stepLat = new ArrayList<>();
        int toDestination = 0;
        if (!deliveryDet.validMove(currLat, currLng, lat, lng)) {
            ArrayList<Double> passLandmark = deliveryDet.getClosestLandmark(currLng, currLat, lng, lat); //lng, lat
            toDestinationLng.add(passLandmark.get(0));
            toDestinationLat.add(passLandmark.get(1));
        }
        toDestinationLng.add(lng);
        toDestinationLat.add(lat);
        for (int i = 0; i < toDestinationLat.size(); i++) {
            // until drone close to the next position, add each step of move to flight path
            while (true) {
                // calculate the radian angle of the flight
                var angle = Math.atan2((toDestinationLat.get(i) - currLat), (toDestinationLng.get(i) - currLng));
                // change radians to degree
                var theta = angle * (180 / Math.PI);
                // angle cannot be negative
                if (theta < 0) {
                    theta = 360 + theta;
                }
                // angle need to be multiple of 10, round the angle
                if (theta % 10 < 5) {
                    theta = Math.floor(theta / 10) * 10;
                } else {
                    theta = Math.floor(theta / 10) * 10 + 10;
                }
                // angle range is 0 to 350, change it to 0 if it is 360
                if (theta == 360) { theta = 0; }
                // convert angle from degree to radian for calculation
                Double radAngle = (theta * Math.PI) / 180;
                // whether step fly in this angle is a valid move
                if (!deliveryDet.validMove(currLat, currLng, currLat + 0.00015 * Math.sin(radAngle),
                        currLng + 0.00015 * Math.cos(radAngle))) {
                    // try theta add 10
                    theta = theta + 10;
                    radAngle = (theta * Math.PI) / 180;
                    if (!deliveryDet.validMove(currLat, currLng, currLat + 0.00015 * Math.sin(radAngle),
                            currLng + 0.00015 * Math.cos(radAngle))) {
                        // if not work then try minus 10
                        theta = theta - 20;
                        radAngle = (theta * Math.PI) / 180;
                    }
                }
                // move the current position
                currLng = currLng + 0.00015 * Math.cos(radAngle);
                currLat = currLat + 0.00015 * Math.sin(radAngle);
                stepLng.add(currLng);
                stepLat.add(currLat);
                stepAngle.add((int) theta);
                toDestination = toDestination + 1;
                // whether is close to the shop, deliver to and destination
                if (Math.sqrt(Math.pow(currLng - toDestinationLng.get(i), 2) +
                        Math.pow(currLat - toDestinationLat.get(i), 2)) < 0.00015) {
                    break;
                }
            }
        }
        return toDestination;
    }

    /**
     * Getter of allOrderNo
     * @return allOrderNo
     */
    public ArrayList<String> getAllOrderNo() {
        if (stepOfMove > 1500) {
            for (int i = 0; i < newOrderMove.size(); i++) {
                if (!newAllOrderNo.contains(newOrderMove.get(i))) {
                    newAllOrderNo.add(newOrderMove.get(i));
                }
            }
            return newAllOrderNo;
        } else {
            return allOrderNo;
        }
    }

    /**
     * Getter of deliverToList
     * @return deliverToList
     */
    public ArrayList<String> getDeliverToList() {
        if (stepOfMove > 1500) {
            for (String s : newAllOrderNo) {
                deliveryDet.deliverInfo(s);
                newDeliverToList.add(deliveryDet.getDeliverTo(s));
            }
            return newDeliverToList;
        } else {
            return deliverToList;
        }
    }
    /**
     * Getter of priceList
     * @return priceList
     */
    public ArrayList<Integer> getPriceList() {
        if (stepOfMove > 1500) {
            for (String s : newAllOrderNo) {
                deliveryDet.deliverInfo(s);
                newPriceList.add(deliveryDet.getDeliverPrice());
            }
            return newPriceList;
        } else {
            return priceList;
        }
    }

    /**
     * Getter of order numbers of each movement
     * @return list of order number
     */
    public ArrayList<String> getOrderMove() {
        if (stepOfMove > 1500) {
            return newOrderMove;
        } else {
            return orderMove;
        }
    }

    /**
     * Getter of angles of each movement
     * @return list of angles
     */
    public ArrayList<Integer> getMoveAngle() {
        if (stepOfMove > 1500) {
            return newMoveAngle;
        } else {
            return moveAngle;
        }
    }

    /**
     * Getter of longitude of each step
     * @return list of longitude of each step
     */
    public ArrayList<Double> getMoveLng() {
        if (stepOfMove > 1500) {
            return newMoveLng;
        } else {
            return moveLng;
        }
    }
    /**
     * Getter of latitude of each step
     * @return list of latitude of each step
     */
    public ArrayList<Double> getMoveLat() {
        if (stepOfMove > 1500) {
            return newMoveLat;
        } else {
            return moveLat;
        }
    }
}













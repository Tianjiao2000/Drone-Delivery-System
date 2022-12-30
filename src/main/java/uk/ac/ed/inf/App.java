package uk.ac.ed.inf;

/**
 * App for initiating a drone flight.
 */
public class App {
    // IP of the server
    private static final String IP = "localhost";
    // start longitude of the flight
    private static final Double startLng = -3.186874;
    // start latitude of the flight
    private static final Double startLat = 55.944494;

    /**
     * Main
     * @param args
     * [0] - flight day
     * [1] - flight month
     * [2] - flight year
     * [3] - port for web connection
     * [4] - port for derby connection
     */
    public static void main(String[] args) {
        String day = args[0];
        String month = args[1];
        String year = args[2];
        String webPort = args[3];
        String derbyPort = args[4];

        String date = year + '-' + month + '-' + day;

        DroneRoute droneRoute = new DroneRoute(IP, derbyPort, webPort, startLng, startLat, date);
        SaveFlight saveFlight = new SaveFlight(droneRoute);
        LoadInfo infoDerby = new LoadInfo(new WebServer(IP, derbyPort));
        String derbyURL = infoDerby.getDerbyURL();

        // plan the flight and save information into table deliveries
        System.out.println("> PLAN THE ROUTE OF DELIVERY...");
        droneRoute.droneDelivery(droneRoute.planRoute());
        droneRoute.moveStep();
        System.out.println("> CREATE TABLE DELIVERIES...");
        saveFlight.createDeliveries(derbyURL);
        System.out.println("> WRITING DELIVERIES...");
        saveFlight.writeDeliveries(derbyURL);
        System.out.println("> FINISH WRITING DELIVERIES");

        // fly the drone in every step of move and save information into table flightPath
        System.out.println("> FLY THE DRONE STEP BY STEP...");
        System.out.println("> CREATE TABLE FLIGHTPATH...");
        saveFlight.createFlightPath(derbyURL);
        System.out.println("> WRITING FLIGHTPATH...");
        saveFlight.writeFlightPath(derbyURL);
        System.out.println("> FINISH WRITING FLIGHTPATH...");

        // save flight path into geoJson file
        String geoJsonFileName = "drone-" + day + '-' + month + '-' + year + ".geojson";
        LoadInfo infoJson = new LoadInfo(new WebServer(IP, webPort));
        System.out.println("> WRITING GEOJSON FILE...");
        saveFlight.drawGeoJson(droneRoute, infoJson, geoJsonFileName);
        System.out.println("> FINISH WRITING GEOJSON FILE...");
    }
}

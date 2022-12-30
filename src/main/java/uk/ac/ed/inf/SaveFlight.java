package uk.ac.ed.inf;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

/**
 * Create table and file to save flight information.
 */
public class SaveFlight {
    // private variables
    private DroneRoute droneRoute;
    private Connection orderConn;

    /**
     * Constructor of SaveFlight
     * @param dr
     */
    public SaveFlight(DroneRoute dr) {
        droneRoute = dr;
    }

    /**
     * Create deliveries table
     * @param derbyURL - URL for this table
     * @throws SQLException Fail to create the table
     */
    public void createDeliveries(String derbyURL) {
        Statement statement;
        try {
            orderConn = DriverManager.getConnection(derbyURL);
            statement = orderConn.createStatement();
            DatabaseMetaData databaseMetadata = orderConn.getMetaData();
            ResultSet resultSet = databaseMetadata.getTables(null, null,
                    "DELIVERIES", null);
            // if the target table already exist, drop the new created one
            if (resultSet.next()) {
                statement.execute("drop table deliveries");
            }
            // create table deliveries
            statement.execute("create table deliveries(" + "orderNo char(8)," +
                    "deliveredTo varchar(19)," + "costInPence int)");
        } catch (SQLException e) {
            System.err.println("Fail to create the table.");
            e.printStackTrace();
        }
    }

    /**
     * Write content of deliveries into the table Deliveries
     * @param derbyURL - URL for this table
     */
    public void writeDeliveries(String derbyURL) {
        ArrayList<String> orderNo = droneRoute.getAllOrderNo();
        ArrayList<String> deliveredTo = droneRoute.getDeliverToList();
        ArrayList<Integer> costInPence = droneRoute.getPriceList();
        PreparedStatement deliveriesQuery = null;
        try {
            orderConn = DriverManager.getConnection(derbyURL);
            final String sql = "insert into DELIVERIES values((?),(?),(?))";
            deliveriesQuery = orderConn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < orderNo.size(); i++) {
                // write each content
                deliveriesQuery.setString(1, orderNo.get(i));
                deliveriesQuery.setString(2, deliveredTo.get(i));
                deliveriesQuery.setInt(3, costInPence.get(i));
                deliveriesQuery.execute();
            }
        } catch (SQLException e) {
            System.err.println("Fail to write into the table.");
            e.printStackTrace();
        }
    }

    /**
     * Create flight path table
     * @param derbyURL - URL for this table
     * @throws SQLException Fail to create the table
     */
    public void createFlightPath(String derbyURL) {
        Statement statement;
        try {
            orderConn = DriverManager.getConnection(derbyURL);
            statement = orderConn.createStatement();
            DatabaseMetaData databaseMetadata = orderConn.getMetaData();
            ResultSet resultSet = databaseMetadata.getTables(null, null,
                    "FLIGHTPATH", null);
            // if table already exist drop the new created one
            if (resultSet.next()) {
                statement.execute("drop table flightpath");
            }
            statement.execute("create table flightpath(" + "orderNo char(8)," +
                    "fromLongitude double," + "fromLatitude double," +
                    "angle integer," + "toLongitude double," + "toLatitude double)");
        } catch (SQLException e) {
            System.err.println("Fail to create the table.");
            e.printStackTrace();
        }
    }

    /**
     * Write content of flight path into the table FlightPath
     * @param derbyURL - URL for this table
     */
    public void writeFlightPath(String derbyURL) {
        ArrayList<String> orderNo = droneRoute.getOrderMove();
        ArrayList<Double> moveLng = droneRoute.getMoveLng();
        ArrayList<Double> moveLat = droneRoute.getMoveLat();
        ArrayList<Integer> angle = droneRoute.getMoveAngle();
        PreparedStatement flightPathQuery = null;
        try {
            orderConn = DriverManager.getConnection(derbyURL);
            final String sql = "insert into flightpath values((?),(?),(?),(?),(?),(?))";
            flightPathQuery = orderConn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < orderNo.size() - 1; i++) {
                flightPathQuery.setString(1, orderNo.get(i));
                flightPathQuery.setDouble(2, moveLng.get(i));
                flightPathQuery.setDouble(3, moveLat.get(i));
                flightPathQuery.setInt(4, angle.get(i));
                flightPathQuery.setDouble(5, moveLng.get(i+1));
                flightPathQuery.setDouble(6, moveLat.get(i+1));
                flightPathQuery.execute();
            }
        } catch (SQLException e) {
            System.err.println("Fail to write into the table.");
            e.printStackTrace();
        }
    }

    /**
     * Save flight path into a geoJson file for visualisation
     * @param droneRoute - class DroneRoute
     * @param infoJson - class LoadInfo
     * @param fileName - file name to save the content
     * @throws FileNotFoundException create file fail
     */
    public void drawGeoJson(DroneRoute droneRoute, LoadInfo infoJson, String fileName) {
        ArrayList<Double> passLat = droneRoute.getMoveLat();
        ArrayList<Double> passLng = droneRoute.getMoveLng();
        // generate the geoJson file
        var finalGeoJson = infoJson.generateGeoJson(passLng, passLat);
        // save this file in folder
        PrintWriter output2 = null;
        try {
            output2 = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            System.err.println("Fail to create the geoJson file.");
            e.printStackTrace();
        }
        output2.println(finalGeoJson);
        output2.close();
    }
}

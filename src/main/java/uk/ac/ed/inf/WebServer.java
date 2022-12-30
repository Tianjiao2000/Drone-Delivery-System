package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.ArrayList;

/**
 * Connect to web and derby server, load files.
 */
public class WebServer {

    // final HttpClient for server connection.
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    // private variables
    private String machineName;
    private String portServer;
    // final sql command to extract content from tables
    private final String ordersQueryByDate = "select * from orders where deliveryDate=(?)";
    private final String ordersQueryByNo = "select * from orders where orderNo=(?)";
    private final String orderDetailsQuery = "select * from orderDetails where orderNo=(?)";

    /**
     * WebServer constructor
     * @param name - IP for connection
     * @param port - port for connection
     */
    public WebServer(String name, String port) {
        machineName = name;
        portServer = port;
    }

    /**
     * Get URL combined with machine name and port.
     * @return URL with current machine name and port.
     */
    public String getServerURL() {
        return machineName + ":" + portServer;
    }

    /**
     * Connection to the server and access the desired file.
     * @param urlConn - URL for connection
     * @throws IOException If fail to connect to server, exits system
     * @throws InterruptedException If fail to connect to server, exits system
     * @return Content of the desired file
     */
    public String getResponse(String urlConn) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlConn))
                .build();
        HttpResponse<String> response = null;
        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            // if connection fail
            System.err.println("Fail to connect to " + machineName + " at port " + portServer + ".");
            e.printStackTrace();
            System.exit(1);
        }
        return response.body();
    }

    /**
     * Find all orders in provided date from corresponding table.
     * @param derbyURL - URL to connect to the derby database
     * @param orderDate - date of ordering placing
     * @throws SQLException Fail to execute the database.
     * @return ArrayList of orders placed in that date.
     */
    public ArrayList<String> getOrderNoByDate(String derbyURL, String orderDate) {
        Connection orderConn;
        ArrayList<String> orderList = new ArrayList<>();
        try {
            orderConn = DriverManager.getConnection(derbyURL);
            PreparedStatement psCourseQuery = orderConn.prepareStatement(ordersQueryByDate);
            psCourseQuery.setString(1, orderDate);
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                // get order number by date
                String orderNo = rs.getString("orderNo");
                orderList.add(orderNo);
            }
        } catch (SQLException e) {
            // if fail to execute the database
            System.err.println("Fail to execute the database.");
            e.printStackTrace();
        }
        return orderList;
    }

    /**
     * Find deliver to location in provided order number from corresponding table.
     * @param derbyURL - URL to connect to the derby database
     * @param orderNo - Order number
     * @throws SQLException Fail to execute the database.
     * @return String of deliver to location from given order.
     */
    public String getDeliverToByNo(String derbyURL, String orderNo) {
        Connection orderConn;
        String deliverTo = null;
        try {
            orderConn = DriverManager.getConnection(derbyURL);
            PreparedStatement psCourseQuery = orderConn.prepareStatement(ordersQueryByNo);
            psCourseQuery.setString(1, orderNo);
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                // find the deliver to location
                deliverTo = rs.getString("deliverTo");
            }
        } catch (SQLException e) {
            // if fail to execute the database
            System.err.println("Fail to execute the database.");
            e.printStackTrace();
        }
        return deliverTo;
    }

    /**
     * Find item to deliver in provided order number from corresponding table.
     * @param derbyURL - URL to connect to the derby database
     * @param orderNo - Order number
     * @throws SQLException Fail to execute the database.
     * @return ArrayList of item in given order.
     */
    public ArrayList<String> getItemByOrderNo(String derbyURL, String orderNo) {
        Connection orderDetConn;
        ArrayList<String> orderDetList = new ArrayList<>();
        try {
            orderDetConn = DriverManager.getConnection(derbyURL);
            PreparedStatement psCourseQuery = orderDetConn.prepareStatement(orderDetailsQuery);
            psCourseQuery.setString(1, orderNo);
            ResultSet rs = psCourseQuery.executeQuery();
            while (rs.next()) {
                // find all items contained in given order
                String item = rs.getString("item");
                orderDetList.add(item);
            }
        } catch (SQLException e) {
            // if fail to execute the database
            System.err.println("Fail to execute the database.");
            e.printStackTrace();
        }
        return orderDetList;
    }


}

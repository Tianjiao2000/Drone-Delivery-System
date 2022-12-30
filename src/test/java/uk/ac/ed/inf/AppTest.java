package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import org.junit.Test;

import java.awt.geom.Line2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

public class AppTest {

    private static final String VERSION = "1.0.5";
    private static final String RELEASE_DATE = "September 28, 2021";
    private boolean approxEq(double d1, double d2) {
        return Math.abs(d1 - d2) < 1e-12;
    }
    /**

    private final LongLat appletonTower = new LongLat(-3.186874, 55.944494);
    private final LongLat businessSchool = new LongLat(-3.1873,55.9430);
    private final LongLat greyfriarsKirkyard = new LongLat(-3.1928,55.9469);

    @Test
    public void testIsConfinedTrueA(){
        assertTrue(appletonTower.isConfined());
    }

    @Test
    public void testIsConfinedTrueB(){
        assertTrue(businessSchool.isConfined());
    }

    @Test
    public void testIsConfinedFalse(){
        assertFalse(greyfriarsKirkyard.isConfined());
    }



    @Test
    public void testDistanceTo(){
        double calculatedDistance = 0.0015535481968716011;
        assertTrue(approxEq(appletonTower.distanceTo(businessSchool), calculatedDistance));
    }

    @Test
    public void testCloseToTrue(){
        LongLat alsoAppletonTower = new LongLat(-3.186767933982822, 55.94460006601717);
        assertTrue(appletonTower.closeTo(alsoAppletonTower));
    }


    @Test
    public void testCloseToFalse(){
        assertFalse(appletonTower.closeTo(businessSchool));
    }


    private boolean approxEq(LongLat l1, LongLat l2) {
        return approxEq(l1.longitude, l2.longitude) &&
                approxEq(l1.latitude, l2.latitude);
    }

    @Test
    public void testAngle0(){
        LongLat nextPosition = appletonTower.nextPosition(0);
        LongLat calculatedPosition = new LongLat(-3.186724, 55.944494);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle20(){
        LongLat nextPosition = appletonTower.nextPosition(20);
        LongLat calculatedPosition = new LongLat(-3.186733046106882, 55.9445453030215);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle50(){
        LongLat nextPosition = appletonTower.nextPosition(50);
        LongLat calculatedPosition = new LongLat(-3.186777581858547, 55.94460890666647);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle90(){
        LongLat nextPosition = appletonTower.nextPosition(90);
        LongLat calculatedPosition = new LongLat(-3.186874, 55.944644);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle140(){
        LongLat nextPosition = appletonTower.nextPosition(140);
        LongLat calculatedPosition = new LongLat(-3.1869889066664676, 55.94459041814145);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle190(){
        LongLat nextPosition = appletonTower.nextPosition(190);
        LongLat calculatedPosition = new LongLat(-3.1870217211629517, 55.94446795277335);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle260(){
        LongLat nextPosition = appletonTower.nextPosition(260);
        LongLat calculatedPosition = new LongLat(-3.18690004722665, 55.944346278837045);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle300(){
        LongLat nextPosition = appletonTower.nextPosition(300);
        LongLat calculatedPosition = new LongLat(-3.186799, 55.94436409618943);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle350(){
        LongLat nextPosition = appletonTower.nextPosition(350);
        LongLat calculatedPosition = new LongLat(-3.1867262788370483, 55.94446795277335);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle999(){
        // The special junk value -999 means "hover and do not change position"
        LongLat nextPosition = appletonTower.nextPosition(-999);
        assertTrue(approxEq(nextPosition, appletonTower));
    }

    @Test
    public void testMenusOne() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 50, totalCost);
    }

    @Test
    public void testMenusTwo() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 230 + 50, totalCost);
    }

    @Test
    public void testMenusThree() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll",
                "Flaming tiger latte"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 230 + 460 + 50, totalCost);
    }

    @Test
    public void testMenusFourA() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll",
                "Flaming tiger latte",
                "Dirty matcha latte"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 230 + 460 + 460 + 50, totalCost);
    }

    @Test
    public void testMenusFourB() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("localhost", "9898");
        int totalCost = menus.getDeliveryCost(
                "Flaming tiger latte",
                "Dirty matcha latte",
                "Strawberry matcha latte",
                "Fresh taro latte"
        );
        // Don't forget the standard delivery charge of 50p
        assertEquals(4 * 460 + 50, totalCost);
    }**/
/////////////////////////
    /**@Test
    public void testWord() {
        WebServer webs = new WebServer("localhost", "9898");
        LoadInfo www = new LoadInfo(webs);
        www.loadWords("army", "monks", "grapes");
    }

    @Test
    public void testGeo() {
        WebServer webs = new WebServer("localhost", "9876");
        LoadInfo geo = new LoadInfo(webs);
        geo.getDetailsDerby("fab79441");
    }

    @Test
    public void testOrderDerby() {
        WebServer webs = new WebServer("localhost", "9876");
        LoadInfo geo = new LoadInfo(webs);
        System.out.println(geo.getOrderNo("2022-03-04"));
    }

    @Test
    public void testDetailDerby() {
        WebServer webs = new WebServer("localhost", "9898");
        LoadInfo geo = new LoadInfo(webs);
        //geo.loadMenus();
        //geo.loadOrderDetailsDerby();
    }

    @Test
    public void testDeliToLoc() {
        DeliveryDetails pl = new DeliveryDetails("localhost", "9876", "9898");
        //pl.orderPrice("fab79441");
    }

    @Test
    public void testLandmarkLoc() {
        DeliveryDetails pl = new DeliveryDetails("localhost", "9876", "9898");
        System.out.println(pl.getLandmarksLng());
        System.out.println(pl.getLandmarksLat());
    }

    //////////////DroneRoute
    @Test
    public void testRouteDet() {
        DeliveryDetails pl = new DeliveryDetails("localhost", "9876", "9898");
        ArrayList<Double> passLng = new ArrayList<>();
        passLng.add(-3.185236);
        passLng.add(-3.191257);
        ArrayList<Double> passLat = new ArrayList<>();
        passLat.add(55.944709);
        passLat.add(55.945626);
        //System.out.println(pl.bestRoute(-3.186874 , 55.944494, passLng, passLat));
        //System.out.println(pl.getClosestPoint());
    }

    @Test
    public void testBestRoute() {
        DroneRoute pl = new DroneRoute("localhost", "9876", "9898",
                -3.186874, 55.944494, "2022-03-04");
        pl.planRoute();
    }

    @Test
    public void testPlanRoute() {
        DroneRoute pl = new DroneRoute("localhost", "9876", "9898",
                -3.186874, 55.944494, "2022-12-11");
        ArrayList<Double> destination = new ArrayList<>();
        destination.add(-3.18933);
        destination.add(55.943389);
        ArrayList<Double> passLng = new ArrayList<>();
        passLng.add(-3.185236);
        passLng.add(-3.191257);
        ArrayList<Double> passLat = new ArrayList<>();
        passLat.add(55.944709);
        passLat.add(55.945626);
        //pl.bestRoute(-3.186874 , 55.944494, destination, passLng, passLat);
    }

    @Test
    public void cal() {
        double stos1 = Math.sqrt(Math.pow(-3.188367 - (-3.1882461), 2) + Math.pow(55.945356 - 55.9453946, 2));
        /**double s1tos1 = Math.sqrt(Math.pow(-3.186103 - (-3.186199), 2) + Math.pow(55.944656 - 55.945734, 2)) +
                Math.sqrt(Math.pow(-3.186199 - (-3.191065), 2) + Math.pow(55.945734 - 55.945626, 2));
        double s2tolm = Math.sqrt(Math.pow(-3.191065 - (-3.191594), 2) + Math.pow(55.945626 - 55.943658, 2)) +
                Math.sqrt(Math.pow(-3.191594 - (-3.188512), 2) + Math.pow(55.943658 - 55.944036, 2));
        //double shopToDes = Math.sqrt(Math.pow(-3.191065 - (-3.188512), 2) + Math.pow(55.945626 - 55.944036, 2));
        //System.out.println(shopToDes);
        //double atToShops = Math.sqrt(Math.pow(-3.186874 - (-3.186103), 2) + Math.pow(55.944494 - 55.944656, 2)) +
          //      Math.sqrt(Math.pow(-3.186103 - (-3.191065), 2) + Math.pow(55.944656 - 55.945626, 2));
        //System.out.println(atToShops);
        //System.out.println(stos1+s1tos1+s2tolm);//0.011880251979994691
        System.out.println(stos1<0.00015);

    }

    @Test
    public void c() {
        ArrayList<Double> a = new ArrayList<>();
        a.add(3.3);
        a.add(6.3);
        a.add(5.3);
        a.add(5.3);
        a.add(4.3);
        a.add(8.3);
        a.remove(5.3);
        System.out.println(a);
    }


    @Test
    public void drawGeo() throws FileNotFoundException {
        DroneRoute pl = new DroneRoute("localhost", "9876", "9898",
                -3.186874, 55.944494, "2023-12-11");
        ArrayList<String> sortedOrder = pl.planRoute();
        pl.droneDelivery(sortedOrder);
        pl.moveStep();
        //ArrayList<Double> passLat = pl.getPassLat();
        //ArrayList<Double> passLng = pl.getPassLng();
        ArrayList<Double> passLat = pl.getMoveLat();
        ArrayList<Double> passLng = pl.getMoveLng();

        WebServer webs = new WebServer("localhost", "9898");
        LoadInfo infoGeo = new LoadInfo(webs);
        var finalFC = infoGeo.generateGeoJson(passLng, passLat);

        String mapGeoJsonReadingsTextFile = "readings20220101" + ".geojson";
        PrintWriter output2 = new PrintWriter(mapGeoJsonReadingsTextFile);
        output2.println(finalFC);
        output2.close();
        System.out.println(pl.getMoveAngle().size());
    }

    @Test
    public void writeFile() {
        DroneRoute dr = new DroneRoute("localhost", "9876", "9898",
                -3.186874, 55.944494, "2022-02-01");
        DeliveryDetails dd = new DeliveryDetails("localhost", "9876", "9898");
        SaveFlight sf = new SaveFlight(dr);
        dr.planRoute();
        //sf.createDeliveries("jdbc:derby://localhost:9876/derbyDB");
        sf.writeDeliveries("jdbc:derby://localhost:9876/derbyDB");
    }

    @Test
    public void writePath() {
        DroneRoute dr = new DroneRoute("localhost", "9876", "9898",
                -3.186874, 55.944494, "2022-01-03");
        DeliveryDetails dd = new DeliveryDetails("localhost", "9876", "9898");
        SaveFlight sf = new SaveFlight(dr);
        dr.droneDelivery(dr.planRoute());
        dr.moveStep();
        //sf.createFlightPath("jdbc:derby://localhost:9876/derbyDB");
        sf.writeFlightPath("jdbc:derby://localhost:9876/derbyDB");
    }

    @Test
    public void testRead() {
        DroneRoute dr = new DroneRoute("localhost", "9876", "9898",
                -3.186874, 55.944494, "2022-01-03");
        DeliveryDetails dd = new DeliveryDetails("localhost", "9876", "9898");
        SaveFlight sf = new SaveFlight(dr);
        sf.testPath("jdbc:derby://localhost:9876/derbyDB", "f8cf9009");
    }

    @Test
    public void testAngle() {
        //var angle = Math.atan2((55.944494-55.944656), (-3.186874-(-3.186103))); //弧度  0.6435011087932844 //168
        var angle = Math.atan2((55.9440 - 55.944494), (-3.1885 - (-3.186874)));

        var theta = angle*(180/Math.PI);
        if (theta < 0) {
            theta = 360 + theta;
        }
        theta = 135;
        if (theta % 10 < 5) {
            theta = Math.floor(theta / 10) * 10;
        } else {
            theta = Math.floor(theta / 10) * 10 + 10;
        }
        System.out.println(theta);
    }

    @Test
    public void testNoFly() {
        //DeliveryDetails pl = new DeliveryDetails("localhost", "9876", "9898");
        DroneRoute pl = new DroneRoute("localhost", "9876", "9898", -3.186874, 55.944494, "2022-12-11");
        // shop to the first lm
        //System.out.println(pl.validMove(55.9456, -3.1911, 	55.9457, 	-3.1862));
        // lm to deliver
        //System.out.println(pl.validMove(	55.9457, 	-3.1862, 	55.9454, 	-3.1884));
        //System.out.println(pl.getClosestLandmark(-3.1911, 55.9456, -3.1884, 55.9454));
        //pl.addLandmark(	-3.191065, 55.945626, 	-3.188367, 	55.945356);

        //f5c7f6bb
        ArrayList<String> sortedOrder = new ArrayList<>();
        sortedOrder.add("f5c7f6bb");
        pl.droneDelivery(sortedOrder);
        //DeliveryDetails pl = new DeliveryDetails("localhost", "9876", "9898");

    }

    @Test
    public void testMain() {
        String[] aaa = {"31", "12", "2022", "9898", "9876"};
        App.main(aaa);
    }**/
}
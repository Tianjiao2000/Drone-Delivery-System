package uk.ac.ed.inf;

import java.util.ArrayList;

/**
 * Json structure for defining menu information.
 */
public class MenusDetails {
    String name;
    String location;
    ArrayList<ItemsDetails> menu;

    /**
     * Class response for defining structure in ArrayList menu.
     */
    public class ItemsDetails {
        String item;
        int pence;
    }

}

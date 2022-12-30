package uk.ac.ed.inf;

/**
 * Json structure for defining WhatThreeWords location details.
 */
public class WordsLocation {
    String country;
    Square square;

    /**
     * Class response for defining structure in Square.
     */
    public class Square {
        Southwest southwest;
        /**
         * Class response for defining structure in southwest.
         */
        public class Southwest {
            double lng;
            double lat;
        }
        Northeast northeast;
        /**
         * Class response for defining structure in northwest.
         */
        public class Northeast {
            double lng;
            double lat;
        }
    }

    String nearestPlace;
    Coordinate coordinates;

    /**
     * Class response for defining structure in coordinates.
     */
    public class Coordinate {
        double lng;
        double lat;
    }

    String words;
    String language;
    String map;
}

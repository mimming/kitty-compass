package com.google.glassware.model;

/**
 * This class represents a point of interest that has geographical coordinates (latitude and
 * longitude) and a name that is displayed to the user.
 */
public class Place {

  private final double mLatitude;
  private final double mLongitude;
  private final String mName;

  /**
   * Initializes a new place with the specified coordinates and name.
   *
   * @param latitude  the latitude of the place
   * @param longitude the longitude of the place
   * @param name      the name of the place
   */
  public Place(double latitude, double longitude, String name) {
    mLatitude = latitude;
    mLongitude = longitude;
    mName = name;
  }

  /**
   * Gets the latitude of the place.
   *
   * @return the latitude of the place
   */
  public double getLatitude() {
    return mLatitude;
  }

  /**
   * Gets the longitude of the place.
   *
   * @return the longitude of the place
   */
  public double getLongitude() {
    return mLongitude;
  }

  /**
   * Gets the name of the place.
   *
   * @return the name of the place
   */
  public String getName() {
    return mName;
  }
}

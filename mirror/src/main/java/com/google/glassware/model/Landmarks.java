package com.google.glassware.model;

import com.google.glassware.util.MathUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class provides access to a list of hard-coded landmarks (located in
 * {@code res/raw/landmarks.json}) that will appear on the compass when the user is near them.
 */
public class Landmarks {

  private static final Logger LOG = Logger.getLogger(Landmarks.class.getSimpleName());

  /**
   * The threshold used to display a landmark on the compass.
   */
  private static final double MAX_DISTANCE_KM = 100;

  /**
   * The list of landmarks loaded from resources.
   */
  private final ArrayList<Place> mPlaces;

  /**
   * Initializes a new {@code Landmarks} object by loading the landmarks from the resource
   * bundle.
   */
  public Landmarks() {
    mPlaces = new ArrayList<Place>();

    // This class will be instantiated on the service's main thread, and doing I/O on the
    // main thread can be dangerous if it will block for a noticeable amount of time. In
    // this case, we assume that the landmark data will be small enough that there is not
    // a significant penalty to the application. If the landmark data were much larger,
    // we may want to load it in the background instead.
    String jsonString = readLandmarksResource();
    populatePlaceList(jsonString);
  }

  /**
   * Gets a list of landmarks that are within ten kilometers of the specified coordinates. This
   * function will never return null; if there are no locations within that threshold, then an
   * empty list will be returned.
   */
  public List<Place> getNearbyLandmarks(double latitude, double longitude) {
    ArrayList<Place> nearbyPlaces = new ArrayList<Place>();

    for (Place knownPlace : mPlaces) {
      if (MathUtils.getDistance(latitude, longitude,
          knownPlace.getLatitude(), knownPlace.getLongitude()) <= MAX_DISTANCE_KM) {
        nearbyPlaces.add(knownPlace);
      }
    }

    return nearbyPlaces;
  }

  /**
   * Populates the internal places list from places found in a JSON string. This string should
   * contain a root object with a "landmarks" property that is an array of objects that represent
   * places. A place has three properties: name, latitude, and longitude.
   */
  private void populatePlaceList(String jsonString) {
    try {
      JSONObject json = new JSONObject(jsonString);
      JSONArray array = json.optJSONArray("landmarks");

      if (array != null) {
        for (int i = 0; i < array.length(); i++) {
          JSONObject object = array.optJSONObject(i);
          Place place = jsonObjectToPlace(object);
          if (place != null) {
            mPlaces.add(place);
          }
        }
      }
    } catch (JSONException e) {
      LOG.log(Level.SEVERE, "Could not parse landmarks JSON string", e);
    }
  }

  /**
   * Converts a JSON object that represents a place into a {@link Place} object.
   */
  private Place jsonObjectToPlace(JSONObject object) {
    String name = object.optString("name");
    double latitude = object.optDouble("latitude", Double.NaN);
    double longitude = object.optDouble("longitude", Double.NaN);

    if (!name.isEmpty() && !Double.isNaN(latitude) && !Double.isNaN(longitude)) {
      return new Place(latitude, longitude, name);
    } else {
      return null;
    }
  }

  /**
   * Reads the text from {@code res/raw/landmarks.json} and returns it as a string.
   */
  private static String readLandmarksResource() {
    InputStream is = null;
    try {
      is = new FileInputStream(new File("./src/main/resources/landmarks.json"));
      StringBuffer buffer = new StringBuffer();

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line);
        buffer.append('\n');
      }

      return buffer.toString();

    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Could not read landmarks resource", e);
      return null;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          LOG.log(Level.SEVERE, "Could not close landmarks resource stream", e);
        }
      }
    }
  }
}

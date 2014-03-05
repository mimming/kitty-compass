/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.glass.sample.kittycompass.model;

import com.google.android.glass.sample.kittycompass.R;
import com.google.android.glass.sample.kittycompass.util.MathUtils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides access to a list of hard-coded landmarks (located in
 * {@code res/raw/landmarks.json}) that will appear on the compass when the user is near them.
 */
public class Landmarks {

    private static final String TAG = Landmarks.class.getSimpleName();

    /**
     * The threshold used to display a landmark on the compass.
     */
    private static final double MAX_DISTANCE_KM = 100;

    // TODO: If you use this in production, point this to your live server
    public static final String LANDMARKS_URL = "https://mimming.com/landmarks.json";

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
            Log.e(TAG, "Could not parse landmarks JSON string", e);
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
        URL landmarksUrl;
        try {
            landmarksUrl = new URL(LANDMARKS_URL);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Landmarks URL is malformed", e);
            return null;
        }
        InputStream is;
        try {
            is = landmarksUrl.openStream();
        } catch (IOException e) {
            Log.e(TAG, "Could not fetch landmarks", e);
            return null;
        }
        StringBuffer buffer = new StringBuffer();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append('\n');
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not read landmarks resource", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close landmarks resource stream", e);
                }
            }
        }

        return buffer.toString();
    }
}

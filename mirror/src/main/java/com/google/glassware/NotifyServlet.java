/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.glassware;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.*;
import com.google.common.collect.Lists;
import com.google.glassware.model.Landmarks;
import com.google.glassware.model.Place;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles the notifications sent back from subscriptions
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class NotifyServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(NotifyServlet.class.getSimpleName());


  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Respond with OK and status 200 in a timely fashion to prevent redelivery
    response.setContentType("text/html");
    Writer writer = response.getWriter();
    writer.append("OK");
    writer.close();

    // Get the notification object from the request body (into a string so we
    // can log it)
    BufferedReader notificationReader =
        new BufferedReader(new InputStreamReader(request.getInputStream()));
    String notificationString = "";

    // Count the lines as a very basic way to prevent Denial of Service attacks
    int lines = 0;
    while (notificationReader.ready()) {
      notificationString += notificationReader.readLine();
      lines++;

      // No notification would ever be this long. Something is very wrong.
      if (lines > 1000) {
        throw new IOException("Attempted to parse notification payload that was unexpectedly long.");
      }
    }

    LOG.info("got raw notification " + notificationString);

    JsonFactory jsonFactory = new JacksonFactory();

    // If logging the payload is not as important, use
    // jacksonFactory.fromInputStream instead.
    Notification notification = jsonFactory.fromString(notificationString, Notification.class);

    LOG.info("Got a notification with ID: " + notification.getItemId());

    // Figure out the impacted user and get their credentials for API calls
    String userId = notification.getUserToken();
    Credential credential = AuthUtil.getCredential(userId);
    Mirror mirrorClient = MirrorClient.getMirror(credential);

    if (notification.getCollection().equals("locations")) {
      LOG.info("Notification of updated location");
      Mirror glass = MirrorClient.getMirror(credential);
      // item id is usually 'latest'
      Location location = glass.locations().get(notification.getItemId()).execute();

      LOG.info("New location is " + location.getLatitude() + ", " + location.getLongitude());

      Landmarks landmarks = new Landmarks();
      List<Place> nearbyPlaces =
          landmarks.getNearbyLandmarks(location.getLatitude(), location.getLongitude());
      if (nearbyPlaces.size() > 0) {
        LOG.info("Found " + nearbyPlaces.size() + " places");

        // Pick an arbitrary place
        Place arbitraryNearbyPlace = nearbyPlaces.get(0);

        // If you're doing this for real, don't re-send a notification you've already sent

        MirrorClient.insertTimelineItem(
            credential,
            new TimelineItem()
                .setText("Meow! Did you know you are close to " + arbitraryNearbyPlace.getName())
                .setNotification(new NotificationConfig().setLevel("DEFAULT"))
                .setLocation(new Location()
                    .setLatitude(arbitraryNearbyPlace.getLatitude())
                    .setLongitude(arbitraryNearbyPlace.getLongitude()))
                .setMenuItems(Lists.newArrayList(
                    new MenuItem()
                        .setAction("OPEN_URI")
                        .setPayload("kittycompass://open")
                        .setValues(
                            Lists.newArrayList(new MenuValue()
                                .setDisplayName("Open")
                                .setIconUrl(WebUtil.buildUrl(request, "/static/images/ic_compass.png")))
                        )
                ))
        );
      } else {
        LOG.info("Got a location ping, but not near anything interesting");
      }

    } else {
      LOG.warning("I don't know how to handle this notification, so ignoring " + notificationString);
    }
  }

  /**
   * Wraps some HTML content in article/section tags and adds a footer
   * identifying the card as originating from the Java Quick Start.
   *
   * @param content the HTML content to wrap
   * @return the wrapped HTML content
   */
  private static String makeHtmlForCard(String content) {
    return "<article class='auto-paginate'>" + content
        + "<footer><p>Java Quick Start</p></footer></article>";
  }
}

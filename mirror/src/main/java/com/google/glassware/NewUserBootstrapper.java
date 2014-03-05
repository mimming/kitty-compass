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
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.mirror.model.*;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility functions used when users first authenticate with this service
 *
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class NewUserBootstrapper {
  private static final Logger LOG = Logger.getLogger(NewUserBootstrapper.class.getSimpleName());

  /**
   * Bootstrap a new user. Do all of the typical actions for a new user:
   * <ul>
   * <li>Creating a timeline subscription</li>
   * <li>Inserting a contact</li>
   * <li>Sending the user a welcome message</li>
   * </ul>
   */
  public static void bootstrapNewUser(HttpServletRequest req, String userId) throws IOException {
    Credential credential = AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);

    try {
      // Subscribe to locations updates
      Subscription subscription =
          MirrorClient.insertSubscription(credential, WebUtil.buildUrl(req, "/notify"), userId,
              "locations");
      LOG.info("Bootstrapper inserted subscription " + subscription
          .getId() + " for user " + userId);
    } catch (GoogleJsonResponseException e) {
      LOG.warning("Failed to create locations subscription. Might be running on "
          + "localhost. Details:" + e.getDetails().toPrettyString());
    }

    // Send welcome timeline item
    TimelineItem timelineItem = new TimelineItem();
    timelineItem.setText("Welcome to Kitty Compass");
    timelineItem.setNotification(new NotificationConfig().setLevel("DEFAULT"));
    final HttpServletRequest finalReq = req;
    timelineItem.setMenuItems(new ArrayList<MenuItem>(){{
      this.add(new MenuItem()
          .setAction("OPEN_URI")
          .setPayload("kittycompass://open")
          .setValues(new ArrayList<MenuValue>(){{
            this.add(new MenuValue()
                .setDisplayName("Open")
                .setIconUrl(WebUtil.buildUrl(finalReq, "/static/images/ic_compass.png")));
          }}));
    }});


    TimelineItem insertedItem = MirrorClient.insertTimelineItem(credential, timelineItem);
    LOG.info("Bootstrapper inserted welcome message " + insertedItem.getId() + " for user "
        + userId);
  }
}

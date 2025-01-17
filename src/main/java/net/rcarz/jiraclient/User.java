/**
 * jira-client - a simple JIRA REST client
 * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.rcarz.jiraclient;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * Represents a JIRA user.
 */
public class User extends Resource {

    private boolean active = false;
    private Map<String, String> avatarUrls = null;
    private String displayName = null;
    private String email = null;
    private String name = null;

    /**
     * Creates a user from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json       JSON payload
     */
    protected User(RestClient restclient, JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    /**
     * Retrieves the given user record.
     *
     * @param restclient REST client instance
     * @param username   User logon name
     * @return a user instance
     * @throws JiraException when the retrieval fails
     */
    public static User get(RestClient restclient, String username)
            throws JiraException {

        JSONObject result = null;

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);

        try {
            result = restclient.getMap(getBaseUri() + "user", params);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve user " + username, ex);
        }

        if (result == null)
            throw new JiraException("JSON payload is malformed");

        return new User(restclient, (JSONObject) result);
    }

    private void deserialise(JSONObject json) {
        self = Field.getString(json.opt("self"));
        id = Field.getString(json.opt("id"));
        active = Field.getBoolean(json.opt("active"));
        avatarUrls = Field.getMap(String.class, String.class, json.opt("avatarUrls"));
        displayName = Field.getString(json.opt("displayName"));
        email = getEmailFromMap(json);
        name = Field.getString(json.opt("name"));
    }

    /**
     * API changes email address might be represented as either "email" or "emailAddress"
     *
     * @param map JSON object for the User
     * @return String email address of the JIRA user.
     */
    private String getEmailFromMap(JSONObject map) {
        if (map.has("email")) {
            return Field.getString(map.get("email"));
        } else {
            return Field.getString(map.opt("emailAddress"));
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isActive() {
        return active;
    }

    public Map<String, String> getAvatarUrls() {
        return avatarUrls;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}


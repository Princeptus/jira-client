/**
 * jira-client - a simple JIRA REST client
 * Copyright (c) 2013 Bob Carroll (bob.carroll@alum.rit.edu)
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.rcarz.jiraclient.agile;

import org.json.JSONObject;

import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;

/**
 * Represents an Agile User.
 *
 * @author pldupont
 */
public class User extends AgileResource {

    private String emailAddress;
    private String displayName;
    private boolean active;
    private String timeZone;

    /**
     * Creates a new Agile resource.
     *
     * @param restclient REST client instance
     * @param json       JSON payload
     */
    public User(RestClient restclient, JSONObject json) throws JiraException {
        super(restclient, json);
    }

    /**
     * Deserialize the json to extract standard attributes and keep a reference of
     * other attributes.
     *
     * @param json The JSON object to read.
     */
    @Override
    void deserialize(JSONObject json) throws JiraException {
        super.deserialize(json);
        this.emailAddress = Field.getString(json.opt("emailAddress"));
        this.displayName = Field.getString(json.opt("displayName"));
        this.active = Field.getBoolean(json.opt("active"));
        this.timeZone = Field.getString(json.opt("timeZone"));
    }

    @Override
    public String toString() {
        return String.format("%s{name='%s', Display Name='%s'}", getClass().getSimpleName(), getName(), getDisplayName());
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }

    public String getTimeZone() {
        return timeZone;
    }
}

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

import org.json.JSONObject;

/**
 * Represents an issue priority.
 */
public class Transition extends Resource {

    private String name = null;
    private Status toStatus = null;
    private JSONObject fields = null;

    /**
     * Creates a priority from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Transition(RestClient restclient, JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        self = Field.getString(json.opt("self"));
        id = Field.getString(json.opt("id"));
        name = Field.getString(json.opt("name"));
        toStatus = Field.getResource(Status.class, json.opt(Field.TRANSITION_TO_STATUS), restclient);

        fields = (JSONObject) json.opt("fields");
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public Status getToStatus() {
        return toStatus;
    }

    public JSONObject getFields() {
        return fields;
    }

}


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

package net.rcarz.jiraclient.greenhopper;

import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.RestClient;

import org.json.JSONObject;

/**
 * Represents a GreenHopper marker (a sprint that hasn't started).
 */
public class Marker extends GreenHopperResource {

    private String name = null;

    /**
     * Creates a marker from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Marker(RestClient restclient, JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        id = Field.getInteger(json.opt("id"));
        name = Field.getString(json.opt("name"));
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }
}


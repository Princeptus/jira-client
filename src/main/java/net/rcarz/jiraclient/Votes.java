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
 * Represents issue votes.
 */
public class Votes extends Resource {

    private int votes = 0;
    private boolean hasVoted = false;

    /**
     * Creates votes from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Votes(RestClient restclient, JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        self = Field.getString(json.opt("self"));
        id = Field.getString(json.opt("id"));
        votes = Field.getInteger(json.opt("votes"));
        hasVoted = Field.getBoolean(json.opt("hasVoted"));
    }

    /**
     * Retrieves the given votes record.
     *
     * @param restclient REST client instance
     * @param issue Internal JIRA ID of the issue
     *
     * @return a votes instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Votes get(RestClient restclient, String issue)
        throws JiraException {

        JSONObject result = null;

        try {
            result = restclient.getMap(getBaseUri() + "issue/" + issue + "/votes");
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve votes for issue " + issue, ex);
        }

        if (result == null)
            throw new JiraException("JSON payload is malformed");
        return new Votes(restclient, result);
    }

    @Override
    public String toString() {
        return Integer.toString(getVotes());
    }

    public int getVotes() {
        return votes;
    }

    public boolean hasVoted() {
        return hasVoted;
    }
}


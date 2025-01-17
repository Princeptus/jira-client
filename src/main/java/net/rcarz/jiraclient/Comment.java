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

import java.util.Date;
import org.json.JSONObject;

/**
 * Represents an issue comment.
 */
public class Comment extends Resource {

    private String issueKey = null;
    private User author = null;
    private String body = null;
    private Date created = null;
    private Date updated = null;
    private User updatedAuthor = null;

    public Visibility getVisibility() {
        return visibility;
    }

    private Visibility visibility = null;

    /**
     * Creates a comment from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Comment(RestClient restclient, JSONObject json, String issueKey) {
        super(restclient);

        this.issueKey = issueKey;
        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {

        self = Field.getString(json.opt("self"));
        id = Field.getString(json.opt("id"));
        author = Field.getResource(User.class, json.opt("author"), restclient);
        body = Field.getString(json.opt("body"));
        created = Field.getDateTime(json.opt("created"));
        updated = Field.getDateTime(json.opt("updated"));
        updatedAuthor = Field.getResource(User.class, json.opt("updatedAuthor"), restclient);
        visibility = Field.getResource(Visibility.class, json.opt("visibility"), restclient);
    }

    /**
     * Retrieves the given comment record.
     *
     * @param restclient REST client instance
     * @param issue Internal JIRA ID of the associated issue
     * @param id Internal JIRA ID of the comment
     *
     * @return a comment instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Comment get(RestClient restclient, String issue, String id)
        throws JiraException {

        JSONObject result = null;

        try {
            result = restclient.getMap(getBaseUri() + "issue/" + issue + "/comment/" + id);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve comment " + id + " on issue " + issue, ex);
        }

        if (result == null)
            throw new JiraException("JSON payload is malformed");

        return new Comment(restclient, result, issue);
    }

    /**
     * Updates the comment body.
     *
     * @param issue associated issue record
     * @param body Comment text
     *
     * @throws JiraException when the comment update fails
     */
    public void update(String body) throws JiraException {
        update(body, null, null);
    }

    /**
     * Updates the comment body with limited visibility.
     *
     * @param issue associated issue record
     * @param body Comment text
     * @param visType Target audience type (role or group)
     * @param visName Name of the role or group to limit visibility to
     *
     * @throws JiraException when the comment update fails
     */
    public void update(String body, String visType, String visName)
        throws JiraException {

        JSONObject req = new JSONObject();
        req.put("body", body);

        if (visType != null && visName != null) {
            JSONObject vis = new JSONObject();
            vis.put("type", visType);
            vis.put("value", visName);

            req.put("visibility", vis);
        }

        JSONObject result = null;

        try {
            String issueUri = getBaseUri() + "issue/" + issueKey;
            result = restclient.put(issueUri + "/comment/" + id, req);
        } catch (Exception ex) {
            throw new JiraException("Failed add update comment " + id, ex);
        }

        if (result == null) {
            throw new JiraException("JSON payload is malformed");
        }

        deserialise(result);
    }

    @Override
    public String toString() {
        return created + " by " + author;
    }

    public User getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public Date getCreatedDate() {
        return created;
    }

    public User getUpdateAuthor() {
        return updatedAuthor;
    }

    public Date getUpdatedDate() {
        return updated;
    }
}


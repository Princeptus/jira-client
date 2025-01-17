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
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * A base class for GreenHopper issues.
 */
public abstract class GreenHopperIssue extends GreenHopperResource {

    private String key = null;
    private boolean hidden = false;
    private String summary = null;
    private String typeName = null;
    private String typeId = null;
    private String typeUrl = null;
    private String priorityUrl = null;
    private String priorityName = null;
    private boolean done = false;
    private String assignee = null;
    private String assigneeName = null;
    private String avatarUrl = null;
    private String colour = null;
    private String statusId = null;
    private String statusName = null;
    private String statusUrl = null;
    private List<Integer> fixVersions = null;
    private int projectId = 0;

    /**
     * Creates an issue from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected GreenHopperIssue(RestClient restclient, JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        id = Field.getInteger(json.opt("id"));
        key = Field.getString(json.opt("key"));
        hidden = Field.getBoolean(json.opt("hidden"));
        summary = Field.getString(json.opt("summary"));
        typeName = Field.getString(json.opt("key"));
        typeId = Field.getString(json.opt("typeId"));
        typeUrl = Field.getString(json.opt("typeUrl"));
        priorityUrl = Field.getString(json.opt("priorityUrl"));
        priorityName = Field.getString(json.opt("priorityName"));
        done = Field.getBoolean(json.opt("done"));
        assignee = Field.getString(json.opt("assignee"));
        assigneeName = Field.getString(json.opt("assigneeName"));
        avatarUrl = Field.getString(json.opt("avatarUrl"));
        colour = Field.getString(json.opt("color"));
        statusId = Field.getString(json.opt("statusId"));
        statusName = Field.getString(json.opt("statusName"));
        statusUrl = Field.getString(json.opt("statusUrl"));
        fixVersions = GreenHopperField.getIntegerArray(json.opt("fixVersions"));
        projectId = Field.getInteger(json.opt("projectId"));
    }

    /**
     * Retrieves the full JIRA issue.
     *
     * @return an Issue
     *
     * @throws JiraException when the retrieval fails
     */
    public Issue getJiraIssue() throws JiraException {
        return Issue.get(restclient, key);
    }

    @Override
    public String toString() {
        return key;
    }

    public String getKey() {
        return key;
    }

    public Boolean isHidden() {
        return hidden;
    }

    public String getSummary() {
        return summary;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getTypeUrl() {
        return typeUrl;
    }

    public String getPriorityUrl() {
        return priorityUrl;
    }

    public String getPriorityName() {
        return priorityName;
    }

    public Boolean isDone() {
        return done;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getColour() {
        return colour;
    }

    public String getStatusId() {
        return statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public String getStatusUrl() {
        return statusUrl;
    }

    public List<Integer> getFixVersions() {
        return fixVersions;
    }

    public int getProjectId() {
        return projectId;
    }
}


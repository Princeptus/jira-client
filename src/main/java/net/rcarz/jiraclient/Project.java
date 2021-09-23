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

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents a JIRA project.
 */
public class Project extends Resource {

    private Map<String, String> avatarUrls = null;
    private String key = null;
    private String name = null;
    private String description = null;
    private User lead = null;
    private String assigneeType = null;
    private List<Component> components = null;
    private List<IssueType> issueTypes = null;
    private List<Version> versions = null;
    private Map<String, String> roles = null;
    private ProjectCategory category = null;
    private String email = null;

    /**
     * Creates a project from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Project(RestClient restclient, JSONObject json) {
        super(restclient);

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        self = Field.getString(json.opt("self"));
        id = Field.getString(json.opt("id"));
        avatarUrls = Field.getMap(String.class, String.class, json.opt("avatarUrls"));
        key = Field.getString(json.opt("key"));
        name = Field.getString(json.opt("name"));
        description = Field.getString(json.opt("description"));
        lead = Field.getResource(User.class, json.opt("lead"), restclient);
        assigneeType = Field.getString(json.opt("assigneeType"));
        components = Field.getResourceArray(Component.class, json.opt("components"), restclient);
        issueTypes = Field.getResourceArray(
            IssueType.class,
            json.has("issueTypes") ? json.opt("issueTypes") : json.opt("issuetypes"),
            restclient);
        versions = Field.getResourceArray(Version.class, json.opt("versions"), restclient);
        roles = Field.getMap(String.class, String.class, json.opt("roles"));
        category = Field.getResource(ProjectCategory.class, json.opt( "projectCategory" ), restclient);
        email = Field.getString( json.opt("email"));
    }

    /**
     * Retrieves the given project record.
     *
     * @param restclient REST client instance
     * @param key Project key
     *
     * @return a project instance
     *
     * @throws JiraException when the retrieval fails
     */
    public static Project get(RestClient restclient, String key)
        throws JiraException {

        JSONObject result = null;

        try {
            result = restclient.getMap(getBaseUri() + "project/" + key);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve project " + key, ex);
        }

        if (result == null)
            throw new JiraException("JSON payload is malformed");

        return new Project(restclient, result);
    }

    /**
     * Retrieves all project records visible to the session user.
     *
     * @param restclient REST client instance
     *
     * @return a list of projects
     *
     * @throws JiraException when the retrieval fails
     */
    public static List<Project> getAll(RestClient restclient) throws JiraException {
        JSONArray result = null;

        try {
            result = restclient.getArray(getBaseUri() + "project");
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve projects", ex);
        }

        if (result == null)
            throw new JiraException("JSON payload is malformed");

        return Field.getResourceArray(Project.class, result, restclient);
    }

    public List<User> getAssignableUsers() throws JiraException {
    	JSONArray result = null;

        try {			
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("project", this.key);
            URI searchUri = restclient.buildURI(getBaseUri() + "user/assignable/search", queryParams);
            result = restclient.getArray(searchUri);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve assignable users", ex);
        }

        if (result == null)
            throw new JiraException("JSON payload is malformed");

        return Field.getResourceArray(User.class, result, restclient);
    }

    @Override
    public String toString() {
        return getName();
    }

    public Map<String, String> getAvatarUrls() {
        return avatarUrls;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public User getLead() {
        return lead;
    }

    public String getAssigneeType() {
        return assigneeType;
    }

    public List<Component> getComponents() {
        return components;
    }

    public List<IssueType> getIssueTypes() {
        return issueTypes;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public ProjectCategory getCategory() {
        return category;
    }

    public String getEmail() {
        return email;
    }
}


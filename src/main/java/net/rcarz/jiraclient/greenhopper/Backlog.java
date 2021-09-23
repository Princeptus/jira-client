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
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * GreenHopper backlog data.
 */
public class Backlog {

    private RestClient restclient = null;
    private List<SprintIssue> issues = null;
    private List<SprintIssue> backlogIssues = null;
    private int rankCustomFieldId = 0;
    private List<Sprint> sprints = null;
    private List<RapidViewProject> projects = null;
    private List<Marker> markers = null;
    private List<Epic> epics = null;
    private boolean canEditEpics = false;
    private boolean canManageSprints = false;
    private boolean maxIssuesExceeded = false;
    private int queryResultLimit = 0;
    private Map<String, List<RapidViewVersion>> versionsPerProject = null;

    /**
     * Creates the backlog from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected Backlog(RestClient restclient, JSONObject json) {
        this.restclient = restclient;

        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        issues = GreenHopperField.getResourceArray(
            SprintIssue.class,
            json.opt("issues"),
            restclient);
        rankCustomFieldId = Field.getInteger(json.opt("rankCustomFieldId"));
        sprints = GreenHopperField.getResourceArray(
            Sprint.class,
            json.opt("sprints"),
            restclient);
        projects = GreenHopperField.getResourceArray(
            RapidViewProject.class,
            json.opt("projects"),
            restclient);
        markers = GreenHopperField.getResourceArray(
            Marker.class,
            json.opt("markers"),
            restclient);
        canManageSprints = Field.getBoolean(json.opt("canManageSprints"));
        maxIssuesExceeded = Field.getBoolean(json.opt("maxIssuesExceeded"));
        queryResultLimit = Field.getInteger(json.opt("queryResultLimit"));

        if (json.has("epicData") && json.get("epicData") instanceof JSONObject) {
            JSONObject epicData = (JSONObject) json.get("epicData");

            epics = GreenHopperField.getResourceArray(Epic.class, epicData.opt("epics"), restclient);
            canEditEpics = Field.getBoolean(epicData.opt("canEditEpics"));
        }

        if (json.has("versionData") && json.get("versionData") instanceof JSONObject) {
            JSONObject verData = (JSONObject)json.get("versionData");

            if (verData.has("versionsPerProject") &&
                verData.get("versionsPerProject") instanceof JSONObject) {

                JSONObject verMap = (JSONObject)verData.get("versionsPerProject");
                versionsPerProject = new HashMap<String, List<RapidViewVersion>>();

                for (String key : verMap.keySet()) {
                    Object value = verMap.get(key);

                    if (!(value instanceof JSONArray))
                        continue;

                    List<RapidViewVersion> versions = new ArrayList<RapidViewVersion>();

                    for (Object item : (JSONArray) value) {
                        if (!(item instanceof JSONObject))
                            continue;

                        RapidViewVersion ver = new RapidViewVersion(restclient, (JSONObject) item);
                        versions.add(ver);
                    }

                    versionsPerProject.put(key, versions);
                }
            }
        }

        //determining which issues are actually in the backlog vs the sprints
        //fill in the issues into the single sprints and the backlog issue list respectively
        for(SprintIssue issue : issues){
            boolean addedToSprint = false;
            for(Sprint sprint : sprints){
                if(sprint.getIssuesIds().contains(issue.getId())){
                    sprint.getIssues().add(issue);
                    addedToSprint = true;
                }
            }
            if(!addedToSprint){
                if(backlogIssues == null){
                    backlogIssues = new ArrayList<SprintIssue>();
                }
                backlogIssues.add(issue);
            }
        }

    }

    /**
     * Retrieves the backlog data for the given rapid view.
     *
     * @param restclient REST client instance
     * @param rv Rapid View instance
     *
     * @return the backlog
     *
     * @throws JiraException when the retrieval fails
     */
    public static Backlog get(RestClient restclient, RapidView rv)
        throws JiraException {

        final int rvId = rv.getId();
        JSONObject result = null;

        try {
            URI reporturi = restclient.buildURI(
                GreenHopperResource.RESOURCE_URI + "xboard/plan/backlog/data",
                new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;

                {
                    put("rapidViewId", Integer.toString(rvId));
                }});
            result = restclient.getMap(reporturi);
        } catch (Exception ex) {
            throw new JiraException("Failed to retrieve backlog data", ex);
        }

        return new Backlog(restclient, result);
    }

    public List<SprintIssue> getIssues() {
        return issues;
    }

    public List<SprintIssue> getBacklogIssues() {
        return backlogIssues;
    }

    public int getRankCustomFieldId() {
        return rankCustomFieldId;
    }

    public List<Sprint> getSprints() {
        return sprints;
    }

    public List<RapidViewProject> getProjects() {
        return projects;
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public List<Epic> getEpics() {
        return epics;
    }

    public boolean canEditEpics() {
        return canEditEpics;
    }

    public boolean canManageSprints() {
        return canManageSprints;
    }

    public boolean maxIssuesExceeded() {
        return maxIssuesExceeded;
    }

    public int queryResultLimit() {
        return queryResultLimit;
    }

    public Map<String, List<RapidViewVersion>> getVersionsPerProject() {
        return versionsPerProject;
    }
}


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

import org.apache.http.HttpRequest;
import org.json.JSONObject;

/**
 * Basic HTTP authentication credentials.
 */
public class TokenCredentials implements ICredentials {

    private String username;
    private String password;
    private String token;
	private String cookieName="JSESSIONID";

    /**
     * Creates new basic HTTP credentials.
     *
     * @param username
     * @param password
     */
    public TokenCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public TokenCredentials(String jsessionId) {
        token = jsessionId;
    }

    /**
     * Sets the Authorization header for the given request.
     *
     * @param req HTTP request to authenticate
     */
    public void authenticate(HttpRequest req) {
        if (token != null) {
            req.addHeader("Cookie",cookieName+"="+token+";");
        }
    }

    /**
     * Gets the logon name representing these credentials.
     *
     * @return logon name as a string
     */
    public String getLogonName() {
        return username;
    }

    public void initialize(RestClient client) throws JiraException {
        if (token==null) {
            try {
                JSONObject req = new JSONObject();
                req.put("username", username);
                req.put("password", password);
                JSONObject json = client.post(Resource.getAuthUri() + "session", req);
                
                JSONObject session = (JSONObject) json.opt("session");
                cookieName = session.getString("name");
                token = session.getString("value");
            } catch (Exception ex) {
                throw new JiraException("Failed to login", ex);
            }
        }
    }

    public void logout(RestClient client) throws JiraException {
        if (token != null) {
           try {
                client.delete(Resource.getAuthUri() + "session");
            } catch (Exception e) {
                throw new JiraException("Failed to logout", e);
            }
        }
    }

    public String getToken() {
        return token;
    }
}


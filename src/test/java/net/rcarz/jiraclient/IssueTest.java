package net.rcarz.jiraclient;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class IssueTest {

    /**
     * If no exception thrown the test is passed.
     */
    @Test
    public void testCreateIssue() {
        new Issue(null, Utils.getTestIssue());
    }

    @Test
    public void testGetIssueStatus() {

        String statusName = "To Do";
        String statusID = "10004";
        String description = "Issue is currently in progress.";
        String iconURL = "https://brainbubble.atlassian.net/images/icons/statuses/open.png";

        Issue issue = new Issue(null, Utils.getTestIssue());
        assertNotNull(issue.getStatus());
        assertEquals(description, issue.getStatus().getDescription());
        assertEquals(iconURL, issue.getStatus().getIconUrl());

        assertEquals(statusName, issue.getStatus().getName());
        assertEquals(statusID, issue.getStatus().getId());
    }

    @Test
    public void getReporter() {
        Issue issue = new Issue(null, Utils.getTestIssue());
        assertNotNull(issue.getReporter());

        User reporter = issue.getReporter();

        assertEquals(reporter.getDisplayName(), "Joseph McCarthy");
        assertEquals(reporter.getName(), "joseph");
        assertTrue(reporter.isActive());
        assertEquals(reporter.getEmail(), "joseph.b.mccarthy2012@googlemail.com");

        Map<String, String> avatars = reporter.getAvatarUrls();

        assertNotNull(avatars);
        assertEquals(avatars.size(), 4);

        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=16", avatars.get("16x16"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=24", avatars.get("24x24"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=32", avatars.get("32x32"));
        assertEquals("https://secure.gravatar.com/avatar/a5a271f9eee8bbb3795f41f290274f8c?d=mm&s=48", avatars.get("48x48"));
    }

    @Test
    public void testGetIssueType(){
        Issue issue = new Issue(null, Utils.getTestIssue());
        IssueType issueType = issue.getIssueType();
        assertNotNull(issueType);

        assertFalse(issueType.isSubtask());
        assertEquals(issueType.getName(), "Story");
        assertEquals(issueType.getId(), "7");
        assertEquals(issueType.getIconUrl(), "https://brainbubble.atlassian.net/images/icons/issuetypes/story.png");
        assertEquals(issueType.getDescription(), "This is a test issue type.");
    }

    @Test
    public void testGetVotes(){
        Issue issue = new Issue(null, Utils.getTestIssue());
        Votes votes = issue.getVotes();
        assertNotNull(votes);

        assertFalse(votes.hasVoted());
        assertEquals(votes.getVotes(),0);
    }

    @Test
    public void testGetWatchers(){
        Issue issue = new Issue(null, Utils.getTestIssue());
        Watches watches = issue.getWatches();

        assertNotNull(watches);

        assertFalse(watches.isWatching());
        assertEquals(watches.getWatchCount(), 0);
        assertEquals(watches.getSelf(), "https://brainbubble.atlassian.net/rest/api/2/issue/FILTA-43/watchers");
        assertEquals(watches.getWatchers(), new ArrayList<User>());
    }

    @Test
    public void testGetVersion(){
        Issue issue = new Issue(null, Utils.getTestIssue());
        List<Version> versions = issue.getFixVersions();

        assertNotNull(versions);
        assertEquals(versions.size(),1);

        Version version = versions.get(0);

        Assert.assertEquals(version.getId(), "10200");
        Assert.assertEquals(version.getName(), "1.0");
        assertFalse(version.isArchived());
        assertFalse(version.isReleased());
        Assert.assertEquals(version.getReleaseDate(), "2013-12-01");
        Assert.assertEquals(version.getDescription(), "First Full Functional Build");
    }

    @Test
    public void testPlainTimeTracking() {
        Issue issue = new Issue(null,Utils.getTestIssue());

        assertEquals(new Integer(144000), issue.getTimeEstimate());
        assertEquals(new Integer(86400), issue.getTimeSpent());
    }

    @Test
    public void testCreatedDate(){
        Issue issue = new Issue(null,Utils.getTestIssue());
        assertEquals(new DateTime(2013, 9, 29, 20, 16, 19, 854, DateTimeZone.forOffsetHours(1)).toDate(), issue.getCreatedDate());
    }

    @Test
    public void testUpdatedDate(){
      Issue issue = new Issue(null,Utils.getTestIssue());
      assertEquals(new DateTime(2013, 10, 9, 22, 24, 55, 961, DateTimeZone.forOffsetHours(1)).toDate(), issue.getUpdatedDate());
    }

    @Test
    public void testAddRemoteLink() throws JiraException {
        final TestableRestClient restClient = new TestableRestClient();
        Issue issue = new Issue(restClient, Utils.getTestIssue());
        issue.addRemoteLink("test-url", "test-title", "test-summary");
        assertEquals("/rest/api/latest/issue/FILTA-43/remotelink", restClient.postPath);
        assertEquals(true, restClient.postPayload.has("object"));
        JSONObject object = (JSONObject) restClient.postPayload.get("object");
        assertEquals("test-url", object.opt("url"));
        assertEquals("test-title", object.opt("title"));
        assertEquals("test-summary", object.opt("summary"));
    }


    @Test
    public void testRemoteLink() throws JiraException {
        final TestableRestClient restClient = new TestableRestClient();
        Issue issue = new Issue(restClient, Utils.getTestIssue());
        issue.remoteLink()
                .globalId("gid")
                .title("test-title")
                .summary("summary")
                .application("app-type", "app-name")
                .relationship("fixes")
                .icon("icon", "icon-url")
                .status(true, "status-icon", "status-title", "status-url")
                .create();
        assertEquals("/rest/api/latest/issue/FILTA-43/remotelink", restClient.postPath);
        assertEquals("gid", restClient.postPayload.optString("globalId"));
        assertEquals(true, restClient.postPayload.has("application"));
        JSONObject application = restClient.postPayload.getJSONObject("application");
        assertEquals("app-type", application.optString("type"));
        assertEquals("app-name", application.optString("name"));
        assertEquals("fixes", restClient.postPayload.optString("relationship"));
        assertEquals(true, restClient.postPayload.has("object"));
        JSONObject object = restClient.postPayload.getJSONObject("object");
        assertEquals("gid", object.optString("url"));
        assertEquals("test-title", object.optString("title"));
        assertEquals("summary", object.optString("summary"));
        assertEquals(true, object.has("icon"));
        JSONObject icon = object.getJSONObject("icon");
        assertEquals("icon", icon.optString("url16x16"));
        assertEquals("icon-url", icon.optString("title"));
        assertEquals(true, object.has("icon"));
        JSONObject status = object.getJSONObject("status");
        assertEquals(true, status.optBoolean("resolved"));
        assertEquals(true, status.has("icon"));
        JSONObject icon2 = status.getJSONObject("icon");
        assertEquals("status-title", icon2.optString("title"));
        assertEquals("status-icon", icon2.optString("url16x16"));
        assertEquals("status-url", icon2.optString("link"));
    }


    private static class TestableRestClient extends RestClient {

        public String postPath = "not called";
        public JSONObject postPayload = null;

        public TestableRestClient() {
            super(null, null);
        }

        @Override
        public JSONObject post(String path, JSONObject payload) {
            postPath = path;
            postPayload = payload;
            return null;
        }

    }

    /**
     * false is bu default so we test positive case only
     */
    @Test
    public void testDelete() throws Exception {
        RestClient restClient = mock(RestClient.class);
        URI uri = new URI("DUMMY");
        when(restClient.buildURI(anyString(), any(Map.class))).thenReturn(uri);
        when(restClient.delete(eq(uri))).thenReturn(null);
        Issue issue = new Issue(restClient, Utils.getTestIssue());
        Assert.assertTrue(issue.delete(true));
    }
}

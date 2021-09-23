package net.rcarz.jiraclient;

import java.util.ArrayList;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

public class IssueHistory extends Resource {

    private User user;
    private ArrayList<IssueHistoryItem> changes;
    private Date created;

    /**
     * Creates an issue history record from a JSON payload.
     *
     * @param restclient REST client instance
     * @param json JSON payload
     */
    protected IssueHistory(RestClient restclient, JSONObject json) {
        super(restclient);

        if (json != null) {
            deserialise(restclient,json);
        }
    }

    public IssueHistory(IssueHistory record, ArrayList<IssueHistoryItem> changes) {
        super(record.restclient);
        user = record.user;
        id = record.id;
        self = record.self;
        created = record.created;
        this.changes = changes;
    }

    private void deserialise(RestClient restclient, JSONObject json) {
        self = Field.getString(json.opt("self"));
        id = Field.getString(json.opt("id"));
        user = new User(restclient, (JSONObject)json.opt("author"));
        created = Field.getDateTime(json.opt("created"));
        JSONArray items = new JSONArray(json.opt("items"));
        changes = new ArrayList<IssueHistoryItem>(items.length());
        for (int i = 0; i < items.length(); i++) {
            JSONObject p = items.getJSONObject(i);
            changes.add(new IssueHistoryItem(restclient, p));
        }
    }

    public User getUser() {
        return user;
    }

    public ArrayList<IssueHistoryItem> getChanges() {
        return changes;
    }

    public Date getCreated() {
        return created;
    }

}

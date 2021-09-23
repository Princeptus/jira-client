package net.rcarz.jiraclient;

import org.json.JSONObject;

public class IssueHistoryItem extends Resource {

    private String field;
    private String from;
    private String to;
    private String fromStr;
    private String toStr;

    public IssueHistoryItem(RestClient restclient) {
        super(restclient);
    }

    public IssueHistoryItem(RestClient restclient, JSONObject json) {
        this(restclient);
        if (json != null) {
            deserialise(restclient,json);
        }
    }

    private void deserialise(RestClient restclient, JSONObject json) {
        self = Field.getString(json.opt("self"));
        id = Field.getString(json.opt("id"));
        field = Field.getString(json.opt("field"));
        from = Field.getString(json.opt("from"));
        to = Field.getString(json.opt("to"));
        fromStr = Field.getString(json.opt("fromString"));
        toStr = Field.getString(json.opt("toString"));
    }

    public String getField() {
        return field;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getFromStr() {
        return fromStr;
    }

    public String getToStr() {
        return toStr;
    }
    
}

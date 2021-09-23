package net.rcarz.jiraclient;

import org.json.JSONObject;

public class RemoteLink extends Resource {
    private String remoteUrl;
    private String title;

    public RemoteLink(RestClient restclient, JSONObject json) {
        super(restclient);
        if (json != null)
            deserialise(json);
    }

    private void deserialise(JSONObject json) {
        self = Field.getString(json.opt("self"));
        id = Field.getString(json.opt("id"));
        
        JSONObject object = (JSONObject)json.opt("object");
        
        remoteUrl = Field.getString(object.opt("url"));
        title = Field.getString(object.opt("title"));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }
}

package net.rcarz.jiraclient;

import java.net.URI;
import org.json.JSONObject;

/**
 * Represens a Jira filter.
 */
public class Filter extends Resource {

	private String name;
	private String jql;
	private boolean favourite;

	public Filter(RestClient restclient, JSONObject json) {
		super(restclient);

		if (json != null)
			deserialise(json);
	}

	private void deserialise(JSONObject json) {
		id = Field.getString(json.opt("id"));
		self = Field.getString(json.opt("self"));
		name = Field.getString(json.opt("name"));
		jql = Field.getString(json.opt("jql"));
		favourite = Field.getBoolean(json.opt("favourite"));
	}

	public boolean isFavourite() {
		return favourite;
	}

	public String getJql() {
		return jql;
	}

	public String getName() {
		return name;
	}

	public static Filter get(final RestClient restclient, final String id) throws JiraException {
		JSONObject result = null;

		try {
			URI uri = restclient.buildURI(getBaseUri() + "filter/" + id);
			result = restclient.getMap(uri);
		} catch (Exception ex) {
			throw new JiraException("Failed to retrieve filter with id " + id, ex);
		}

		if (result == null) {
			throw new JiraException("JSON payload is malformed");
		}

		return new Filter(restclient, result);
	}

	@Override
	public String toString() {
		return "Filter{" +
				"favourite=" + favourite +
				", name='" + name + '\'' +
				", jql='" + jql + '\'' +
				'}';
	}


}

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

import java.util.Map;

import org.json.JSONObject;

/**
 * GreenHopper estimate statistics for rapid views.
 */
public class EstimateStatistic {

    private String statFieldId = null;
    private Double statFieldValue = 0.0;
    private String statFieldText = null;

    /**
     * Creates an estimate statistic from a JSON payload.
     *
     * @param json JSON payload
     */
    protected EstimateStatistic(JSONObject json) {
        statFieldId = Field.getString(json.opt("statFieldId"));

        if (json.has("statFieldValue") &&
        		json.get("statFieldValue") instanceof JSONObject) {

        	JSONObject val = (JSONObject)json.get("statFieldValue");

            statFieldValue = Field.getDouble(val.opt("value"));
            statFieldText = Field.getString(val.opt("text"));
        }
    }

    public String getFieldId() {
        return statFieldId;
    }

    public Double getFieldValue() {
        return statFieldValue;
    }

    public String getFieldText() {
        return statFieldText;
    }
}


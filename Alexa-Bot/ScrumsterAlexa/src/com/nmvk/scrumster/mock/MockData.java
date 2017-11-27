package com.nmvk.scrumster.mock;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

/**
 * Mock data used for mocking.
 * @author raghav
 */
public class MockData {

    /**
     * Mock issue JSON.
     */
    static String issues[] = new String[2];

    /**
     * Mock calendar data.
     */
    static String calendar[] = new String[2];

    static {
        try {
            issues[0] = new JSONObject().append("key", 10)
                    .append("Summary", "Database migration")
                    .append("Status", "In-Progress").toString();

            issues[1] =  new JSONObject().append("key", 20)
                    .append("Summary", "Database Design")
                    .append("Status", "Done").toString();

            calendar[0] = new JSONArray().put("2:30 PM - 3:30PM")
                            .put("4:00 PM - 5:00 PM").toString();

            calendar[1] = new JSONArray().toString();
        } catch (JSONException je) {
            // This should not be thrown
        }
    }


}

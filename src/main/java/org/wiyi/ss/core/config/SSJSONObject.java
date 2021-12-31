package org.wiyi.ss.core.config;

import org.json.JSONException;
import org.json.JSONObject;

public class SSJSONObject extends JSONObject {

    public SSJSONObject(String s) {
        super(s);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return getInt(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public String getString(String key, String defaultValue) {
        try {
            return getString(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return getBoolean(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
}

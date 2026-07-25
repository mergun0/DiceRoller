package com.merg.diceroller.model;

import org.json.JSONException;
import org.json.JSONObject;

public final class DiceRoll {
    public final DiceMode mode;
    public final int first;
    public final int second;
    public final long timestamp;

    public DiceRoll(DiceMode mode, int first, int second, long timestamp) {
        this.mode = mode;
        this.first = first;
        this.second = second;
        this.timestamp = timestamp;
    }

    public int total() { return mode == DiceMode.DOUBLE ? first + second : first; }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("mode", mode.name());
        object.put("first", first);
        object.put("second", second);
        object.put("timestamp", timestamp);
        return object;
    }

    public static DiceRoll fromJson(JSONObject object) throws JSONException {
        return new DiceRoll(DiceMode.valueOf(object.getString("mode")), object.getInt("first"),
                object.optInt("second", 0), object.getLong("timestamp"));
    }
}

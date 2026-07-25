package com.merg.diceroller.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.merg.diceroller.model.AnimationSpeed;
import com.merg.diceroller.model.AppTheme;
import com.merg.diceroller.model.DiceMode;
import com.merg.diceroller.model.DiceRoll;
import com.merg.diceroller.model.DiceTheme;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DiceRepository {
    private static final String PREFS = "dice_roller_prefs";
    private static final String KEY_HISTORY = "history";
    private final SharedPreferences prefs;

    public DiceRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean soundEnabled() { return prefs.getBoolean("sound", true); }
    public boolean vibrationEnabled() { return prefs.getBoolean("vibration", true); }
    public boolean shakeEnabled() { return prefs.getBoolean("shake", false); }
    public DiceMode diceMode() { return DiceMode.valueOf(prefs.getString("mode", DiceMode.SINGLE.name())); }
    public DiceTheme diceTheme() { return DiceTheme.byId(prefs.getString("diceTheme", "classic")); }
    public AppTheme appTheme() { return AppTheme.valueOf(prefs.getString("appTheme", AppTheme.SYSTEM.name())); }
    public AnimationSpeed animationSpeed() { return AnimationSpeed.valueOf(prefs.getString("speed", AnimationSpeed.NORMAL.name())); }

    public void setSoundEnabled(boolean value) { prefs.edit().putBoolean("sound", value).apply(); }
    public void setVibrationEnabled(boolean value) { prefs.edit().putBoolean("vibration", value).apply(); }
    public void setShakeEnabled(boolean value) { prefs.edit().putBoolean("shake", value).apply(); }
    public void setDiceMode(DiceMode value) { prefs.edit().putString("mode", value.name()).apply(); }
    public void setDiceTheme(DiceTheme value) { prefs.edit().putString("diceTheme", value.themeId).apply(); }
    public void setAppTheme(AppTheme value) { prefs.edit().putString("appTheme", value.name()).apply(); }
    public void setAnimationSpeed(AnimationSpeed value) { prefs.edit().putString("speed", value.name()).apply(); }

    public List<DiceRoll> history() {
        ArrayList<DiceRoll> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(KEY_HISTORY, "[]"));
            for (int i = 0; i < array.length(); i++) result.add(DiceRoll.fromJson(array.getJSONObject(i)));
        } catch (JSONException ignored) {
            clearHistory();
        }
        return result;
    }

    public void addRoll(DiceRoll roll) {
        List<DiceRoll> list = history();
        list.add(0, roll);
        saveHistory(limitToTwenty(list));
    }

    public static List<DiceRoll> limitToTwenty(List<DiceRoll> list) {
        ArrayList<DiceRoll> copy = new ArrayList<>(list);
        return copy.size() > 20 ? copy.subList(0, 20) : copy;
    }

    public void clearHistory() { prefs.edit().putString(KEY_HISTORY, "[]").apply(); }

    private void saveHistory(List<DiceRoll> rolls) {
        JSONArray array = new JSONArray();
        try {
            for (DiceRoll roll : rolls) array.put(roll.toJson());
            prefs.edit().putString(KEY_HISTORY, array.toString()).apply();
        } catch (JSONException ignored) {
            prefs.edit().putString(KEY_HISTORY, "[]").apply();
        }
    }
}

package com.example.thiltapeshunting.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "thiltapes_session";
    private static final String KEY_PLAYER_ID = "player_id";
    private static final String KEY_PLAYER_NAME = "player_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void savePlayer(int playerId, String playerName) {
        prefs.edit()
                .putInt(KEY_PLAYER_ID, playerId)
                .putString(KEY_PLAYER_NAME, playerName)
                .apply();
    }

    public int getPlayerId() {
        return prefs.getInt(KEY_PLAYER_ID, -1);
    }

    public String getPlayerName() {
        return prefs.getString(KEY_PLAYER_NAME, "");
    }

    public boolean hasActiveSession() {
        return getPlayerId() > 0;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}

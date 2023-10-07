package ru.dimon6018.metrolauncher.content.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.Map;

public class Prefs implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String FILE_NAME = "Prefs";
    public static final String APP_PREFIX = "app:";
    public static final String APP_POS_PREFIX = "pos:";
    public static final String APP_TILE_SIZE = "size:";
    private final Context mContext;
    private final SharedPreferences prefs;
    public Prefs(Context context) {
        mContext = context;
        prefs = mContext.getSharedPreferences(FILE_NAME, 0);
    }
    public void addApp(String packag, String label) {
        prefs.edit()
                .putString(APP_PREFIX + packag, label)
                .apply();
    }
    public void setPos(String packag, int Pos) {
        prefs.edit()
                .putInt(APP_POS_PREFIX + packag, Pos)
                .apply();
        Log.i("Prefs", "set pos " + Pos + " for app " + packag);
    }
    public int getPos(String packag) {
        Log.i("Prefs", "getting pos " + prefs.getInt(APP_POS_PREFIX + packag, 0) + " for app " + packag);
        return prefs.getInt(APP_POS_PREFIX + packag, 0);
    }
    public void setTileSize(String packag, int size) {
        // 0 - small, default
        // 1 - medium
        // 2 - big
        prefs.edit()
                .putInt(APP_TILE_SIZE + packag, size)
                .apply();
    }
    public int getTileSize(String packag) {
        // 0 - small, default
        // 1 - medium
        // 2 - big
        return prefs.getInt(APP_TILE_SIZE + packag, 0);
    }
    public void removeApp(String packag) {
        prefs.edit().remove(APP_PREFIX + packag).apply();
        prefs.edit().remove(APP_POS_PREFIX + packag).apply();
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
    }
        public LinkedList<App> getAppsPackage() {
            LinkedList<App> apps = new LinkedList<>();
            Map<String, ?> map = prefs.getAll();
            for (String key : map.keySet()) {
                App app = new App();
                if (key.startsWith(APP_PREFIX)) {
                    String seed = key.substring(APP_PREFIX.length()); //package
                    app.app_label = (String) map.get(key); //name
                    app.app_package = seed;
                    apps.add(app);
                }
            }
            return apps;
        }

}


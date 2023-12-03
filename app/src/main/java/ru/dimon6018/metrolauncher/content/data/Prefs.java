package ru.dimon6018.metrolauncher.content.data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.Map;

import static ru.dimon6018.metrolauncher.content.data.DataProvider.mDataStatic;

public class Prefs implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String FILE_NAME = "Prefs";
    public static final String APP_PREFIX = "app:";
    public static final String APP_POS_PREFIX = "pos:";
    public static final String APP_TILE_COLOR = "tilecolor:";
    public static final String IS_APP_HAVE_CUSTOM_COLOR = "isTileHaveCC:";
    public static final String APP_TILE_SIZE = "size:";
    public static final String ACCENT_COLOR = "accentColor";
    public static final String LAUNCHER_LIGHT_THEME = "useLightTheme";
    public static final String LAUNCHER_CUSTOM_BACKGRD = "useCustomBackground";
    public static final String IS_LAUNCHER_USING_CUSTOM_BACKGRD = "isCustomBackgrdUsing";
    private final SharedPreferences prefs;
    public static boolean isAccentChanged = false;
    public Prefs(Context context) {
        prefs = context.getSharedPreferences(FILE_NAME, 0);
    }
    public void addApp(String packag, String label) {
        prefs.edit()
                .putString(APP_PREFIX + packag, label)
                .apply();
    }
    public void removeApp(String packag) {
        prefs.edit().remove(APP_PREFIX + packag).apply();
        prefs.edit().remove(APP_POS_PREFIX + packag).apply();
    }
    public void setLauncherCustomBackgrdAvailability(boolean bool) {
        prefs.edit().putBoolean(IS_LAUNCHER_USING_CUSTOM_BACKGRD, bool).apply();
    }
    public boolean isCustomBackgroundUsed() {
        return prefs.getBoolean(IS_LAUNCHER_USING_CUSTOM_BACKGRD, false);
    }
    public void setCustomBackgrdPath(String path) {
        prefs.edit().putString(LAUNCHER_CUSTOM_BACKGRD, path).apply();
    }
    public String getBackgroundPath() {
        return prefs.getString(LAUNCHER_CUSTOM_BACKGRD, "");
    }
    public void useLightTheme(boolean bool) {
        prefs.edit().putBoolean(LAUNCHER_LIGHT_THEME, bool).apply();
    }
    public boolean isLightThemeUsed() {
        return prefs.getBoolean(LAUNCHER_LIGHT_THEME, false);
    }
    public void setPos(String packag, int Pos) {
        prefs.edit()
                .putInt(APP_POS_PREFIX + packag, Pos)
                .apply();
    }
    public int getPos(String packag) {
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
    public void setTileColor(String packag, int color) {
        // 0 - use accent color
        // 1 - red
        // 2 - yellow
        // 3 - green
        // 4 - purple
        // 5 - orange
        // 6 - blue
        prefs.edit()
                .putInt(APP_TILE_COLOR + packag, color)
                .apply();
    }
    public int getTileColor(String packag) {
        // 0 - use accent color
        // 1 - lime
        // 2 - green
        // 3 - emerald
        // 4 - cyan
        // 5 - teal
        // 6 - cobalt
        // 7 - indigo
        // 8 - violet
        // 9 - pink
        // 10 - magenta
        // 11 - crimson
        // 12 - red
        // 13 - orange
        // 14 - amber
        // 15 - yellow
        // 16 - brown
        // 17 - olive
        // 18 - steel
        // 19 - mauve
        // 20 - taupe
        return prefs.getInt(APP_TILE_COLOR + packag, 0);
    }
    public void setAccentColor(int color) {
        // 0 - lime
        // 1 - green
        // 2 - emerald
        // 3 - cyan
        // 4 - teal
        // 5 - cobalt
        // 6 - indigo
        // 7 - violet
        // 8 - pink
        // 9 - magenta
        // 10 - crimson
        // 11 - red
        // 12 - orange
        // 13 - amber
        // 14 - yellow
        // 15 - brown
        // 16 - olive
        // 17 - steel
        // 18 - mauve
        // 19 - taupe
        isAccentChanged = true;
        prefs.edit()
                .putInt(ACCENT_COLOR, color)
                .apply();
    }
    public int getAccentColor() {
        // 0 - lime
        // 1 - green
        // 2 - emerald
        // 3 - cyan
        // 4 - teal
        // 5 - cobalt
        // 6 - indigo
        // 7 - violet
        // 8 - pink
        // 9 - magenta
        // 10 - crimson
        // 11 - red
        // 12 - orange
        // 13 - amber
        // 14 - yellow
        // 15 - brown
        // 16 - olive
        // 17 - steel
        // 18 - mauve
        // 19 - taupe
        return prefs.getInt(ACCENT_COLOR, 5);
    }
    public void setAppTileColorAvailability(String packag, boolean answer) {
        prefs.edit().putBoolean(IS_APP_HAVE_CUSTOM_COLOR + packag, answer).apply();
    }
    public boolean getAppTileColorAvailability(String packag) {
        return prefs.getBoolean(IS_APP_HAVE_CUSTOM_COLOR + packag, false);
    }
    public boolean isItemPinedToStart(String packag) {
        int temp = mDataStatic.size();
        boolean isItemAdded = false;
        while (temp != 0) {
            temp = temp -1;
            if (mDataStatic.get(temp).getPackage().equals(packag)) {
                isItemAdded = true;
                break;
            }
        }
        return isItemAdded;
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


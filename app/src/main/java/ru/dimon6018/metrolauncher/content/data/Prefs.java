package ru.dimon6018.metrolauncher.content.data;

import android.content.Context;
import android.content.SharedPreferences;
public class Prefs {
    public static final String FILE_NAME = "Prefs";
    public static final String ACCENT_COLOR = "accentColor";
    public static final String LAUNCHER_LIGHT_THEME = "useLightTheme";
    public static final String LAUNCHER_CUSTOM_BACKGRD = "useCustomBackground";
    public static final String IS_LAUNCHER_USING_CUSTOM_BACKGRD = "isCustomBackgrdUsing";
    private final SharedPreferences prefs;
    public static boolean isAccentChanged = false;
    public Prefs(Context context) {
        prefs = context.getSharedPreferences(FILE_NAME, 0);
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
        return prefs.getInt(ACCENT_COLOR, 5);
    }
    public void reset() {
        prefs.getAll().clear();
    }
}


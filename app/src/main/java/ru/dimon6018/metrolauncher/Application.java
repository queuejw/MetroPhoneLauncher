package ru.dimon6018.metrolauncher;

import android.annotation.SuppressLint;
import android.content.Context;

import ru.dimon6018.metrolauncher.content.data.Prefs;

public class Application extends android.app.Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    static int[] accentColors = {
            R.color.tile_lime, R.color.tile_green, R.color.tile_emerald, R.color.tile_cyan,
            R.color.tile_teal, R.color.tile_cobalt, R.color.tile_indigo, R.color.tile_violet,
            R.color.tile_pink, R.color.tile_magenta, R.color.tile_crimson, R.color.tile_red,
            R.color.tile_orange, R.color.tile_amber, R.color.tile_yellow, R.color.tile_brown,
            R.color.tile_olive, R.color.tile_steel, R.color.tile_mauve, R.color.tile_taupe
    };
    static String[] accentNames = {
            "lime", "green", "emerald", "cyan", "teal", "cobalt", "indigo", "violet",
            "pink", "magenta", "crimson", "red", "orange", "amber", "yellow", "brown",
            "olive", "steel", "mauve", "taupe"
    };
    public void onCreate() {
        super.onCreate();
        Application.context = getApplicationContext();
    }
    public static int getAccentColorFromPrefs() {

        int selectedColor = new Prefs(context).getAccentColor();

        if (selectedColor >= 0 && selectedColor < accentColors.length) {
            return context.getColor(accentColors[selectedColor]);
        } else {
            // Default to cobalt if the selected color is out of bounds
            return context.getColor(R.color.tile_cobalt);
        }
    }
    public static int getTileColorFromPrefs(int tileColor) {
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
        if (tileColor >= 0 && tileColor < accentColors.length) {
            return context.getColor(accentColors[tileColor]);
        } else {
            // Default to cobalt if the selected color is out of bounds
            return context.getColor(R.color.tile_cobalt);
        }
    }
    public static int getLauncherAccentTheme() {
        int[] themeStyles = {
                R.style.AppTheme_Lime, R.style.AppTheme_Green, R.style.AppTheme_Emerald,
                R.style.AppTheme_Cyan, R.style.AppTheme_Teal, R.style.AppTheme_Cobalt,
                R.style.AppTheme_Indigo, R.style.AppTheme_Violet, R.style.AppTheme_Pink,
                R.style.AppTheme_Magenta, R.style.AppTheme_Crimson, R.style.AppTheme_Red,
                R.style.AppTheme_Orange, R.style.AppTheme_Amber, R.style.AppTheme_Yellow,
                R.style.AppTheme_Brown, R.style.AppTheme_Olive, R.style.AppTheme_Steel,
                R.style.AppTheme_Mauve, R.style.AppTheme_Taupe
        };

        int selectedColor = new Prefs(context).getAccentColor();

        if (selectedColor >= 0 && selectedColor < themeStyles.length) {
            return themeStyles[selectedColor];
        } else {
            // Default to cobalt theme if the selected color is out of bounds
            return R.style.AppTheme_Cobalt;
        }
    }
    public static String getAccentName() {

        int selectedColor = new Prefs(context).getAccentColor();

        if (selectedColor >= 0 && selectedColor < accentNames.length) {
            return accentNames[selectedColor];
        } else {
            // Default to "unknown" if the selected color is out of bounds
            return "unknown";
        }
    }
    public static String getTileColorName(int color) {
        if (color >= 0 && color < accentNames.length) {
            return accentNames[color];
        } else {
            // Default to "unknown" if the selected color is out of bounds
            return "unknown";
        }
    }
    public static Context getAppContext() {
        return Application.context;
    }
}


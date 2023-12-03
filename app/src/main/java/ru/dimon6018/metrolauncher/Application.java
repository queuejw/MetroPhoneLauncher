package ru.dimon6018.metrolauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import ru.dimon6018.metrolauncher.content.data.Prefs;

public class Application extends android.app.Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Application.context = getApplicationContext();
    }
    public static int getAccentColorFromPrefs() {
         int color;
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
         switch(new Prefs(context).getAccentColor()) {
            case 0: {
                color = context.getColor(R.color.tile_lime);
            }
            case 1: {
                color = context.getColor(R.color.tile_green);
            }
            case 2: {
                color = context.getColor(R.color.tile_emerald);
            }
            case 3: {
                color = context.getColor(R.color.tile_cyan);
            }
            case 4: {
                color = context.getColor(R.color.tile_teal);
            }
            case 5: {
                color = context.getColor(R.color.tile_cobalt);
            }
            case 6: {
                color = context.getColor(R.color.tile_indigo);
            }
            case 7: {
                 color = context.getColor(R.color.tile_violet);
            }
            case 8: {
                 color = context.getColor(R.color.tile_pink);
            }
             case 9: {
                 color = context.getColor(R.color.tile_magenta);
             }
             case 10: {
                 color = context.getColor(R.color.tile_crimson);
             }
             case 11: {
                 color = context.getColor(R.color.tile_red);
             }
             case 12: {
                 color = context.getColor(R.color.tile_orange);
             }
             case 13: {
                 color = context.getColor(R.color.tile_amber);
             }
             case 14: {
                 color = context.getColor(R.color.tile_yellow);
             }
             case 15: {
                 color = context.getColor(R.color.tile_brown);
             }
             case 16: {
                 color = context.getColor(R.color.tile_olive);
             }
             case 17: {
                 color = context.getColor(R.color.tile_steel);
             }
             case 18: {
                 color = context.getColor(R.color.tile_mauve);
             }
             case 19: {
                 color = context.getColor(R.color.tile_taupe);
             }
            default: {
                color = context.getColor(R.color.tile_cobalt);
            }
        }
        return color;
    }
    public static int getTileColorFromPrefs(int tileColor) {
        int color;
        switch (tileColor) {
            case 0: {
                color = context.getColor(R.color.tile_lime);
            }
            case 1: {
                color = context.getColor(R.color.tile_green);
            }
            case 2: {
                color = context.getColor(R.color.tile_emerald);
            }
            case 3: {
                color = context.getColor(R.color.tile_cyan);
            }
            case 4: {
                color = context.getColor(R.color.tile_teal);
            }
            case 5: {
                color = context.getColor(R.color.tile_cobalt);
            }
            case 6: {
                color = context.getColor(R.color.tile_indigo);
            }
            case 7: {
                color = context.getColor(R.color.tile_violet);
            }
            case 8: {
                color = context.getColor(R.color.tile_pink);
            }
            case 9: {
                color = context.getColor(R.color.tile_magenta);
            }
            case 10: {
                color = context.getColor(R.color.tile_crimson);
            }
            case 11: {
                color = context.getColor(R.color.tile_red);
            }
            case 12: {
                color = context.getColor(R.color.tile_orange);
            }
            case 13: {
                color = context.getColor(R.color.tile_amber);
            }
            case 14: {
                color = context.getColor(R.color.tile_yellow);
            }
            case 15: {
                color = context.getColor(R.color.tile_brown);
            }
            case 16: {
                color = context.getColor(R.color.tile_olive);
            }
            case 17: {
                color = context.getColor(R.color.tile_steel);
            }
            case 18: {
                color = context.getColor(R.color.tile_mauve);
            }
            case 19: {
                color = context.getColor(R.color.tile_taupe);
            }
            default: {
                color = context.getColor(R.color.tile_cobalt);
            }
        }
        return color;
    }
    public static int getLauncherAccentTheme() {
        int result;
        Prefs prefs = new Prefs(context);
        switch (prefs.getAccentColor()) {
            case 0:
                result = R.style.AppTheme_Lime;
                break;
            case 1:
                result = R.style.AppTheme_Green;
                break;
            case 2:
                result = R.style.AppTheme_Emerald;
                break;
            case 3:
                result = R.style.AppTheme_Cyan;
                break;
            case 4:
                result = R.style.AppTheme_Teal;
                break;
            case 5:
                result = R.style.AppTheme_Cobalt;
                break;
            case 6:
                result = R.style.AppTheme_Indigo;
                break;
            case 7:
                result = R.style.AppTheme_Violet;
                break;
            case 8:
                result = R.style.AppTheme_Pink;
                break;
            case 9:
                result = R.style.AppTheme_Magenta;
                break;
            case 10:
                result = R.style.AppTheme_Crimson;
                break;
            case 11:
                result = R.style.AppTheme_Red;
                break;
            case 12:
                result = R.style.AppTheme_Orange;
                break;
            case 13:
                result = R.style.AppTheme_Amber;
                break;
            case 14:
                result = R.style.AppTheme_Yellow;
                break;
            case 15:
                result = R.style.AppTheme_Brown;
                break;
            case 16:
                result = R.style.AppTheme_Olive;
                break;
            case 17:
                result = R.style.AppTheme_Steel;
                break;
            case 18:
                result = R.style.AppTheme_Mauve;
                break;
            case 19:
                result = R.style.AppTheme_Taupe;
                break;
            default:
                result = R.style.AppTheme_Cobalt;
                break;
        }
        return result;
    }
    public static String getAccentName() {
        String result = "unknown";
        Prefs prefs = new Prefs(context);
        switch (prefs.getAccentColor()) {
            case 0:
                result = "lime";
                break;
            case 1:
                result = "green";
                break;
            case 2:
                result = "emerald";
                break;
            case 3:
                result = "cyan";
                break;
            case 4:
                result = "teal";
                break;
            case 5:
                result = "cobalt";
                break;
            case 6:
                result = "indigo";
                break;
            case 7:
                result = "violet";
                break;
            case 8:
                result = "pink";
                break;
            case 9:
                result = "magenta";
                break;
            case 10:
                result = "crimson";
                break;
            case 11:
                result = "red";
                break;
            case 12:
                result = "orange";
                break;
            case 13:
                result = "amber";
                break;
            case 14:
                result = "yellow";
                break;
            case 15:
                result = "brown";
                break;
            case 16:
                result = "olive";
                break;
            case 17:
                result = "steel";
                break;
            case 18:
                result = "mauve";
                break;
            case 19:
                result = "taupe";
                break;
            default:
                break;
        }
        return result;
    }
    public static Context getAppContext() {
        return Application.context;
    }
}
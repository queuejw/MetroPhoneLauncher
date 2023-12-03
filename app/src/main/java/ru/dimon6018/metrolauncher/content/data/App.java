package ru.dimon6018.metrolauncher.content.data;

import android.graphics.drawable.Drawable;

public class App {
    public String app_label;
    public String app_package;
    public Drawable app_icon;
    public int CurrentPosition;
    public int tilesize;
    public boolean isSection = false;
    public boolean isTileUseCustomColor = false;
    public int tileCustomColor;
    public String getLabel() {
        return this.app_label;
    }
    public String getPackagel() {
        return this.app_package;
    }
    public Drawable getDrawable() {
        return this.app_icon;
    }
    public int getCurrentPosition() {
        return CurrentPosition;
    }
    public int getTileCustomColor() {
        return tileCustomColor;
    }
    public boolean isTileUsingCustomColor() {
        return isTileUseCustomColor;
    }
    public int getTileSize() {
        return this.tilesize;
    }
}
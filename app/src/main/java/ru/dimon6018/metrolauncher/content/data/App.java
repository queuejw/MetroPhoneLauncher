package ru.dimon6018.metrolauncher.content.data;

import android.graphics.drawable.Drawable;

public class App {
    public CharSequence app_label;
    public CharSequence app_package;
    public Drawable app_icon;
    public int CurrentPosition;
    public boolean tilebig = false;
    public boolean tilemedium = false;
    public boolean tilesmall = false;
    public boolean isSection = false;
    public CharSequence getLabel() {
        return this.app_label;
    }
    public CharSequence getPackagel() {
        return this.app_package;
    }
    public Drawable getDrawable() {
        return this.app_icon;
    }
    public int getCurrentPosition() {
        return CurrentPosition;
    }
    public boolean isTileBig() {
        return this.tilebig;
    }
    public boolean isTileMedium() {
        return this.tilemedium;
    }
    public boolean isTileSmall() {
        return this.tilesmall;
    }
}

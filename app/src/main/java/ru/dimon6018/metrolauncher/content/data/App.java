package ru.dimon6018.metrolauncher.content.data;

import android.graphics.drawable.Drawable;

public class App {
    public String app_label;
    public String app_package;
    public Drawable app_icon;
    public boolean isSection = false;
    public String getLabel() {
        return this.app_label;
    }
    public String getPackagel() {
        return this.app_package;
    }
    public Drawable getDrawable() {
        return this.app_icon;
    }
}
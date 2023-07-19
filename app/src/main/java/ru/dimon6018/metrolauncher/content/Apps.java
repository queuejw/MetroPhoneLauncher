package ru.dimon6018.metrolauncher.content;

import android.graphics.drawable.Drawable;

public class Apps {
    public CharSequence app_label;
    public CharSequence app_package;
    public Drawable app_icon;

    public CharSequence getLabel() {
        return this.app_label;
    }
    public CharSequence getPackagel() {
        return this.app_package;
    }
    public Drawable getDrawable() {
        return this.app_icon;
    }
}


package ru.dimon6018.metrolauncher;

import android.annotation.SuppressLint;
import android.content.Context;

public class Application extends android.app.Application {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Application.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Application.context;
    }
}
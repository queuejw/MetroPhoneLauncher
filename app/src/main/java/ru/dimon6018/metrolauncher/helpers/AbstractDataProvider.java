package ru.dimon6018.metrolauncher.helpers;

import android.graphics.drawable.Drawable;
import ru.dimon6018.metrolauncher.content.data.App;
import ru.dimon6018.metrolauncher.content.data.DataProvider;

public abstract class AbstractDataProvider {
    public static abstract class Data {
        public abstract long getId();
        public abstract boolean isSectionHeader();
        public abstract int getViewType();
        public abstract int getTilePos();
        public abstract void setTileSize(int size);
        public abstract int getTileSize();
        public abstract String getText();
        public abstract String getPackage();

        public abstract boolean isTileUsingCustomColor();

        public abstract int getTileColor();

        public abstract void setPinned(boolean pinned);

        public abstract boolean isPinned();
        public abstract Drawable getDrawable();
    }
    public abstract int getCount();

    public abstract Data getItem(int index);

    public abstract void addItem(int pos, DataProvider.ConcreteData app, App apps);

    public abstract void removeItem(int position);

    public abstract void moveItem(int fromPosition, int toPosition);

    public abstract void swapItem(int fromPosition, int toPosition);
    public abstract int undoLastRemoval();
}
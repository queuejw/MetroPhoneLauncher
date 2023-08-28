package ru.dimon6018.metrolauncher.helpers;

import android.graphics.drawable.Drawable;

public abstract class AbstractDataProvider {
    public static abstract class Data {

        public abstract long getId();

        public abstract boolean isSectionHeader();

        public abstract int getViewType();
        public abstract int getTilePos();
        public abstract String getText();
        public abstract String getPackage();

        public abstract void setPinned(boolean pinned);

        public abstract boolean isPinned();
        public abstract Drawable getDrawable();
        public abstract boolean isTileBig();
        public abstract boolean isTileMedium();
        public abstract boolean isTileSmall();
    }
    public abstract int getCount();

    public abstract Data getItem(int index);

    public abstract void removeItem(int position);

    public abstract void moveItem(int fromPosition, int toPosition);

    public abstract void swapItem(int fromPosition, int toPosition);
    public abstract int undoLastRemoval();
}
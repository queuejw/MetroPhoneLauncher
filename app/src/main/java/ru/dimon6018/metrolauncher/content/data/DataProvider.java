package ru.dimon6018.metrolauncher.content.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import ru.dimon6018.metrolauncher.Application;
import ru.dimon6018.metrolauncher.R;
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
public class DataProvider extends AbstractDataProvider {
    public final List<ConcreteData> mData;
    public static List<ConcreteData> mDataStatic;
    private final LinkedList<App> mPrevData;
    private ConcreteData mLastRemovedData;
    private int mLastRemovedPosition = -1;
    boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    public DataProvider() {
        Context context = Application.getAppContext();
        mData = new LinkedList<>();
        mPrevData = new Prefs(context).getAppsPackage();
        PackageManager pManager = context.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        int end = mPrevData.size();
        int end2 = mPrevData.size();
        //Firstly create placeholder
        while (end != 0) {
            end = end - 1;
            mData.add(new ConcreteData(mPrevData.size(), 0, "", context.getDrawable(R.drawable.ic_os_android), end, "", 1, false, 0));
        }
        //change placeholders to the apps
        while (end2 != 0) {
            final int id = mData.size();
            final int viewType = 0;
            App app = mPrevData.get(end2 - 1);
            if(!isPackageInstalled(app.app_package, pManager)) {
                new Prefs(context).removeApp(app.app_package);
                continue;
            }
            app.CurrentPosition = new Prefs(context).getPos(app.app_package);
            int pos = new Prefs(context).getPos(app.app_package);
            final String text = app.app_label;
            final String packag = app.app_package;
            app.isTileUseCustomColor = new Prefs(context).getAppTileColorAvailability(packag);
            app.tilesize = new Prefs(context).getTileSize(app.app_package);
            app.tileCustomColor = new Prefs(context).getTileColor(app.app_package);
            final Drawable icon;
            try {
                icon = pManager.getApplicationIcon(app.app_package);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            end2 = end2 - 1;
            mData.set(pos, new ConcreteData(id, viewType, text, icon, pos, packag, app.tilesize, app.isTileUseCustomColor, app.tileCustomColor));
        }
        mDataStatic = mData;
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Data getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }
        return mData.get(index);
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mData.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mData.size();
            }

            mData.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        ConcreteData item1 = mData.get(fromPosition);
        ConcreteData item2 = mData.get(toPosition);
        mData.remove(item1);
        mData.add(toPosition, item1);
        mData.remove(item2);
        mData.add(fromPosition, item2);
        mLastRemovedPosition = -1;
    }

    @Override
    public void swapItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        Collections.swap(mData, toPosition, fromPosition);
        mLastRemovedPosition = -1;
    }
    @Override
    public void addItem(int pos, ConcreteData app, App apps) {
        mData.add(pos, app);
        mPrevData.add(apps);
    }

    @Override
    public void removeItem(int position) {
        final ConcreteData removedItem = mData.remove(position);
        mLastRemovedData = removedItem;
        mLastRemovedPosition = position;
    }

    public static final class ConcreteData extends Data {

        private final long mId;
        @NonNull
        private final String mText;
        private final String mPackage;
        @NonNull
        private final Drawable mIco;
        private final int mViewType;
        private boolean mPinned;
        private boolean mIsTileUsingCustomColor;
        private final int mPos;
        private final int mTileColor;
        private int mSize;
        public ConcreteData(long id, int viewType, @NonNull String text, @NonNull Drawable ico, int pos, String packag, int size, boolean usingCustomColor, int tileColor) {
            mId = id;
            mViewType = viewType;
            mIco = ico;
            mPos = pos;
            mPackage = packag;
            mTileColor = tileColor;
            mIsTileUsingCustomColor = usingCustomColor;
            mSize = size;
            mText = makeText(text);
        }
        private static String makeText(String text) {
            return text;
        }
        @Override
        public boolean isSectionHeader() {
            return false;
        }
        @Override
        public int getViewType() {
            return mViewType;
        }
        @Override
        public int getTilePos() {
            return mPos;
        }
        @Override
        public void setTileSize(int size) {
            mSize = size;
        }
        @Override
        public int getTileSize() {
            return mSize;
        }
        @Override
        public long getId() {
            return mId;
        }
        public Drawable getDrawable() {
            return mIco;
        }
        @NonNull
        @Override
        public String toString() {
            return mText;
        }
        @Override
        public String getText() {
            return mText;
        }
        @Override
        public String getPackage() {
            return mPackage;
        }
        @Override
        public boolean isPinned() {
            return mPinned;
        }
        @Override
        public boolean isTileUsingCustomColor() {
            return mIsTileUsingCustomColor;
        }
        @Override
        public int getTileColor() {
            return mTileColor;
        }
        @Override
        public void setPinned(boolean pinned) {
            mPinned = pinned;
        }
    }
}
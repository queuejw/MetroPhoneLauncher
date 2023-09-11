package ru.dimon6018.metrolauncher.content.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ru.dimon6018.metrolauncher.Application;
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider;

public class DataProvider extends AbstractDataProvider {
    public final List<ConcreteData> mData;
    private final LinkedList<App> mPrevData;
    private ConcreteData mLastRemovedData;
    private int mLastRemovedPosition = -1;

    public DataProvider() {
        Context context = Application.getAppContext();
        mData = new LinkedList<>();
        mPrevData = new Prefs(context).getAppsPackage();
        PackageManager pManager = context.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        int end = mPrevData.size();
        Log.i("Data", "size " + end);
        while (end != 0) {
            final int id = mPrevData.size();
            final int viewType = 0;
            App app = mPrevData.get(end - 1);
            app.CurrentPosition = new Prefs(context).getPos(app.app_package);
            int pos = new Prefs(context).getPos(app.app_package);
            Log.i("Data", "current id size mData: " + mData.size());
            Log.i("Data", "current item pos: " + app.getCurrentPosition());
            Log.i("Data", "current item label: " + app.app_label);
            Log.i("Data", "current item package: " + app.app_package);
            final String text = app.app_label;
            final String packag = app.app_package;
            app.tilesize = new Prefs(context).getTileSize(app.app_package);
            final int size = app.tilesize;
            final Drawable icon;
            try {
                icon = pManager.getApplicationIcon(app.app_package);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            end = end - 1;
            mData.add(new ConcreteData(id, viewType, text, icon, pos, packag, size));
        }
       after(context);
    }

    private void after(Context context) {
        Log.i("Data", "Run tasks after init load");
        int end = mPrevData.size();
        while (end != 0) {
            ConcreteData item = mData.get(end - 1);
            swapItem(end - 1, new Prefs(context).getPos(item.getPackage()));
            end = end - 1;

        }
        Log.i("Data", "done");
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
        Log.d("SwapItems", "Swap");
        Collections.swap(mData, toPosition, fromPosition);
        mLastRemovedPosition = -1;
    }

    @Override
    public void removeItem(int position) {
        final ConcreteData removedItem = mData.remove(position);
        Log.d("Remove Item", "Remove Item");
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
        private final int mPos;
        private final int mSize;
        ConcreteData(long id, int viewType, @NonNull String text, @NonNull Drawable ico, int pos, String packag, int size) {
            mId = id;
            mViewType = viewType;
            mIco = ico;
            mPos = pos;
            mPackage = packag;
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
        public void setPinned(boolean pinned) {
            mPinned = pinned;
        }
    }
}
package ru.dimon6018.metrolauncher.content.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ru.dimon6018.metrolauncher.Application;
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider;

public class DataProvider extends AbstractDataProvider {
    private final List<ConcreteData> mData;
    private final LinkedList mPrevData;
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
        final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;
        while (end != 0) {
            final int id = mPrevData.size();
            final int viewType = 0;
            App app;
            app = (App) mPrevData.get(end - 1);
            app.CurrentPosition = new Prefs(context).getPos((String) app.app_package);
            int pos = new Prefs(context).getPos((String) app.app_package);
            Log.i("Data", "current id size mData: " + mData.size());
            Log.i("Data", "current item pos: " + app.getCurrentPosition());
            Log.i("Data", "current item label: " + app.app_label);
            Log.i("Data", "current item package: " + app.app_package);
            final String text = (String) app.app_label;
            final String packag = (String) app.app_package;
            final Drawable icon;
            try {
                icon = pManager.getApplicationIcon((String) app.app_package);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            end = end - 1;
            mData.add(new ConcreteData(id, viewType, text, swipeReaction, icon, pos, packag));
        }
       after(context);
    }

    private void after(Context context) {
        Log.i("Data", "Run tasks after init load");
        int end = mPrevData.size();
        while (end != 0) {
            ConcreteData item = mData.get(end - 1);
            mData.remove(item);
            mData.add(item.getTilePos(), item);
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
        Log.d("move", "Move Items");
        Log.d("move", "Move " + mData.get(fromPosition).getText() + " to " + mData.get(toPosition).getText());
        ConcreteData item1 = mData.get(fromPosition);
        ConcreteData item2 = mData.get(toPosition);
        mData.remove(item1);
        mData.add(toPosition, item1);
        Log.d("move", "Remove " + item1.getText() );
        Log.d("move", "Remove " + item2.getText() );
        mData.remove(item2);
        mData.add(fromPosition, item2);
        Log.d("move", "Add " + item2.getText() + " to pos " + fromPosition);
        Log.d("move", "Add " + item1.getText() + " to pos " + toPosition);
        new Prefs(Application.getAppContext()).setPos(item1.getPackage(), toPosition);
        new Prefs(Application.getAppContext()).setPos(item2.getPackage(), fromPosition);

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
        private int mPos;

        ConcreteData(long id, int viewType, @NonNull String text, int swipeReaction, @NonNull Drawable ico, int pos, String packag) {
            mId = id;
            mViewType = viewType;
            mIco = ico;
            mPos = pos;
            mPackage = packag;
            mText = makeText(text, swipeReaction);
        }

        private static String makeText(String text, int swipeReaction) {
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
        public long getId() {
            return mId;
        }
        public Drawable getDrawable() {
            return mIco;
        }
        @Override
        public boolean isTileBig() {
            return false;
        }

        @Override
        public boolean isTileMedium() {
            return false;
        }

        @Override
        public boolean isTileSmall() {
            return false;
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
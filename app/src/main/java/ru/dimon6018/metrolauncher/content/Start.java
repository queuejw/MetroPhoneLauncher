package ru.dimon6018.metrolauncher.content;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemState;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.LinkedList;

import ru.dimon6018.metrolauncher.Main;
import ru.dimon6018.metrolauncher.R;
import ru.dimon6018.metrolauncher.content.data.App;
import ru.dimon6018.metrolauncher.content.data.Prefs;
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider;
import ru.dimon6018.metrolauncher.helpers.DrawableUtils;

public class Start extends Fragment {
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;

    public Start() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_screen_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //noinspection ConstantConditions
        mRecyclerView = getView().findViewById(R.id.start_apps_tiles);
        mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(requireContext(), R.drawable.material_shadow_z3));
        // Start dragging after long press
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);
        mRecyclerViewDragDropManager.setInitiateOnMove(false);
        mRecyclerViewDragDropManager.setLongPressTimeout(1000);

        //adapter
        final StartAdapter myItemAdapter = new StartAdapter(getDataProvider());
        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);      // wrap for dragging

        final GeneralItemAnimator animator = new DraggableItemAnimator();

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setHasFixedSize(false);

        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        // for debugging
//        animator.setDebug(true);
//        animator.setMoveDuration(2000);
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }
    public AbstractDataProvider getDataProvider() {
        return ((Main) requireActivity()).getDataProvider();
    }
    public class StartAdapter
            extends RecyclerView.Adapter<StartAdapter.BaseViewHolder>
            implements DraggableItemAdapter<StartAdapter.BaseViewHolder> {
        private static final String TAG = "adapter";
        private static final int ITEM_VIEW_TYPE_HEADER = 0;
        private static final int ITEM_VIEW_TYPE_NORMAL_ITEM_OFFSET = 1;

        private static final boolean USE_DUMMY_HEADER = true;
        private static final boolean RANDOMIZE_ITEM_SIZE = false;

        private final AbstractDataProvider mProvider;

        public static class BaseViewHolder extends AbstractDraggableItemViewHolder {
            public BaseViewHolder(View v) {
                super(v);
            }
        }

        public static class HeaderItemViewHolder extends BaseViewHolder {
            public HeaderItemViewHolder(View v) {
                super(v);
            }
        }

        public static class NormalItemViewHolder extends BaseViewHolder {
            public FrameLayout mContainer;
            public View mDragHandle;
            public TextView mTextView;
            public ImageView mAppIcon;
            public NormalItemViewHolder(View v) {
                super(v);
                mContainer = v.findViewById(R.id.container);
                mDragHandle = v.findViewById(R.id.drag_handle);
                mTextView = v.findViewById(android.R.id.text1);
                mAppIcon = v.findViewById(android.R.id.icon1);
            }
        }
        public StartAdapter(AbstractDataProvider dataProvider) {
            mProvider = dataProvider;

            // DraggableItemAdapter requires stable ID, and also
            // have to implement the getItemId() method appropriately.
            setHasStableIds(true);
            Log.e("Adapter","Adapter Loaded");
        }

        @Override
        public long getItemId(int position) {
            if (isHeader(position)) {
                return RecyclerView.NO_ID;
            } else {
                return mProvider.getItem(toNormalItemPosition(position)).getId();
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isHeader(position)) {
                return ITEM_VIEW_TYPE_HEADER;
            } else {
                return ITEM_VIEW_TYPE_NORMAL_ITEM_OFFSET + mProvider.getItem(toNormalItemPosition(position)).getViewType();
            }
        }

        @NonNull
        @Override
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case ITEM_VIEW_TYPE_HEADER: {
                    // NOTE:
                    // This dummy header item is required to workaround the
                    // weired animation when occurs on moving the item 0
                    //
                    // Related issue
                    //   Issue 99047:	Inconsistent behavior produced by mAdapter.notifyItemMoved(indexA,indexB);
                    //   https://code.google.com/p/android/issues/detail?id=99047&q=notifyItemMoved&colspec=ID%20Status%20Priority%20Owner%20Summary%20Stars%20Reporter%20Opened&
                    final View v = inflater.inflate(R.layout.placeholder, parent, false);
                    return new StartAdapter.HeaderItemViewHolder(v);
                }
                default: {
                    final View v = inflater.inflate(R.layout.tile, parent, false);
                    return new StartAdapter.NormalItemViewHolder(v);
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
            if (isHeader(position)) {
                onBindHeaderViewHolder((HeaderItemViewHolder) holder, position);
            } else {
                onBindNormalItemViewHolder((NormalItemViewHolder) holder, toNormalItemPosition(position));
            }
        }

        private void onBindHeaderViewHolder(HeaderItemViewHolder holder, int position) {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            lp.setFullSpan(true);
        }

        private void onBindNormalItemViewHolder(NormalItemViewHolder holder, int position) {

            final AbstractDataProvider.Data item = mProvider.getItem(position);

            // set text
            holder.mTextView.setText(item.getText());
            holder.mAppIcon.setImageDrawable(item.getDrawable());

            // set item view height
            Context context = holder.itemView.getContext();
            int itemHeight = calcItemHeight(context, item);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp.height != itemHeight) {
                lp.height = itemHeight;
                holder.itemView.setLayoutParams(lp);
            }
            // set background resource (target view ID: container)
            final DraggableItemState dragState = holder.getDragState();

            if (dragState.isUpdated()) {
                if (dragState.isActive()) {
                } else if (dragState.isDragging()) {
                } else {
                }
                holder.mDragHandle.setOnClickListener(view -> showMenuAdapter(holder.itemView, R.menu.tile_menu_start, holder, position, item));
            }
        }
        private void showMenuAdapter(View v, @MenuRes int menuRes, NormalItemViewHolder holder, int pos, AbstractDataProvider.Data item) {
            LinkedList mPrevData = new Prefs(getContext()).getAppsPackage();
            PopupMenu popup = new PopupMenu(getActivity(), v);
            popup.getMenuInflater().inflate(menuRes, popup.getMenu());
            App app = (App) mPrevData.get(pos);
            popup.setOnMenuItemClickListener(
                    menuItem -> {
                        if(menuItem.getItemId() == R.id.remove_tile) {
                            new Prefs(getContext()).removeApp((String) app.app_package);
                            mProvider.removeItem(item.getTilePos());
                            Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT);
                            notifyDataSetChanged();
                        }
                        else if(menuItem.getItemId() == R.id.tile_small) {
                            app.tilesmall = true;
                            app.tilemedium = false;
                            app.tilebig = false;
                            notifyDataSetChanged();
                        }
                        else if(menuItem.getItemId() == R.id.tile_medium) {
                            app.tilesmall = false;
                            app.tilemedium = true;
                            app.tilebig = false;
                            notifyDataSetChanged();
                        }
                        else if(menuItem.getItemId() == R.id.tile_big) {
                            app.tilesmall = false;
                            app.tilemedium = false;
                            app.tilebig = true;
                            notifyDataSetChanged();
                        }
                        return true;
                    });
            popup.show();
        }
        @Override
        public int getItemCount() {
            int headerCount = getHeaderItemCount();
            int count = mProvider.getCount();
            return headerCount + count;
        }

        @Override
        public void onMoveItem(int fromPosition, int toPosition) {
            Log.d(TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");

            fromPosition = toNormalItemPosition(fromPosition);
            toPosition = toNormalItemPosition(toPosition);

            mProvider.moveItem(fromPosition, toPosition);
        }

        @Override
        public boolean onCheckCanStartDrag(@NonNull BaseViewHolder holder, int position, int x, int y) {
            return !isHeader(position);
        }

        @Nullable
        @Override
        public ItemDraggableRange onGetItemDraggableRange(@NonNull BaseViewHolder holder, int position) {
            int headerCount = getHeaderItemCount();
            return new ItemDraggableRange(headerCount, getItemCount() - 1);
        }

        @Override
        public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
            return true;
        }

        @Override
        public void onItemDragStarted(int position) {
            notifyDataSetChanged();
        }

        @Override
        public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
            notifyDataSetChanged();
        }

        static int getHeaderItemCount() {
            return (USE_DUMMY_HEADER) ? 1 : 0;
        }

        static boolean isHeader(int position) {
            return (position < getHeaderItemCount());
        }

        static int toNormalItemPosition(int position) {
            return position - getHeaderItemCount();
        }

        static int calcItemHeight(Context context, AbstractDataProvider.Data item) {
            float density = context.getResources().getDisplayMetrics().density;
            if (RANDOMIZE_ITEM_SIZE) {
                int s = (int) item.getId();
                s = swapBit(s, 2, 2);
                s = swapBit(s, 4,  4);
                s = swapBit(s, 6, 4);
                return (int) ((8 + (s % 13)) * 10 * density);
            } else {
                int s = (int) item.getId();
                if(item.isTileBig()) {
                    return (int) (150 * density);
                } else if(item.isTileMedium()) {
                    return (int) (100 * density);
                } else if(item.isTileSmall()) {
                    return (int) (50 * density);
                } else {
                    return (int) (100 * density);
                }
            }
        }

        static int swapBit(int x, int pos1, int pos2) {
            int m1 = 1 << pos1;
            int m2 = 1 << pos2;
            int y = x & ~(m1 | m2);

            if ((x & m1) != 0) {
                y |= m2;
            }
            if ((x & m2) != 0) {
                y |= m1;
            }

            return y;
        }
    }
}

package ru.dimon6018.metrolauncher.content;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.arasthel.spannedgridlayoutmanager.SpanSize;
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager;
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemState;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import ru.dimon6018.metrolauncher.Main;
import ru.dimon6018.metrolauncher.R;
import ru.dimon6018.metrolauncher.content.data.Prefs;
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider;

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
        SpannedGridLayoutManager mSpannedLayoutManager = new SpannedGridLayoutManager(SpannedGridLayoutManager.Orientation.VERTICAL, 3);
        mSpannedLayoutManager.setSpanSizeLookup(new SpannedGridLayoutManager.SpanSizeLookup(position -> {
            Log.i("LM", "pos" + position);
            int size = getDataProvider().getCount();
            if(position != size) {
                AbstractDataProvider.Data item = getDataProvider().getItem(position);
                Log.i("LM", "item pos" + item.getTilePos());
                Log.i("LM", "item pack" + item.getPackage());
                Log.i("LM", "item size" + item.getTileSize());
                return switch (item.getTileSize()) {
                    case 0 -> new SpanSize(1, 1);
                    case 1 -> new SpanSize(2, 2);
                    case 2 -> new SpanSize(3, 2);
                    default -> new SpanSize(1, 1);
                };
            } else {
                return new SpanSize(1, 1);
            }
        }));
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

        @SuppressLint("ResourceType")
        private void onBindNormalItemViewHolder(NormalItemViewHolder holder, int position) {

            final AbstractDataProvider.Data item = mProvider.getItem(position);
            // set text
            holder.mTextView.setText(item.getText());
            holder.mAppIcon.setImageDrawable(item.getDrawable());

            final DraggableItemState dragState = holder.getDragState();
            if (dragState.isUpdated()) {
                if (dragState.isActive()) {
                } else if (dragState.isDragging()) {
                } else {
                }
            }
        }
        @Override
        public int getItemCount() {
            int headerCount = getHeaderItemCount();
            int count = mProvider.getCount();
            return headerCount + count;
        }

        @Override
        public void onMoveItem(int fromPosition, int toPosition) {
            Log.i(TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");
            fromPosition = toNormalItemPosition(fromPosition);
            toPosition = toNormalItemPosition(toPosition);
            AbstractDataProvider.Data item1 = getDataProvider().getItem(fromPosition);
            AbstractDataProvider.Data item2 = getDataProvider().getItem(toPosition);
            new Prefs(getContext()).setPos(item1.getPackage(), toPosition);
            new Prefs(getContext()).setPos(item2.getPackage(), fromPosition);
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
    }
}
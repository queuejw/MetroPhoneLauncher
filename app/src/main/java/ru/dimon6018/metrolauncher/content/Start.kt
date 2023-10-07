package ru.dimon6018.metrolauncher.content

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.DataProvider.mDataStatic
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider
import ru.dimon6018.metrolauncher.helpers.ItemTouchCallback
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperAdapter
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperViewHolder
import ru.dimon6018.metrolauncher.helpers.OnStartDragListener
import ru.dimon6018.metrolauncher.helpers.SpaceItemDecorator
import java.util.Collections





class Start : Fragment(), OnStartDragListener {
     private var mRecyclerView: RecyclerView? = null
     private var mItemTouchHelper: ItemTouchHelper? = null
     private var mSpannedLayoutManager: SpannedGridLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_screen_content, container, false)
    }

    override fun onResume() {
        super.onResume()
        mRecyclerView?.adapter?.notifyDataSetChanged()
        Log.i("resume", "resume")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerView = view.findViewById(R.id.start_apps_tiles)
        //adapter
        val adapter = StartAdapter(dataProvider, this)
        mRecyclerView?.addItemDecoration(SpaceItemDecorator(5, 5, 5, 5))
        mSpannedLayoutManager = SpannedGridLayoutManager(orientation = RecyclerView.VERTICAL, _rowCount = 8, _columnCount = 4, context = requireActivity())
        mSpannedLayoutManager!!.itemOrderIsStable = true
        mSpannedLayoutManager!!.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
            when (dataProvider.getItem(position).tileSize) {
                0 -> {
                    SpanSize(1, 1)
                }
                1 -> {
                    SpanSize(2, 2)
                }
                2 -> {
                    SpanSize(4, 2)
                }
                else -> {
                    SpanSize(1, 1)
                }
            }
        }
        mRecyclerView?.layoutManager = mSpannedLayoutManager
        mRecyclerView?.adapter = adapter
        val callback: ItemTouchHelper.Callback = ItemTouchCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper!!.attachToRecyclerView(mRecyclerView)
    }
    override fun onDestroyView() {
        if (mRecyclerView != null) {
            mRecyclerView!!.itemAnimator = null
            mRecyclerView!!.layoutManager = null
            mRecyclerView!!.adapter = null
            mRecyclerView = null
        }
        super.onDestroyView()
    }

    private val dataProvider: AbstractDataProvider
        get() = (requireActivity() as Main).dataProvider

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper!!.startDrag(viewHolder)
    }
    inner class StartAdapter(private val mProvider: AbstractDataProvider, private val mDragStartListener: OnStartDragListener) : Adapter<StartAdapter.NormalItemViewHolder>(), ItemTouchHelperAdapter {
        override fun onItemMove(fromPosition: Int, toPosition: Int) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(mDataStatic, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(mDataStatic, i, i - 1)
                }
            }
            trySaveAllPositions()
            notifyItemMoved(fromPosition, toPosition)
        }
        private fun trySaveAllPositions() {
            var allItemsCount = itemCount
            while (allItemsCount != 0) {
                val pos = allItemsCount - 1
                val item = mDataStatic[pos]
                allItemsCount -= 1
                Log.i("savePos", "Saving item pos. Item " + item.text + ". New pos: " + pos + " . (Old pos: " + item.tilePos)
                Prefs(context).setPos(item.`package`, pos)
            }
        }
        override fun onItemDismiss(position: Int) {}

        inner class NormalItemViewHolder(v: View) : RecyclerView.ViewHolder(v), ItemTouchHelperViewHolder {
            val mContainer: FrameLayout
            val mTextView: TextView
            val mAppIcon: ImageView

            init {
                mContainer = v.findViewById(R.id.container)
                mTextView = v.findViewById(android.R.id.text1)
                mAppIcon = v.findViewById(android.R.id.icon1)
            }

            override fun onItemSelected() {}
            override fun onItemClear() {}
        }


        override fun getItemId(position: Int): Long {
            return mProvider.getItem(position).id
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NormalItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.tile, parent, false)
            return NormalItemViewHolder(v)
        }

        override fun onBindViewHolder(holder: NormalItemViewHolder, position: Int) {
            val item = mProvider.getItem(position)
            // set text
            if(item.tileSize != 0) {
                holder.mTextView.text = item.text
            } else {
                holder.mTextView.text = ""
            }
            holder.mAppIcon.setImageDrawable(item.drawable)
            holder.mContainer.setOnLongClickListener {
                val wp = WPDialog(activity)
                wp.setTitle(item.text)
                        .setMessage("What do you want to do?")
                        .setPositiveButton("resize") {
                            when (item.tileSize) {
                                0 -> {
                                    Prefs(context).setTileSize(item.getPackage(), 1)
                                    item.tileSize = 1
                                }
                                1 -> {
                                    Prefs(context).setTileSize(item.getPackage(), 2)
                                    item.tileSize = 2
                                }
                                2 -> {
                                    Prefs(context).setTileSize(item.getPackage(), 0)
                                    item.tileSize = 0
                                }
                            }
                            notifyItemChanged(position)
                            wp.dismiss()
                        }
                        .setNegativeButton("remove") {
                            Prefs(context).removeApp(item.`package`)
                            mProvider.removeItem(position)
                            notifyItemRemoved(position)
                            wp.dismiss()
                        }
                        .setNeutralButton("move") {
                            mDragStartListener.onStartDrag(holder)
                        }
                        .show()
                true
            }
        }
        override fun getItemCount(): Int {
            return mProvider.count
        }
    }
}
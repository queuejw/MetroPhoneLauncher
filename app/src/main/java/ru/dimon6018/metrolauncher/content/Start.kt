package ru.dimon6018.metrolauncher.content

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import ir.alirezabdn.wp7progress.WP7ProgressBar
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.DataProvider.mDataStatic
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.helpers.*
import java.util.*

class Start : Fragment(), OnStartDragListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAppListButton: MaterialCardView? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var mSpannedLayoutManager: SpannedGridLayoutManager? = null
    private var adapter: StartAdapter? = null
    private var backgroundImg: ImageView? = null

    private var loadingHolder: LinearLayout? = null
    private var progressBar: WP7ProgressBar? = null

    private var background: LinearLayout? = null

    var contxt: Context? = null
    var prefs: Prefs? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.start_screen, container, false)

        contxt = context
        prefs = Prefs(contxt)
        progressBar = v.findViewById(R.id.progressBarStart)
        progressBar!!.showProgressBar()
        loadingHolder = v.findViewById(R.id.loadingHolderStart)
        mAppListButton = v.findViewById(R.id.open_applist_btn)
        background = v.findViewById(R.id.startBackground)
        //background = v.findViewById(R.id.startBackground)
        mRecyclerView = v.findViewById(R.id.start_apps_tiles)
        if (prefs!!.isCustomBackgroundUsed) {
            try {
                background!!.background = AppCompatResources.getDrawable(contxt!!, R.drawable.start_transparent)
            } catch (ex: Exception) {
                Snackbar.make(mRecyclerView!!, "something went wrong. see $ex", Snackbar.LENGTH_LONG).show()
                Log.e("Start", ex.toString())
                requireActivity().window.setBackgroundDrawable(AppCompatResources.getDrawable(contxt!!, R.drawable.start_background))
            }
        } else {
            requireActivity().window.setBackgroundDrawable(AppCompatResources.getDrawable(contxt!!, R.drawable.start_background))
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val threadStart = Thread {
            adapter = StartAdapter(dataProvider, this)
            mSpannedLayoutManager = SpannedGridLayoutManager(orientation = RecyclerView.VERTICAL, _rowCount = 8, _columnCount = 4, context = contxt!!)
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
            val callback: ItemTouchHelper.Callback = ItemTouchCallback(adapter)
            mItemTouchHelper = ItemTouchHelper(callback)
            mRecyclerView!!.post {
                mRecyclerView!!.layoutManager = mSpannedLayoutManager
                mRecyclerView!!.adapter = adapter
                mRecyclerView!!.addItemDecoration(SpaceItemDecorator(8, 8, 8, 8))
                mItemTouchHelper!!.attachToRecyclerView(mRecyclerView)
                hideLoadingHolder()
            }
        }
        threadStart.priority = 1
        threadStart.start()
    }

    private fun hideLoadingHolder() {
        progressBar!!.hideProgressBar()
        loadingHolder!!.visibility = View.GONE
        mRecyclerView!!.visibility = View.VISIBLE
        mAppListButton!!.visibility = View.VISIBLE
    }
    override fun onResume() {
        super.onResume()
        adapter?.setNewData(dataProvider)
    }
    override fun onDestroyView() {
        adapter = null

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
    inner class StartAdapter(private var mProvider: AbstractDataProvider, private val mDragStartListener: OnStartDragListener) : Adapter<StartAdapter.NormalItemViewHolder>(), ItemTouchHelperAdapter {
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
            saveAllPositions()
            notifyItemMoved(fromPosition, toPosition)
        }
        private fun saveAllPositions() {
            var allItemsCount = itemCount
            while (allItemsCount != 0) {
                val pos = allItemsCount - 1
                val item = mDataStatic[pos]
                allItemsCount -= 1
                prefs!!.setPos(item.`package`, pos)
            }
        }
        override fun onItemDismiss(position: Int) {
            notifyDataSetChanged()
        }

        fun setNewData(mProviderNew: AbstractDataProvider) {
            mProvider = mProviderNew
            notifyDataSetChanged()
        }
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
            if(prefs!!.isCustomBackgroundUsed) {
                holder.mContainer.background = AppCompatResources.getDrawable(contxt!!, R.drawable.start_transparent)
                val view: View = holder.mContainer
                val bmp = BitmapFactory.decodeFile(prefs!!.backgroundPath)
                view.getViewTreeObserver().addOnGlobalLayoutListener {
                    val bmpNew = Bitmap.createBitmap(bmp, view.x.toInt(), view.y.toInt(), view.width, view.height, null, true)
                    holder.mContainer.background = bmpNew.toDrawable(resources)
                }
            }
            holder.mContainer.setOnClickListener {
                val intent = context!!.packageManager.getLaunchIntentForPackage(item.`package`)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            holder.mAppIcon.setImageDrawable(item.drawable)
            // if(item.isTileUsingCustomColor) { holder.mContainer.setBackgroundColor(Application.getTileColorFromPrefs(item.tileColor)) } else { holder.mContainer.setBackgroundColor(Application.getAccentColorFromPrefs()) }
            holder.mContainer.setOnLongClickListener {
                val wp = WPDialog(activity)
                wp.setTitle(item.text)
                        .setMessage("What do you want to do?")
                        .setPositiveButton("resize") {
                            when (item.tileSize) {
                                0 -> {
                                    prefs!!.setTileSize(item.getPackage(), 1)
                                    item.tileSize = 1
                                    holder.mTextView.text = ""
                                }
                                1 -> {
                                    prefs!!.setTileSize(item.getPackage(), 2)
                                    item.tileSize = 2
                                }
                                2 -> {
                                    prefs!!.setTileSize(item.getPackage(), 0)
                                    item.tileSize = 0
                                }
                            }
                            notifyDataSetChanged()
                            wp.dismiss()
                        }
                        .setNegativeButton("remove") {
                            prefs!!.removeApp(item.`package`)
                            mProvider.removeItem(position)
                            notifyItemRemoved(position)
                            wp.dismiss()
                        }
                        .show()
                false
            }
        }
        override fun getItemCount(): Int {
            return mProvider.count
        }
    }
}
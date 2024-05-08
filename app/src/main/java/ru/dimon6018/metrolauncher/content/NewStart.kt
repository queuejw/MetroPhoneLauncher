package ru.dimon6018.metrolauncher.content

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.collection.ArrayMap
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import ir.alirezabdn.wp7progress.WP7ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isAppOpened
import ru.dimon6018.metrolauncher.Application.Companion.isStartMenuOpened
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.apps.AppDao
import ru.dimon6018.metrolauncher.content.data.apps.AppData
import ru.dimon6018.metrolauncher.content.data.apps.AppEntity
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.helpers.IconPackManager
import ru.dimon6018.metrolauncher.helpers.ItemTouchCallback
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperAdapter
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperViewHolder
import ru.dimon6018.metrolauncher.helpers.OnStartDragListener
import ru.dimon6018.metrolauncher.helpers.receivers.PackageChangesReceiver
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.getTileColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.getTileColorName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.recompressIcon
import java.util.Collections
import kotlin.random.Random


class NewStart: Fragment(), OnStartDragListener {

    private var mRecyclerView: RecyclerView? = null
    private var mSpannedLayoutManager: SpannedGridLayoutManager? = null
    private var mAdapter: NewStartAdapter? = null
    private var tiles: MutableList<AppEntity>? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var appsDbCall: AppDao? = null
    private var loadingProgressBar: WP7ProgressBar? = null

    private lateinit var frame: ConstraintLayout

    private var allAppsButton: MaterialCardView? = null
    private var currentActivity: Activity? = null

    private var packageBroadcastReceiver: BroadcastReceiver? = null
    private val hashCache = ArrayMap<String, Icon?>()
    private var iconManager: IconPackManager? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currentActivity = activity
        if (PREFS!!.iconPackPackage != "") {
            iconManager = IconPackManager()
            iconManager!!.setContext(context)
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.start_screen, container, false)
        mRecyclerView = v.findViewById(R.id.start_apps_tiles)
        loadingProgressBar = v.findViewById(R.id.progressBarStart)
        loadingProgressBar?.showProgressBar()
        frame = v.findViewById(R.id.startFrame)
        lifecycleScope.launch(Dispatchers.Default) {
            appsDbCall = AppData.getAppData(requireContext()).getAppDao()
            tiles = appsDbCall!!.getJustApps()
            //attempt to optimize icon loading
            tiles?.forEach {
                if(it.tileType != -1) {
                    try {
                        hashCache[it.appPackage] = recompressIcon(getAppIcon(it.appPackage, it.tileSize, requireContext().packageManager, resources), 80)
                    } catch (e: NameNotFoundException) {
                        Log.e("Start", e.toString())
                    }
                }
            }
            //
            mSpannedLayoutManager = if (!PREFS!!.isMoreTilesEnabled) {
                SpannedGridLayoutManager(
                    orientation = RecyclerView.VERTICAL,
                    _rowCount = 8,
                    _columnCount = 4
                )
            } else {
                SpannedGridLayoutManager(
                    orientation = RecyclerView.VERTICAL,
                    _rowCount = 12,
                    _columnCount = 6
                )
            }
            mSpannedLayoutManager!!.itemOrderIsStable = true
            mSpannedLayoutManager!!.spanSizeLookup =
                SpannedGridLayoutManager.SpanSizeLookup { position ->
                    when (tiles!![position].tileSize) {
                        "small" -> {
                            SpanSize(1, 1)
                        }

                        "medium" -> {
                            SpanSize(2, 2)
                        }

                        "big" -> {
                            SpanSize(4, 2)
                        }

                        else -> {
                            SpanSize(1, 1)
                        }
                    }
                }
            mAdapter = NewStartAdapter(requireContext(), tiles!!)
            val callback: ItemTouchHelper.Callback = ItemTouchCallback(mAdapter!!)
            mItemTouchHelper = ItemTouchHelper(callback)
            withContext(Dispatchers.Main) {
                allAppsButton = v.findViewById(R.id.allAppsButton)
                mRecyclerView?.apply {
                    layoutManager = mSpannedLayoutManager
                    adapter = mAdapter
                    if(PREFS!!.isWallpaperUsed && !PREFS!!.isTilesTransparent) {
                        addItemDecoration(Utils.MarginItemDecoration(6))
                    }
                    mItemTouchHelper!!.attachToRecyclerView(this)
                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            if (mAdapter?.isEditMode == false) {
                                if (!recyclerView.canScrollVertically(-1)) {
                                    allAppsButton!!.visibility = View.INVISIBLE
                                } else {
                                    allAppsButton!!.visibility = View.VISIBLE
                                }
                            }
                        }
                    })
                }
                frame.setOnClickListener {
                    if (mAdapter?.isEditMode == true) {
                        mAdapter?.disableEditMode()
                    }
                }
                loadingProgressBar?.hideProgressBar()
                Log.d("Start", "launch observer function")
            }
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerBroadcast()
    }
    private fun observe() {
        Log.d("Start", "start observer")
        appsDbCall?.getApps()?.asLiveData()?.observe(viewLifecycleOwner) {
            if (mAdapter != null) {
                if (!mAdapter?.isEditMode!! && mAdapter?.list != it) {
                    Log.d("flow", "update list")
                    mAdapter?.setData(it)
                }
            }
        }
    }
    private fun stopObserver() {
        Log.d("Start", "stop observer")
        appsDbCall?.getApps()?.asLiveData()?.removeObservers(viewLifecycleOwner)
    }
    override fun onResume() {
        super.onResume()
        isStartMenuOpened = true
        if(isAppOpened) {
            Log.d("resumeStart", "start enter animation")
            //TODO add normal animation
            isAppOpened = false
        }
        observe()
    }

    override fun onPause() {
        super.onPause()
        if(mAdapter?.isEditMode == true) {
            mAdapter?.disableEditMode()
        }
        isStartMenuOpened = false
    }
    override fun onStop() {
        Log.d("Start", "stop")
        stopObserver()
        super.onStop()
    }
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null) {
            if (viewHolder.itemViewType == mAdapter?.spaceType) {
                return
            }
            if (mAdapter?.isEditMode == true) {
                mItemTouchHelper!!.startDrag(viewHolder)
            }
        }
    }
    private fun registerBroadcast() {
        packageBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val packageName = intent.getStringExtra("package")
                val action = intent.getIntExtra("action", 42)
                // End early if it has anything to do with us.
                if (! packageName.isNullOrEmpty() && packageName.contains(requireContext().packageName)) return
                when(action) {
                    PackageChangesReceiver.PACKAGE_REMOVED -> {
                        packageName?.apply { broadcastListUpdater(packageName, true) }
                    }
                    PackageChangesReceiver.PACKAGE_INSTALLED -> {
                        if (PREFS!!.pinNewApps) {
                            if (packageName != null) {
                                pinApp(packageName)
                            }
                        }
                    }
                    PackageChangesReceiver.PACKAGE_UPDATED -> {
                        packageName?.apply {
                            broadcastListUpdater(packageName, false)
                        }
                    }
                }
            }
        }
        IntentFilter().apply {
            addAction("ru.dimon6018.metrolauncher.PACKAGE_CHANGE_BROADCAST")
        }.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().registerReceiver(packageBroadcastReceiver, it, Context.RECEIVER_EXPORTED)
            } else {
                requireActivity().registerReceiver(packageBroadcastReceiver, it)
            }
        }
    }
    private fun broadcastListUpdater(packageName: String, isDelete: Boolean) {
        packageName.apply {
            lifecycleScope.launch(Dispatchers.IO) {
                var newList = appsDbCall!!.getJustApps()
                if(isDelete) {
                    newList.forEach {
                        if(it.appPackage == packageName) {
                            destroyTile(it)
                            return@forEach
                        }
                    }
                    newList = appsDbCall!!.getJustApps()
                }
                runBlocking {
                    activity?.runOnUiThread {
                        mAdapter?.setData(newList)
                    }
                }
            }
        }
    }
    private fun pinApp(packageName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dBlist = appsDbCall!!.getJustApps()
            var pos = 0
            for (i in 0..<dBlist.size) {
                if (dBlist[i].tileType == -1) {
                    pos = i
                    break
                }
            }
            val id = Random.nextLong(1000, 2000000)
            val item = AppEntity(
                pos, id, -1, 0,
                isSelected = false,
                tileSize = Utils.generateRandomTileSize(true),
                appLabel = activity?.packageManager?.getApplicationInfo(packageName, 0)?.loadLabel(requireActivity().packageManager!!).toString(),
                appPackage = packageName
            )
            appsDbCall!!.insertItem(item)
            val newData = appsDbCall!!.getJustApps()
            withContext(Dispatchers.Main) {
                mAdapter?.setData(newData)
            }
        }
    }
    private fun unregisterBroadcast() {
        packageBroadcastReceiver?.apply {
            requireActivity().unregisterReceiver(packageBroadcastReceiver)
            packageBroadcastReceiver = null
        } ?: run {
            Log.d("Start", "unregisterBroadcast() was called to a null receiver.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterBroadcast()
    }
    private fun getAppIcon(appPackage: String, size: String, pm: PackageManager, mRes: Resources): Bitmap {
        var drawable = if(PREFS!!.iconPackPackage == "null") pm.getApplicationIcon(appPackage) else iconManager?.getIconPackWithName(PREFS!!.iconPackPackage)?.getDrawableIconForPackage(appPackage, null)
        if(drawable == null) {
            drawable = pm.getApplicationIcon(appPackage)
        }
        return when (size) {
            "small" -> {
                if (PREFS!!.isMoreTilesEnabled) {
                    drawable.toBitmap(mRes.getDimensionPixelSize(R.dimen.tile_small_moreTiles_on), mRes.getDimensionPixelSize(R.dimen.tile_small_moreTiles_on))
                } else {
                    drawable.toBitmap(mRes.getDimensionPixelSize(R.dimen.tile_small_moreTiles_off), mRes.getDimensionPixelSize(R.dimen.tile_small_moreTiles_off))
                }
            }
            "medium" -> {
                if (PREFS!!.isMoreTilesEnabled) {
                    drawable.toBitmap(mRes.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_on), mRes.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_on))
                } else {
                    drawable.toBitmap(mRes.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), mRes.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off))
                }
            }
            "big" -> {
                if (PREFS!!.isMoreTilesEnabled) {
                    drawable.toBitmap(mRes.getDimensionPixelSize(R.dimen.tile_big_moreTiles_on), mRes.getDimensionPixelSize(R.dimen.tile_big_moreTiles_on))
                } else {
                    drawable.toBitmap(mRes.getDimensionPixelSize(R.dimen.tile_big_moreTiles_off), mRes.getDimensionPixelSize(R.dimen.tile_big_moreTiles_off))
                }
            }
            else -> {
                drawable.toBitmap(mRes.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), mRes.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off))
            }
        }
    }
    private fun destroyTile(it: AppEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            it.tileType = -1
            it.tileSize = "small"
            it.appPackage = ""
            it.tileColor = -1
            it.appLabel = ""
            it.id = it.id!! / 2
            appsDbCall!!.updateApp(it)
        }
    }
    inner class NewStartAdapter(val context: Context, var list: MutableList<AppEntity>): RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

        private val defaultTileType: Int = 0
        val spaceType: Int = 1

        var isEditMode = false
        private val animList: MutableList<ObjectAnimator> = ArrayList()
        private var transparentColor: Int? = null

        init {
            setHasStableIds(true)
            transparentColor = ContextCompat.getColor(context, R.color.transparent)
        }
        fun setData(newData: MutableList<AppEntity>) {
            val diffUtilCallback = DiffUtilCallback(list, newData)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback, false)
            diffResult.dispatchUpdatesTo(this)
            list = newData
            tiles = newData
        }
        private fun refreshData(newData: MutableList<AppEntity>) {
            val diffUtilCallback = DiffUtilCallback(list, newData)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback, false)
            diffResult.dispatchUpdatesTo(this)
        }
        private fun enableEditMode() {
            if(isEditMode) {
                return
            }
            mRecyclerView!!.startAnimation(AnimationUtils.loadAnimation(context, R.anim.editmode_enter))
            mRecyclerView!!.scaleX = 0.9f
            mRecyclerView!!.scaleY = 0.9f
            mRecyclerView!!.setBackgroundColor(if(PREFS!!.isLightThemeUsed) ContextCompat.getColor(context, android.R.color.background_light) else ContextCompat.getColor(context, android.R.color.background_dark))
            frame.background = if(PREFS!!.isLightThemeUsed) ContextCompat.getColor(context, android.R.color.background_light).toDrawable() else ContextCompat.getColor(context, android.R.color.background_dark).toDrawable()
            isEditMode = true
            notifyDataSetChanged()
        }
        fun disableEditMode() {
            if(!isEditMode) {
                return
            }
            mRecyclerView!!.startAnimation(AnimationUtils.loadAnimation(context, R.anim.editmode_dismiss))
            mRecyclerView!!.scaleX = 1f
            mRecyclerView!!.scaleY = 1f
            mRecyclerView!!.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            frame.background = null
            isEditMode = false
            clearItems()
            for(anim in animList) {
                anim.cancel()
            }
            animList.clear()
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when(viewType) {
                defaultTileType -> {
                    val v = inflater.inflate(R.layout.tile, parent, false)
                    TileViewHolder(v)
                }
                else -> {
                    val v = inflater.inflate(R.layout.space, parent, false)
                    SpaceViewHolder(v)
                }
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun getItemId(position: Int): Long {
            return list[position].id!!
        }
        private fun createBaseWobble(v: View): ObjectAnimator {
            val animator = ObjectAnimator()
            animator.setDuration(425)
            animator.interpolator = LinearInterpolator()
            animator.repeatMode = ValueAnimator.REVERSE
            animator.repeatCount = ValueAnimator.INFINITE
            animator.setPropertyName("rotation")
            animator.setTarget(v)
            return animator
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when(holder.itemViewType) {
                defaultTileType -> bindDefaultTile(holder as TileViewHolder, position, list[position])
            }
        }
        private fun bindDefaultTile(holder: TileViewHolder, position: Int, item: AppEntity) {
            setTileSize(item, holder.mTextView)
            setTileEditModeAnim(holder, item, position)
            setTileColor(holder, item)
            setTileIcon(holder, item)
        }
        private fun setTileIcon(holder: TileViewHolder, item: AppEntity) {
            try {
                holder.mAppIcon.setImageIcon(hashCache[item.appPackage])
            } catch (e: Exception) {
                Log.e("Adapter", e.toString())
                Utils.saveError(e.toString(), BSOD.getData(context))
            }
        }
        private fun setTileColor(holder: TileViewHolder, item: AppEntity) {
            if(!isEditMode) {
                if (item.tileColor != -1) {
                    holder.mContainer.setBackgroundColor(getTileColorFromPrefs(item.tileColor!!, context))
                } else {
                    if (PREFS!!.isWallpaperUsed) {
                        if(PREFS!!.isTilesTransparent) {
                            holder.mContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                        } else {
                            holder.mContainer.setBackgroundColor(accentColorFromPrefs(context))
                        }
                    } else {
                        holder.mContainer.setBackgroundColor(accentColorFromPrefs(context))
                    }
                }
            }
        }
        private fun setTileEditModeAnim(holder: TileViewHolder, item: AppEntity, position: Int) {
            if(isEditMode) {
                val anim = createBaseWobble(holder.itemView)
                if (item.tileColor != -1) {
                    holder.mContainer.setBackgroundColor(getTileColorFromPrefs(item.tileColor!!, context))
                } else {
                    holder.mContainer.setBackgroundColor(accentColorFromPrefs(context))
                }
                if(item.isSelected == false) {
                    if (position % 2 == 0) {
                        anim.setFloatValues(-1.2f, 1.2f)
                    } else {
                        anim.setFloatValues(1.2f, -1.2f)
                    }
                    animList.add(anim)
                    anim.start()
                } else {
                    try {
                        animList[position].cancel()
                        anim.cancel()
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }
                }
            } else {
                holder.itemView.rotation = 0f
            }
        }
        private fun setTileSize(item: AppEntity, mTextView: TextView) {
            when (item.tileSize) {
                "small" -> {
                    mTextView.text = null
                }
                "medium" -> {
                    if (PREFS!!.isMoreTilesEnabled) {
                        mTextView.text = null
                    } else {
                        mTextView.text = item.appLabel
                    }
                }
                "big" -> {
                    mTextView.text = item.appLabel
                }
            }
        }
        override fun getItemViewType(position: Int): Int {
            return when(list[position].tileType) {
                -1 -> spaceType
                0 -> defaultTileType
                else -> spaceType
            }
        }
        override fun onItemMove(fromPosition: Int, toPosition: Int) {
            if(!isEditMode) {
                return
            }
            Log.d("ItemMove", "from pos: $fromPosition")
            Log.d("ItemMove", "to pos: $toPosition")
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(list, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(list, i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onItemDismiss(position: Int) {
            if(!isEditMode) {
                return
            }
            notifyItemChanged(position)
        }

        override fun onDragAndDropCompleted(viewHolder: RecyclerView.ViewHolder?) {
            if (!isEditMode) {
                return
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val itemsReserved = list
                for (i in 0 until itemsReserved.size) {
                    val item = itemsReserved[i]
                    item.appPos = i
                    appsDbCall?.insertItem(item)
                }
                val updatedList = appsDbCall!!.getJustApps()
                withContext(Dispatchers.Main) {
                    if (isEditMode) {
                        refreshData(updatedList)
                    }
                }
            }
        }
        private fun showPopupWindow(holder: TileViewHolder, item: AppEntity, position: Int) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = if(item.tileSize == "small" && PREFS!!.isMoreTilesEnabled) inflater.inflate(R.layout.tile_window_small, holder.itemView as ViewGroup, false) else inflater.inflate(R.layout.tile_window, holder.itemView as ViewGroup, false)
            val width = holder.itemView.width
            val height = holder.itemView.height
            val popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow.animationStyle = R.style.enterStyle
            val resize = popupView.findViewById<MaterialCardView>(R.id.resize)
            val resizeIcon = popupView.findViewById<ImageView>(R.id.resizeIco)
            val settings = popupView.findViewById<MaterialCardView>(R.id.settings)
            val remove = popupView.findViewById<MaterialCardView>(R.id.remove)
            popupWindow.setOnDismissListener {
                clearItems()
                runBlocking {
                    notifyItemChanged(position)
                }
            }
            popupWindow.showAsDropDown(holder.itemView, 0, ((-1 * holder.itemView.height)), Gravity.CENTER)
            val arrow = when(item.tileSize) {
                "small" -> {
                    resizeIcon.rotation = 45f
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right)
                }
                "medium" -> {
                    resizeIcon.rotation = 0f
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right)
                }
                "big" -> {
                    resizeIcon.rotation = 45f
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up)
                }
                else -> {
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right)
                }
            }
            resizeIcon.setImageDrawable(arrow)
            resize.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    when (item.tileSize) {
                        "small" -> {
                            item.tileSize = "medium"
                            appsDbCall!!.updateApp(item)
                            holder.mTextView.post {
                                holder.mTextView.text = item.appLabel
                            }
                        }
                        "medium" -> {
                            item.tileSize = "big"
                            appsDbCall!!.updateApp(item)
                            holder.mTextView.post {
                                holder.mTextView.text = item.appLabel
                            }
                        }
                        "big" -> {
                            item.tileSize = "small"
                            appsDbCall!!.updateApp(item)
                        }
                    }
                    hashCache[item.appPackage] = recompressIcon(getAppIcon(item.appPackage, item.tileSize, requireContext().packageManager, resources), 80)
                    withContext(Dispatchers.Main) {
                        popupWindow.dismiss()
                        holder.mAppIcon.setImageIcon(hashCache[item.appPackage])
                        notifyItemChanged(position)
                    }
                }
            }
            remove.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    destroyTile(item)
                    withContext(Dispatchers.Main) {
                        refreshData(list)
                    }
                }
                popupWindow.dismiss()
            }
            settings.setOnClickListener {
                showSettingsBottomSheet(item, position)
                popupWindow.dismiss()
            }
        }

        private fun clearItems() {
            lifecycleScope.launch(Dispatchers.IO) {
                for (itemList in list) {
                    itemList.isSelected = false
                    appsDbCall!!.insertItem(itemList)
                }
            }
        }

        fun showSettingsBottomSheet(item: AppEntity, position: Int) {
            val bottomsheet = BottomSheetDialog(context)
            bottomsheet.setContentView(R.layout.tile_bottomsheet)
            bottomsheet.dismissWithAnimation = true
            val bottomSheetInternal = bottomsheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View?>(bottomSheetInternal!!).peekHeight = context.resources.getDimensionPixelSize(R.dimen.bottom_sheet_size)
            val label = bottomSheetInternal.findViewById<TextView>(R.id.appLabelSheet)
            val colorSub = bottomSheetInternal.findViewById<TextView>(R.id.chooseColorSub)
            val removeColor = bottomSheetInternal.findViewById<TextView>(R.id.chooseColorRemove)
            val uninstall = bottomSheetInternal.findViewById<MaterialCardView>(R.id.uninstallApp)
            val changeLabel = bottomSheetInternal.findViewById<MaterialCardView>(R.id.editAppLabel)
            val changeColor = bottomSheetInternal.findViewById<MaterialCardView>(R.id.editTileColor)
            val editor = bottomSheetInternal.findViewById<EditText>(R.id.textEdit)
            val labellayout = bottomSheetInternal.findViewById<LinearLayout>(R.id.changeLabelLayout)
            val labelChangeBtn = bottomSheetInternal.findViewById<MaterialCardView>(R.id.labelChange)
            val editLabelText = bottomSheetInternal.findViewById<TextView>(R.id.editAppLabelText)
            val appInfo = bottomSheetInternal.findViewById<MaterialCardView>(R.id.appInfo)
            editLabelText.setOnClickListener {
                labellayout.visibility = View.VISIBLE
            }
            val originalLabel = context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(item.appPackage, 0)).toString()
            label.text = item.appLabel
            editor.setText(item.appLabel)
            editor.hint = originalLabel
            if(item.tileColor == -1) {
                colorSub.visibility = View.GONE
                removeColor.visibility = View.GONE
            } else {
                colorSub.setTextColor(getTileColorFromPrefs(item.tileColor!!, context))
                colorSub.text = getString(R.string.tileSettings_color_sub, getTileColorName(item.tileColor!!, context))
            }
            changeLabel.setOnClickListener {
                labellayout.visibility = View.VISIBLE
            }
            labelChangeBtn.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    if(editor.text.toString() == "") {
                        item.appLabel = originalLabel
                    } else {
                        item.appLabel = editor.text.toString()
                    }
                    appsDbCall!!.updateApp(item)
                    withContext(Dispatchers.Main) {
                        bottomsheet.dismiss()
                        notifyItemRemoved(position)
                    }
                }
            }
            removeColor.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = -1
                    appsDbCall!!.updateApp(item)
                    withContext(Dispatchers.Main) {
                        notifyItemRemoved(position)
                    }
                }
                bottomsheet.dismiss()
            }
            changeColor.setOnClickListener {
                val dialog = AccentDialog()
                dialog.configure(item, appsDbCall!!, this)
                dialog.show(childFragmentManager, "accentDialog")
                bottomsheet.dismiss()
            }
            uninstall.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + item.appPackage)))
                bottomsheet.dismiss()
            }
            appInfo.setOnClickListener {
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + item.appPackage)))
                bottomsheet.dismiss()
            }
            bottomsheet.show()
        }
        inner class TileViewHolder(v: View) : RecyclerView.ViewHolder(v), ItemTouchHelperViewHolder {
            private val mCardContainer: MaterialCardView = v.findViewById(R.id.cardContainer)
            val mContainer: FrameLayout = v.findViewById(R.id.container)
            val mTextView: TextView = v.findViewById(android.R.id.text1)
            val mAppIcon: ImageView = v.findViewById(android.R.id.icon1)

            init {
                mContainer.alpha = PREFS!!.getTilesTransparency
                mCardContainer.apply {
                    strokeWidth = if (PREFS!!.isWallpaperUsed && !PREFS!!.isTilesTransparent) context.resources?.getDimensionPixelSize(R.dimen.tileStrokeWidthDisabled)!! else context.resources?.getDimensionPixelSize(R.dimen.tileStrokeWidth)!!
                }
                mContainer.setOnClickListener {
                    val item = list[absoluteAdapterPosition]
                    if (isEditMode) {
                        itemView.rotation = 0f
                        lifecycleScope.launch(Dispatchers.IO) {
                            item.isSelected = true
                            appsDbCall!!.insertItem(item)
                            withContext(Dispatchers.Main) {
                                showPopupWindow(this@TileViewHolder, item, absoluteAdapterPosition)
                                notifyItemChanged(absoluteAdapterPosition)
                            }
                        }
                    } else {
                        isAppOpened = true
                        startApp(item.appPackage)
                    }
                }
                mContainer.setOnLongClickListener {
                    if(!isEditMode) {
                        enableEditMode()
                    }
                    true
                }
            }
            private fun startApp(packageName: String) {
                when (packageName) {
                    "ru.dimon6018.metrolauncher" -> {
                        startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                    }
                    else -> {
                        val intent = context.packageManager!!.getLaunchIntentForPackage(packageName)
                        intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
            }
            override fun onItemSelected() {}
            override fun onItemClear() {}
        }
        inner class WeatherTileViewHolder(v: View) : RecyclerView.ViewHolder(v), ItemTouchHelperViewHolder {
            val mCardContainer: MaterialCardView = v.findViewById(R.id.cardContainer)
            val mContainer: FrameLayout = v.findViewById(R.id.container)
            val mTextViewAppTitle: TextView = v.findViewById(android.R.id.text1)
            val mTextViewTempValue: TextView = v.findViewById(R.id.weatherTempValue)
            val mTextViewValue: TextView = v.findViewById(R.id.weatherValue)
            val mTextViewCity: TextView = v.findViewById(android.R.id.text2)

            override fun onItemSelected() {}
            override fun onItemClear() {}
        }
        inner class SpaceViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            init {
                itemView.setOnClickListener {
                    if (isEditMode) {
                        disableEditMode()
                    }
                }
                if(PREFS!!.isWallpaperUsed) {
                    if(!PREFS!!.isTilesTransparent) {
                        transparentColor?.apply { itemView.setBackgroundColor(this) }
                    }
                }
            }
        }
    }
    class DiffUtilCallback(private val old: MutableList<AppEntity>, private val new: MutableList<AppEntity>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return old.size
        }

        override fun getNewListSize(): Int {
            return new.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]
            return oldItem.appPos == newItem.appPos
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]
            return oldItem.id == newItem.id
        }
    }
    class AccentDialog : DialogFragment() {

        private lateinit var item: AppEntity
        private lateinit var dbCall: AppDao
        private lateinit var adapter: NewStartAdapter

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
        }

        fun configure(i: AppEntity, c: AppDao, a: NewStartAdapter) {
            item = i
            dbCall = c
            adapter = a
        }

        override fun onStart() {
            super.onStart()
            val dialog = dialog
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog?.setTitle("TILE COLOR")
            dialog?.window!!.setLayout(width, height)
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            return inflater.inflate(R.layout.accent_dialog, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val lime = view.findViewById<ImageView>(R.id.choose_color_lime)
            val back = view.findViewById<FrameLayout>(R.id.back_accent_menu)
            back.setOnClickListener { dismiss() }
            lime.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 0
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val green = view.findViewById<ImageView>(R.id.choose_color_green)
            green.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 1
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val emerald = view.findViewById<ImageView>(R.id.choose_color_emerald)
            emerald.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 2
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val cyan = view.findViewById<ImageView>(R.id.choose_color_cyan)
            cyan.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 3
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val teal = view.findViewById<ImageView>(R.id.choose_color_teal)
            teal.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 4
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val cobalt = view.findViewById<ImageView>(R.id.choose_color_cobalt)
            cobalt.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 5
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val indigo = view.findViewById<ImageView>(R.id.choose_color_indigo)
            indigo.setOnClickListener {
                 lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 6
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val violet = view.findViewById<ImageView>(R.id.choose_color_violet)
            violet.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 7
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val pink = view.findViewById<ImageView>(R.id.choose_color_pink)
            pink.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 8
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val magenta = view.findViewById<ImageView>(R.id.choose_color_magenta)
            magenta.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 9
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val crimson = view.findViewById<ImageView>(R.id.choose_color_crimson)
            crimson.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 10
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val red = view.findViewById<ImageView>(R.id.choose_color_red)
            red.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 11
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val orange = view.findViewById<ImageView>(R.id.choose_color_orange)
            orange.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 12
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val amber = view.findViewById<ImageView>(R.id.choose_color_amber)
            amber.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 13
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val yellow = view.findViewById<ImageView>(R.id.choose_color_yellow)
            yellow.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 14
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val brown = view.findViewById<ImageView>(R.id.choose_color_brown)
            brown.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 15
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val olive = view.findViewById<ImageView>(R.id.choose_color_olive)
            olive.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 16
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val steel = view.findViewById<ImageView>(R.id.choose_color_steel)
            steel.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 17
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val mauve = view.findViewById<ImageView>(R.id.choose_color_mauve)
            mauve.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 18
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val taupe = view.findViewById<ImageView>(R.id.choose_color_taupe)
            taupe.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    item.tileColor = 19
                    dbCall.updateApp(item)
                }
                dismiss()
            }
        }

        override fun dismiss() {
            adapter.notifyItemChanged(item.appPos!!)
            adapter.showSettingsBottomSheet(item, item.appPos!!)
            super.dismiss()
        }
    }
}
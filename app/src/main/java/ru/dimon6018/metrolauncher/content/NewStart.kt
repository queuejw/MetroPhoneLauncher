package ru.dimon6018.metrolauncher.content

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
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
import androidx.appcompat.content.res.AppCompatResources
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isAppOpened
import ru.dimon6018.metrolauncher.Application.Companion.isStartMenuOpened
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.apps.AppDao
import ru.dimon6018.metrolauncher.content.data.apps.AppData
import ru.dimon6018.metrolauncher.content.data.apps.AppEntity
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
import java.util.Collections
import kotlin.random.Random


class NewStart: Fragment(), OnStartDragListener {

    private var mRecyclerView: RecyclerView? = null
    private var mSpannedLayoutManager: SpannedGridLayoutManager? = null
    private var mAdapter: NewStartAdapter? = null
    private var tiles: MutableList<AppEntity>? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var appsDbCall: AppDao? = null

    private lateinit var frame: ConstraintLayout

    private var allAppsButton: MaterialCardView? = null
    private var currentActivity: Activity? = null

    private var packageBroadcastReceiver: BroadcastReceiver? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currentActivity = activity
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.start_screen, container, false)
        mRecyclerView = v.findViewById(R.id.start_apps_tiles)
        frame = v.findViewById(R.id.startFrame)
        CoroutineScope(Dispatchers.Default).launch {
            appsDbCall = AppData.getAppData(requireContext()).getAppDao()
            tiles = appsDbCall!!.getJustApps()
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
            currentActivity?.runOnUiThread {
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
            }
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerBroadcast()
    }
    private fun observe() {
        appsDbCall?.getApps()?.asLiveData()?.observe(viewLifecycleOwner) {
            if (!mAdapter?.isEditMode!! && mAdapter?.list != it) {
                Log.d("flow", "update list")
                mAdapter?.setData(it)
            }
        }
    }
    private fun stopObserver() {
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
        stopObserver()
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
                if (action == PackageChangesReceiver.PACKAGE_REMOVED) {
                    packageName?.apply { broadcastListUpdater(packageName, true) }
                }
                if (action == PackageChangesReceiver.PACKAGE_INSTALLED) {
                    if (PREFS!!.pinNewApps) {
                        if (packageName != null) {
                            pinApp(packageName)
                        }
                    }
                }
                if (action == PackageChangesReceiver.PACKAGE_UPDATED) {
                    packageName?.apply {
                        broadcastListUpdater(packageName, false)
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
                            it.tileType = -1
                            it.tileSize = "small"
                            it.appPackage = ""
                            it.tileColor = -1
                            it.appLabel = ""
                            it.id = it.id!! / 2
                            appsDbCall!!.updateApp(it)
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
            runBlocking {
                activity?.runOnUiThread {
                    mAdapter?.setData(newData)
                }
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
    inner class NewStartAdapter(val context: Context, var list: MutableList<AppEntity>): RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

        private val defaultTileType: Int = 0
        val spaceType: Int = 1

        var isEditMode = false
        private val animList: MutableList<ObjectAnimator> = ArrayList()
        private var iconManager: IconPackManager? = null
        private var transparentColor: Int? = null

        init {
            setHasStableIds(true)
            if (PREFS!!.iconPackPackage != "") {
                iconManager = IconPackManager()
                iconManager!!.setContext(context)
            }
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
            return if(viewType == defaultTileType) {
                val v = inflater.inflate(R.layout.tile, parent, false)
                TileViewHolder(v)
            } else {
                val v = inflater.inflate(R.layout.space, parent, false)
                SpaceViewHolder(v)
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
                spaceType -> {
                    if(PREFS!!.isWallpaperUsed) {
                        if(!PREFS!!.isTilesTransparent) {
                            transparentColor?.apply { holder.itemView.setBackgroundColor(this) }
                        }
                    }
                }
                defaultTileType -> bindDefaultTile(holder as TileViewHolder, position)
            }
        }
        private fun bindDefaultTile(holder: TileViewHolder, position: Int) {
            val item = list[position]
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
            when (item.tileSize) {
                "small" -> {
                    holder.mTextView.text = ""
                }
                "medium" -> {
                    if (PREFS!!.isMoreTilesEnabled) {
                        holder.mTextView.text = ""
                    } else {
                        holder.mTextView.text = item.appLabel
                    }
                }
                "big" -> {
                    holder.mTextView.text = item.appLabel
                }
            }
            if (PREFS!!.isWallpaperUsed && !PREFS!!.isTilesTransparent) {
                holder.mCardContainer.strokeWidth = context.resources?.getDimensionPixelSize(R.dimen.tileStrokeWidthDisabled)!!
            } else {
                holder.mCardContainer.strokeWidth = context.resources?.getDimensionPixelSize(R.dimen.tileStrokeWidth)!!
            }
            holder.mContainer.alpha = PREFS!!.getTilesTransparency
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
            try {
                holder.mAppIcon.setImageBitmap(getAppIcon(item.appPackage, item.tileSize, context.packageManager))
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("Start Adapter", e.toString())
                holder.mAppIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_close))
                CoroutineScope(Dispatchers.IO).launch {
                    appsDbCall!!.removeApp(item)
                    val updatedList = appsDbCall!!.getJustApps()
                    runBlocking {
                        activity?.runOnUiThread {
                            setData(updatedList)
                        }
                    }
                }
            } catch (e: Resources.NotFoundException) {
                Log.e("Start Adapter", e.toString())
                holder.mAppIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_alert))
                PREFS!!.setIconPack("null")
            } catch (e: NullPointerException) {
                Log.e("Start Adapter", e.toString())
                holder.mAppIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_alert))
                PREFS!!.setIconPack("null")
            }
        }
        private fun getAppIcon(appPackage: String, size: String, pm: PackageManager): Bitmap {
            val drawable = if(PREFS!!.iconPackPackage == "null") pm.getApplicationIcon(appPackage) else iconManager?.getIconPackWithName(PREFS!!.iconPackPackage)?.getDrawableIconForPackage(appPackage, pm.getApplicationIcon(appPackage))
            val bmp: Bitmap
            when (size) {
                "small" -> {
                    bmp = if (PREFS!!.isMoreTilesEnabled) {
                        drawable!!.toBitmap(resources.getDimensionPixelSize(R.dimen.tile_small_moreTiles_on), resources.getDimensionPixelSize(R.dimen.tile_small_moreTiles_on))
                    } else {
                        drawable!!.toBitmap(resources.getDimensionPixelSize(R.dimen.tile_small_moreTiles_off), resources.getDimensionPixelSize(R.dimen.tile_small_moreTiles_off))
                    }
                }
                "medium" -> {
                    bmp = if (PREFS!!.isMoreTilesEnabled) {
                        drawable!!.toBitmap(resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_on), resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_on))
                    } else {
                        drawable!!.toBitmap(resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off))
                    }
                }
                "big" -> {
                    bmp = if (PREFS!!.isMoreTilesEnabled) {
                        drawable!!.toBitmap(resources.getDimensionPixelSize(R.dimen.tile_big_moreTiles_on), resources.getDimensionPixelSize(R.dimen.tile_big_moreTiles_on))
                    } else {
                        drawable!!.toBitmap(resources.getDimensionPixelSize(R.dimen.tile_big_moreTiles_off), resources.getDimensionPixelSize(R.dimen.tile_big_moreTiles_off))
                    }
                }
                else -> {
                    bmp = drawable!!.toBitmap(resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off))
                }
            }
            return bmp
        }
        override fun getItemViewType(position: Int): Int {
            return when(list[position].tileType) {
                -1 -> spaceType
                0 -> defaultTileType
                else -> defaultTileType
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
            if(!isEditMode) {
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
                val itemsReserved = list
                for (i in 0 until itemsReserved.size) {
                    val item = itemsReserved[i]
                    item.appPos = i
                    appsDbCall?.insertItem(item)
                }
                runBlocking {
                    val updatedList = appsDbCall!!.getJustApps()
                    if (isEditMode) {
                        currentActivity?.runOnUiThread {
                            refreshData(updatedList)
                        }
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
                CoroutineScope(Dispatchers.IO).launch {
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
                    runBlocking {
                        requireActivity().runOnUiThread {
                            popupWindow.dismiss()
                            notifyItemChanged(position)
                        }
                    }
                }
            }
            remove.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileType = -1
                    item.tileSize = "small"
                    item.appPackage = ""
                    item.tileColor = -1
                    item.appLabel = ""
                    item.id = item.id!! / 2
                    appsDbCall!!.insertItem(item)
                    runBlocking {
                        requireActivity().runOnUiThread {
                            refreshData(list)
                        }
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
            CoroutineScope(Dispatchers.IO).launch {
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
                CoroutineScope(Dispatchers.IO).launch {
                    if(editor.text.toString() == "") {
                        item.appLabel = originalLabel
                    } else {
                        item.appLabel = editor.text.toString()
                    }
                    appsDbCall!!.updateApp(item)
                    runBlocking {
                        requireActivity().runOnUiThread {
                            bottomsheet.dismiss()
                            notifyItemRemoved(position)
                        }
                    }
                }
            }
            removeColor.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = -1
                    appsDbCall!!.updateApp(item)
                    requireActivity().runOnUiThread {
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
            val mCardContainer: MaterialCardView = v.findViewById(R.id.cardContainer)
            val mContainer: FrameLayout = v.findViewById(R.id.container)
            val mTextView: TextView = v.findViewById(android.R.id.text1)
            val mAppIcon: ImageView = v.findViewById(android.R.id.icon1)

            init {
                mContainer.setOnClickListener {
                    val item = list[absoluteAdapterPosition]
                    if (isEditMode) {
                        itemView.rotation = 0f
                        CoroutineScope(Dispatchers.IO).launch {
                            item.isSelected = true
                            appsDbCall!!.insertItem(item)
                            runBlocking {
                                currentActivity?.runOnUiThread {
                                    showPopupWindow(this@TileViewHolder, item, absoluteAdapterPosition)
                                    notifyItemChanged(absoluteAdapterPosition)
                                }
                            }
                        }
                    } else {
                        val intent = context.packageManager!!.getLaunchIntentForPackage(item.appPackage)
                        intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        isAppOpened = true
                        context.startActivity(intent)
                    }
                }
                mContainer.setOnLongClickListener {
                    if(!isEditMode) {
                        enableEditMode()
                    }
                    true
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
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 0
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val green = view.findViewById<ImageView>(R.id.choose_color_green)
            green.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 1
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val emerald = view.findViewById<ImageView>(R.id.choose_color_emerald)
            emerald.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 2
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val cyan = view.findViewById<ImageView>(R.id.choose_color_cyan)
            cyan.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 3
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val teal = view.findViewById<ImageView>(R.id.choose_color_teal)
            teal.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 4
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val cobalt = view.findViewById<ImageView>(R.id.choose_color_cobalt)
            cobalt.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 5
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val indigo = view.findViewById<ImageView>(R.id.choose_color_indigo)
            indigo.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 6
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val violet = view.findViewById<ImageView>(R.id.choose_color_violet)
            violet.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 7
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val pink = view.findViewById<ImageView>(R.id.choose_color_pink)
            pink.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 8
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val magenta = view.findViewById<ImageView>(R.id.choose_color_magenta)
            magenta.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 9
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val crimson = view.findViewById<ImageView>(R.id.choose_color_crimson)
            crimson.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 10
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val red = view.findViewById<ImageView>(R.id.choose_color_red)
            red.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 11
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val orange = view.findViewById<ImageView>(R.id.choose_color_orange)
            orange.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 12
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val amber = view.findViewById<ImageView>(R.id.choose_color_amber)
            amber.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 13
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val yellow = view.findViewById<ImageView>(R.id.choose_color_yellow)
            yellow.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 14
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val brown = view.findViewById<ImageView>(R.id.choose_color_brown)
            brown.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 15
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val olive = view.findViewById<ImageView>(R.id.choose_color_olive)
            olive.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 16
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val steel = view.findViewById<ImageView>(R.id.choose_color_steel)
            steel.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 17
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val mauve = view.findViewById<ImageView>(R.id.choose_color_mauve)
            mauve.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 18
                    dbCall.updateApp(item)
                }
                dismiss()
            }
            val taupe = view.findViewById<ImageView>(R.id.choose_color_taupe)
            taupe.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
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
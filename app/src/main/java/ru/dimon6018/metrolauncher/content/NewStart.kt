package ru.dimon6018.metrolauncher.content

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isAppOpened
import ru.dimon6018.metrolauncher.Application.Companion.isStartMenuOpened
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.Main.Companion.isLandscape
import ru.dimon6018.metrolauncher.MainViewModel
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.app.App
import ru.dimon6018.metrolauncher.content.data.tile.Tile
import ru.dimon6018.metrolauncher.content.data.tile.TileDao
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.databinding.SpaceBinding
import ru.dimon6018.metrolauncher.databinding.StartScreenBinding
import ru.dimon6018.metrolauncher.databinding.TileBinding
import ru.dimon6018.metrolauncher.helpers.ItemTouchCallback
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperAdapter
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperViewHolder
import ru.dimon6018.metrolauncher.helpers.OnStartDragListener
import ru.dimon6018.metrolauncher.helpers.receivers.PackageChangesReceiver
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.getTileColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.getTileColorName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.isScreenOn
import kotlin.random.Random

class NewStart: Fragment(), OnStartDragListener {

    private lateinit var mItemTouchHelper: ItemTouchHelper

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private var mSpannedLayoutManager: SpannedGridLayoutManager? = null
    private var mAdapter: NewStartAdapter? = null
    private var tiles: MutableList<Tile>? = null
    private var packageBroadcastReceiver: BroadcastReceiver? = null

    private var isBroadcasterRegistered = false
    private var startScreenReady = false
    private var screenIsOn = false

    private lateinit var mainViewModel: MainViewModel

    private var _binding: StartScreenBinding? = null
    private val binding get() = _binding!!

    private var lastItemPos: Int? = null

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            mAdapter?.let { if(it.isEditMode) it.disableEditMode() }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = StartScreenBinding.inflate(inflater, container, false)
        val view = binding.root
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        binding.startFrame.setOnClickListener {
            mAdapter?.let { if(it.isEditMode) it.disableEditMode() }
        }
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(context != null) {
            viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                tiles = mainViewModel.getTileDao().getTilesList()
                val userTiles = mainViewModel.getTileDao().getUserTiles()
                lastItemPos = if(userTiles.isNotEmpty()) userTiles.last().tilePosition!! + 6 else tiles!!.size
                setupRecyclerViewLayoutManager(requireContext())
                setupAdapter()
                withContext(mainDispatcher) {
                    configureRecyclerView()
                    observe()
                    registerBroadcast()
                    startScreenReady = true
                }
                cancel("done")
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setupAdapter() {
        mAdapter = NewStartAdapter(requireContext(), tiles!!)
        mItemTouchHelper = ItemTouchHelper(ItemTouchCallback(mAdapter!!))
    }
    private fun addCallback() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backCallback)
    }
    private fun removeCallback() {
        backCallback.remove()
    }
    private fun configureRecyclerView() {
        binding.startAppsTiles.apply {
            layoutManager = mSpannedLayoutManager
            adapter = mAdapter
            mItemTouchHelper.attachToRecyclerView(this)
            addOnScrollListener(ScrollListener())
        }
    }
    private fun setupRecyclerViewLayoutManager(context: Context?) {
        if(mSpannedLayoutManager != null) {
            mSpannedLayoutManager = null
        }
        if (!isLandscape) {
            // phone
            mSpannedLayoutManager = SpannedGridLayoutManager(
                orientation = RecyclerView.VERTICAL,
                rowCount = if(!PREFS.isMoreTilesEnabled) 8 else 12,
                columnCount = if(!PREFS.isMoreTilesEnabled) 4 else 6
            )
        } else {
            // Landscape orientation
            val tablet = context?.resources?.getBoolean(R.bool.isTablet) == true
            mSpannedLayoutManager = if (tablet) {
                // tablet
                SpannedGridLayoutManager(
                    orientation = RecyclerView.VERTICAL,
                    rowCount =  if(!PREFS.isMoreTilesEnabled) 2 else 3,
                    columnCount = if(!PREFS.isMoreTilesEnabled) 4 else 6
                )
            } else {
                // phone but landscape
                SpannedGridLayoutManager(
                    orientation = RecyclerView.VERTICAL,
                    rowCount = 3,
                    columnCount = if(!PREFS.isMoreTilesEnabled) 4 else 6
                )
            }
        }
        mSpannedLayoutManager?.apply {
            itemOrderIsStable = true
            spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
                when (tiles!![position].tileSize) {
                    "small" -> SpanSize(1, 1)
                    "medium" -> SpanSize(2, 2)
                    "big" -> SpanSize(4, 2)
                    else -> SpanSize(1, 1)
                }
            }
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        setupRecyclerViewLayoutManager(context)
        binding.startAppsTiles.apply {
            layoutManager = mSpannedLayoutManager
        }
    }
    private fun observe() {
        Log.d("Start", "start observer")
        if(!startScreenReady) {
            return
        }
        if(!mainViewModel.getTileDao().getTilesLiveData().hasObservers()) {
            mainViewModel.getTileDao().getTilesLiveData().observe(viewLifecycleOwner) {
                if (mAdapter != null) {
                    if (!mAdapter!!.isEditMode && mAdapter!!.list != it) {
                        Log.d("observer", "update list")
                        mAdapter!!.setData(it)
                    }
                }
            }
        }
    }
    override fun onResume() {
        if(!screenIsOn) {
            if(binding.startAppsTiles.visibility == View.INVISIBLE) {
                binding.startAppsTiles.visibility = View.VISIBLE
            }
        }
        if(isAppOpened) {
            viewLifecycleOwner.lifecycleScope.launch {
                animateTiles(false, null, null)
            }
            isAppOpened = false
        }
        isStartMenuOpened = true
        super.onResume()
        screenIsOn = isScreenOn(context)
        mAdapter?.apply {
            isBottomRight = false
            isBottomLeft = false
            isTopRight = false
            isTopLeft = false
        }
        observe()
    }
    override fun onPause() {
        super.onPause()
        screenIsOn = isScreenOn(context)
        if(!screenIsOn) {
            if(binding.startAppsTiles.visibility == View.VISIBLE) {
                binding.startAppsTiles.visibility = View.INVISIBLE
            }
        }
        mAdapter?.apply {
            if(isEditMode) disableEditMode()
        }
        isStartMenuOpened = false
    }
    override fun onStop() {
        super.onStop()
        isStartMenuOpened = false
    }
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null && !PREFS.isStartBlocked && mAdapter != null) {
            if(!mAdapter!!.isEditMode) {
                mAdapter!!.enableEditMode()
            }
            mItemTouchHelper.startDrag(viewHolder)
        }
    }
    @SuppressLint("InlinedApi", "UnspecifiedRegisterReceiverFlag")
    private fun registerBroadcast() {
        Log.d("Start", "reg broadcaster")
        if(!isBroadcasterRegistered) {
            isBroadcasterRegistered = true
            packageBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val packageName = intent.getStringExtra("package")
                    // End early if it has anything to do with us.
                    if (packageName.isNullOrEmpty()) return
                    val action = intent.getIntExtra("action", 42)
                    when (action) {
                        PackageChangesReceiver.PACKAGE_INSTALLED -> {
                            Log.d("Start", "pkg installed")
                            val bool = PREFS.iconPackPackage != "null"
                            (requireActivity() as Main).generateIcon(packageName, bool)
                            mainViewModel.addAppToList(
                                App(
                                    appLabel = context.packageManager.getApplicationInfo(packageName, 0).name,
                                    appPackage = packageName,
                                    id = Random.nextInt()
                                ))
                            if (PREFS.pinNewApps) {
                                pinApp(packageName)
                            }
                        }
                        PackageChangesReceiver.PACKAGE_REMOVED -> {
                            packageName.apply { broadcastListUpdater(this, true) }
                        }
                        else -> {
                            packageName.apply { broadcastListUpdater(this, false) }
                        }
                    }
                }
            }
            IntentFilter().apply {
                addAction("ru.dimon6018.metrolauncher.PACKAGE_CHANGE_BROADCAST")
            }.also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireActivity().registerReceiver(
                        packageBroadcastReceiver,
                        it,
                        Context.RECEIVER_EXPORTED
                    )
                } else {
                    requireActivity().registerReceiver(packageBroadcastReceiver, it)
                }
            }
        }  else {
            Log.d("Start", "broadcaster already registered")
        }
    }
    private fun broadcastListUpdater(packageName: String, isDelete: Boolean) {
        packageName.apply {
            Log.d("Start", "update list by broadcaster")
            CoroutineScope(ioDispatcher).launch {
                var newList = mainViewModel.getTileDao().getTilesList()
                if(isDelete) {
                    newList.forEach {
                        if(it.tilePackage == packageName) {
                            Log.d("Start", "delete")
                            destroyTile(it)
                        }
                    }
                    newList = mainViewModel.getTileDao().getTilesList()
                }
                withContext(mainDispatcher) {
                    mAdapter?.setData(newList)
                }
            }
        }
    }
    private fun pinApp(packageName: String) {
        lifecycleScope.launch(ioDispatcher) {
            if(mAdapter != null) {
                var pos = 0
                for (i in 0..<mAdapter!!.list.size) {
                    if (mAdapter!!.list[i].tileType == -1) {
                        pos = i
                        break
                    }
                }
                val id = Random.nextLong(1000, 2000000)
                val item = Tile(
                    pos, id, -1, 0,
                    isSelected = false,
                    tileSize = Utils.generateRandomTileSize(true),
                    tileLabel = activity?.packageManager?.getApplicationInfo(packageName, 0)?.loadLabel(requireActivity().packageManager!!).toString(),
                    tilePackage = packageName
                )
                mainViewModel.getTileDao().addTile(item)
                val newDataList = mainViewModel.getTileDao().getTilesList()
                withContext(mainDispatcher) {
                    mAdapter!!.setData(newDataList)
                }
            }
        }
    }
    private fun unregisterBroadcast() {
        isBroadcasterRegistered = false
        packageBroadcastReceiver?.apply {
            requireActivity().unregisterReceiver(packageBroadcastReceiver)
            packageBroadcastReceiver = null
        } ?: run {
            Log.d("Start", "unregisterBroadcast() was called to a null receiver.")
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }
    private suspend fun destroyTile(tile: Tile) {
        tile.apply {
            tileType = -1
            tileSize = "small"
            tilePackage = ""
            tileColor = -1
            tileLabel = ""
            id = this.id!! / 2
            mainViewModel.getTileDao().updateTile(this)
        }
    }
    private suspend fun animateTiles(launchApp: Boolean, launchAppPos: Int?, packageName: String?) {
        mAdapter ?: return
        mSpannedLayoutManager ?: return
        if(launchApp && launchAppPos != null && packageName != null) {
            enterToAppAnim(launchAppPos, packageName)
        } else if(!launchApp) {
            exitFromAppAnim()
        }
    }
    private suspend fun enterToAppAnim(position: Int, packageName: String) {
        binding.startAppsTiles.isScrollEnabled = false
        val first = mSpannedLayoutManager!!.firstVisiblePosition
        val last = mSpannedLayoutManager!!.lastVisiblePosition
        val interpolator = AccelerateInterpolator()
        var duration = 175L
        for(i in last downTo first) {
            if(tiles!![i] == tiles!![position]) continue
            val holder = binding.startAppsTiles.findViewHolderForAdapterPosition(i) ?: continue
            if(holder.itemViewType == mAdapter!!.spaceType) continue
            delay(5)
            duration += 10L
            holder.itemView.animate().rotationY(-110f).translationX(-1000f).translationY(-100f).setInterpolator(interpolator).setDuration(duration).start()
        }
        delay(250)
        duration = 150L
        for(i in last downTo first) {
            if(tiles!![i] != tiles!![position]) continue
            val holder = binding.startAppsTiles.findViewHolderForAdapterPosition(i) ?: continue
            holder.itemView.animate().rotationY(-90f).translationX(-1000f).translationY(-100f).setInterpolator(interpolator).setDuration(duration).withEndAction {
                binding.startAppsTiles.isScrollEnabled = true
                startApp(packageName)
            }
        }
    }
    private suspend fun exitFromAppAnim() {
        val first = mSpannedLayoutManager!!.firstVisiblePosition
        val last = mSpannedLayoutManager!!.lastVisiblePosition
        var duration = 300L
        for(i in last downTo first) {
            val holder = binding.startAppsTiles.findViewHolderForAdapterPosition(i) ?: continue
            delay(5)
            duration += 5L
            holder.itemView.animate().rotationY(0f).translationX(0f).translationY(0f).setInterpolator(AccelerateInterpolator()).setDuration(duration).start()
        }
    }
    private fun startApp(packageName: String) {
        isAppOpened = true
        if (activity != null) {
            val intent = when (packageName) {
                "ru.dimon6018.metrolauncher" -> Intent(requireActivity(), SettingsActivity::class.java)
                else -> requireActivity().packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            intent?.let { startActivity(it) }
        }
    }
    inner class ScrollListener(): RecyclerView.OnScrollListener() {

        var lastDy = 0

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (lastItemPos != null && mSpannedLayoutManager != null && mAdapter != null) {
                if (!mAdapter!!.isEditMode) {
                    if (mSpannedLayoutManager!!.lastVisiblePosition >= lastItemPos!! && dy > 0) {
                        recyclerView.scrollBy(0, -dy)
                    } else {
                        lastDy = dy
                    }
                }
            }
        }
    }
    inner class NewStartAdapter(val context: Context, var list: MutableList<Tile>): RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

        val defaultTileType: Int = 0
        val spaceType: Int = -1

        private val accentColor: Int by lazy { Utils.launcherAccentColor(requireActivity().theme) }

        var isEditMode = false
        var isTopLeft = false
        var isTopRight = false
        var isBottomLeft = false
        var isBottomRight = false

        init {
            setHasStableIds(true)
        }
        fun setData(newData: MutableList<Tile>) {
            val diffUtilCallback = DiffUtilCallback(list, newData)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback, false)
            diffResult.dispatchUpdatesTo(this)
            list = newData
            tiles = newData
        }
        private fun refreshData(newData: MutableList<Tile>) {
            val diffUtilCallback = DiffUtilCallback(list, newData)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback, false)
            diffResult.dispatchUpdatesTo(this)
        }
        fun enableEditMode() {
            Log.d("EditMode", "enter edit mode")
            (requireActivity() as Main).configureViewPagerScroll(false)
            addCallback()
            binding.startAppsTiles.animate().scaleX(0.9f).scaleY(0.9f).setDuration(300).start()
            isEditMode = true
            animateEditMode()
        }
        fun disableEditMode() {
            Log.d("EditMode", "exit edit mode")
            removeCallback()
            (requireActivity() as Main).configureViewPagerScroll(true)
            binding.startAppsTiles.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            isEditMode = false
            stopEditModeAnimation()
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when(viewType) {
                defaultTileType -> TileViewHolder(TileBinding.inflate(inflater, parent, false))
                spaceType -> SpaceViewHolder(SpaceBinding.inflate(inflater, parent, false))
                else -> SpaceViewHolder(SpaceBinding.inflate(inflater, parent, false))
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun getItemId(position: Int): Long {
            return list[position].id!!
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when(holder.itemViewType) {
                defaultTileType -> bindDefaultTile(holder as TileViewHolder, list[position])
            }
        }
        private fun bindDefaultTile(holder: TileViewHolder, item: Tile) {
            setTileSize(item, holder.binding.tileLabel)
            setTileIconSize(holder.binding.tileIcon, item.tileSize, context.resources)
            setTileColor(holder, item)
            setTileIcon(holder.binding.tileIcon, item)
        }
        private fun setTileIcon(imageView: ImageView, item: Tile) {
            try {
                imageView.load(mainViewModel.getIconFromCache(item.tilePackage))
            } catch (e: Exception) {
                Log.e("Adapter", e.toString())
                lifecycleScope.launch(ioDispatcher) {
                    destroyTile(item)
                }
            }
        }
        private fun setTileIconSize(imageView: ImageView, tileSize: String, res: Resources) {
            imageView.layoutParams.apply {
                when (tileSize) {
                    "small" -> {
                        val size = if (PREFS.isMoreTilesEnabled) res.getDimensionPixelSize(R.dimen.tile_small_moreTiles_on) else res.getDimensionPixelSize(
                            R.dimen.tile_small_moreTiles_off
                        )
                        width = size
                        height = size
                    }
                    "medium" -> {
                        val size = if (PREFS.isMoreTilesEnabled) res.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_on) else res.getDimensionPixelSize(
                            R.dimen.tile_medium_moreTiles_off
                        )
                        width = size
                        height = size
                    }
                    "big" -> {
                        val size = if (PREFS.isMoreTilesEnabled) res.getDimensionPixelSize(R.dimen.tile_big_moreTiles_on) else res.getDimensionPixelSize(
                            R.dimen.tile_big_moreTiles_off
                        )
                        width = size
                        height = size
                    }
                    else -> {
                        width = res.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off)
                        height = res.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off)
                    }
                }
            }
        }
        private fun setTileColor(holder: TileViewHolder, item: Tile) {
            if (item.tileColor != -1) {
                holder.binding.container.setBackgroundColor(
                    getTileColorFromPrefs(
                        item.tileColor!!,
                        context
                    )
                )
            } else {
                holder.binding.container.setBackgroundColor(accentColorFromPrefs(context))
            }
        }
        private fun setTileSize(item: Tile, mTextView: MaterialTextView) {
            when (item.tileSize) {
                "small" -> {
                    mTextView.text = null
                }
                "medium" -> {
                    if (PREFS.isMoreTilesEnabled) {
                        mTextView.text = null
                    } else {
                        mTextView.text = item.tileLabel
                    }
                }
                "big" -> {
                    mTextView.text = item.tileLabel
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
                enableEditMode()
                return
            }
            if (fromPosition == toPosition) return
            Log.d("ItemMove", "from pos: $fromPosition")
            Log.d("ItemMove", "to pos: $toPosition")
            list.add(toPosition, list.removeAt(fromPosition))
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
            lifecycleScope.launch(defaultDispatcher) {
                for (i in 0 until list.size) {
                    val item = list[i]
                    item.tilePosition = i
                    mainViewModel.getTileDao().updateTile(item)
                }
                val updatedList = mainViewModel.getTileDao().getTilesList()
                val userTiles = mainViewModel.getTileDao().getUserTiles()
                lastItemPos = if(userTiles.isNotEmpty()) userTiles.last().tilePosition!! + 6 else updatedList.size
                withContext(mainDispatcher) {
                    if (isEditMode) {
                        refreshData(updatedList)
                    }
                }
            }
        }
        private fun showPopupWindow(holder: TileViewHolder, item: Tile, position: Int) {
            holder.itemView.clearAnimation()
            holder.itemView.animate().translationX(0f).translationY(0f).start()
            binding.startAppsTiles.isScrollEnabled = false
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(if(item.tileSize == "small" && PREFS.isMoreTilesEnabled) R.layout.tile_window_small else R.layout.tile_window, holder.itemView as ViewGroup, false)
            val width = holder.itemView.width
            val height = holder.itemView.height
            val popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow.animationStyle = R.style.enterStyle
            val resize = popupView.findViewById<MaterialCardView>(R.id.resize)
            val resizeIcon = popupView.findViewById<ImageView>(R.id.resizeIco)
            val settings = popupView.findViewById<MaterialCardView>(R.id.settings)
            val remove = popupView.findViewById<MaterialCardView>(R.id.remove)
            popupWindow.setOnDismissListener {
                binding.startAppsTiles.isScrollEnabled = true
                editModeAnimate(holder.itemView)
            }
            popupWindow.showAsDropDown(holder.itemView, 0, -height, Gravity.CENTER)
            resizeIcon.apply {
                when(item.tileSize) {
                    "small" -> {
                        rotation = 45f
                        setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right))
                    }
                    "medium" -> {
                        rotation = 0f
                        setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right))
                    }
                    "big" -> {
                        rotation = 45f
                        setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up))
                    }
                    else -> {
                        setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_right))
                    }
                }
            }
            resize.setOnClickListener {
                lifecycleScope.launch(ioDispatcher) {
                    when (item.tileSize) {
                        "small" -> {
                            item.tileSize = "medium"
                            withContext(mainDispatcher) {
                                holder.binding.tileLabel.text = item.tileLabel
                            }
                        }
                        "medium" -> {
                            item.tileSize = "big"
                            withContext(mainDispatcher) {
                                holder.binding.tileLabel.text = item.tileLabel
                            }
                        }
                        "big" -> {
                            item.tileSize = "small"
                        }
                    }
                    mainViewModel.getTileDao().updateTile(item)
                    withContext(mainDispatcher) {
                        popupWindow.dismiss()
                        notifyItemChanged(position)
                    }
                }
            }
            remove.setOnClickListener {
                lifecycleScope.launch(ioDispatcher) {
                    destroyTile(item)
                    withContext(mainDispatcher) {
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
        fun showSettingsBottomSheet(item: Tile, position: Int) {
            val bottomsheet = BottomSheetDialog(context)
            bottomsheet.setContentView(R.layout.tile_bottomsheet)
            bottomsheet.dismissWithAnimation = true
            val bottomSheetInternal = bottomsheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View?>(bottomSheetInternal!!).peekHeight = context.resources.getDimensionPixelSize(R.dimen.bottom_sheet_size)
            configureBottomSheet(bottomSheetInternal, bottomsheet, item, position)
            bottomsheet.show()
        }
        private fun configureBottomSheet(bottomSheetInternal: View, bottomsheet: BottomSheetDialog, item: Tile, position: Int) {
            val label = bottomSheetInternal.findViewById<MaterialTextView>(R.id.appLabelSheet)
            val colorSub = bottomSheetInternal.findViewById<MaterialTextView>(R.id.chooseColorSub)
            val removeColor = bottomSheetInternal.findViewById<MaterialTextView>(R.id.chooseColorRemove)
            val uninstall = bottomSheetInternal.findViewById<MaterialCardView>(R.id.uninstallApp)
            val changeLabel = bottomSheetInternal.findViewById<MaterialCardView>(R.id.editAppLabel)
            val changeColor = bottomSheetInternal.findViewById<MaterialCardView>(R.id.editTileColor)
            val editor = bottomSheetInternal.findViewById<EditText>(R.id.textEdit)
            val labelLayout = bottomSheetInternal.findViewById<LinearLayout>(R.id.changeLabelLayout)
            val labelChangeBtn = bottomSheetInternal.findViewById<MaterialCardView>(R.id.labelChange)
            val editLabelText = bottomSheetInternal.findViewById<MaterialTextView>(R.id.editAppLabelText)
            val appInfo = bottomSheetInternal.findViewById<MaterialCardView>(R.id.appInfo)
            editLabelText.setOnClickListener {
                labelLayout.visibility = View.VISIBLE
            }
            val originalLabel = context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(item.tilePackage, 0)).toString()
            label.text = item.tileLabel
            editor.setText(item.tileLabel)
            editor.hint = originalLabel
            if(item.tileColor == -1) {
                colorSub.visibility = View.GONE
                removeColor.visibility = View.GONE
            } else {
                colorSub.setTextColor(getTileColorFromPrefs(item.tileColor!!, context))
                colorSub.text = getString(R.string.tileSettings_color_sub, getTileColorName(item.tileColor!!, context))
            }
            changeLabel.setOnClickListener {
                labelLayout.visibility = View.VISIBLE
            }
            labelChangeBtn.setOnClickListener {
                lifecycleScope.launch(ioDispatcher) {
                    if(editor.text.toString() == "") {
                        item.tileLabel = originalLabel
                    } else {
                        item.tileLabel = editor.text.toString()
                    }
                    mainViewModel.getTileDao().updateTile(item)
                    withContext(mainDispatcher) {
                        bottomsheet.dismiss()
                        notifyItemRemoved(position)
                    }
                }
            }
            removeColor.setOnClickListener {
                lifecycleScope.launch(ioDispatcher) {
                    item.tileColor = -1
                    mainViewModel.getTileDao().updateTile(item)
                    withContext(mainDispatcher) {
                        notifyItemRemoved(position)
                    }
                }
                bottomsheet.dismiss()
            }
            changeColor.setOnClickListener {
                val dialog = AccentDialog()
                dialog.configure(item, this@NewStartAdapter, mainViewModel.getTileDao())
                dialog.show(childFragmentManager, "accentDialog")
                bottomsheet.dismiss()
            }
            uninstall.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + item.tilePackage)))
                bottomsheet.dismiss()
            }
            appInfo.setOnClickListener {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + item.tilePackage)))
                bottomsheet.dismiss()
            }
        }
        private fun animateEditMode() {
            for (i in 0..itemCount) {
                val holder = binding.startAppsTiles.findViewHolderForAdapterPosition(i) ?: continue
                if(holder.itemViewType == spaceType) continue
                editModeAnimate(holder.itemView)
            }
        }
        private fun editModeAnimate(view: View) {
            if (!isEditMode) {
                view.clearAnimation()
                return
            }
            val randomX = { Random.nextInt(-10, 10).toFloat() }
            val randomY = { Random.nextInt(-10, 10).toFloat() }
            val randomDuration = { Random.nextLong(500, 1000) }
            fun animateView() {
                view.animate()
                    .translationX(randomX())
                    .translationY(randomY())
                    .setDuration(randomDuration())
                    .withEndAction { animateView() }
                    .start()
            }
            animateView()
        }
        private fun stopEditModeAnimation() {
            for (i in 0..itemCount) {
                val holder = binding.startAppsTiles.findViewHolderForAdapterPosition(i) ?: continue
                if(holder.itemViewType == spaceType) continue
                holder.itemView.clearAnimation()
                holder.itemView.animate().translationY(0f).translationX(0f).setDuration(300).start()
            }
        }
        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            super.onViewAttachedToWindow(holder)
            if (isEditMode) {
                editModeAnimate(holder.itemView)
            }
        }
        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            super.onViewDetachedFromWindow(holder)
        }
        @SuppressLint("ClickableViewAccessibility")
        inner class TileViewHolder(val binding: TileBinding) : RecyclerView.ViewHolder(binding.root), ItemTouchHelperViewHolder {

            private val gestureDetector: GestureDetector =
                GestureDetector(context, object : SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent) {
                        handleLongClick()
                    }
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        handleClick()
                        return true
                    }
                })
            init {
                binding.cardContainer.apply {
                    if (PREFS.coloredStroke) strokeColor = accentColor
                    strokeWidth = context?.resources?.getDimensionPixelSize(R.dimen.tileStrokeWidth)!!
                }
                binding.container.apply {
                    alpha = PREFS.tilesTransparency
                    setOnTouchListener { view, event ->
                        val x = event.x
                        val y = event.y
                        val left = view.left
                        val top = view.top
                        val right = view.right
                        val bottom = view.bottom

                        isTopLeft =
                            x >= left && x <= left + view.width / 2 && y >= top && y <= top + view.height/ 2

                        isTopRight =
                            x >= (left + view.width / 2) && x <= right && y >= top && y <= top + view.height / 2

                        isBottomLeft =
                            x >= left && x <= left + view.width / 2 && y >= top + view.height / 2 && y <= bottom

                        isBottomRight =
                            x >= (left + view.width / 2) && x <= right && y >= top + view.height / 2 && y <= bottom

                        return@setOnTouchListener gestureDetector.onTouchEvent(event)
                    }
                }
            }
            private fun handleClick() {
                val item = list[absoluteAdapterPosition]
                if (isEditMode) {
                    showPopupWindow(this@TileViewHolder, item, absoluteAdapterPosition)
                } else {
                    if(PREFS.isTilesAnimEnabled) {
                        lifecycleScope.launch {
                            animateTiles(true, absoluteAdapterPosition, item.tilePackage)
                        }
                    } else {
                        startApp(item.tilePackage)
                    }
                }
            }
            private fun handleLongClick() {
                if(!isEditMode && !PREFS.isStartBlocked) enableEditMode()
            }
            override fun onItemSelected() {}
            override fun onItemClear() {}
        }
        /**inner class WeatherTileViewHolder(v: View) : RecyclerView.ViewHolder(v), ItemTouchHelperViewHolder {
        val mCardContainer: MaterialCardView = v.findViewById(R.id.cardContainer)
        val mContainer: FrameLayout = v.findViewById(R.id.container)
        val mTextViewAppTitle: TextView = v.findViewById(android.R.id.text1)
        val mTextViewTempValue: TextView = v.findViewById(R.id.weatherTempValue)
        val mTextViewValue: TextView = v.findViewById(R.id.weatherValue)
        val mTextViewCity: TextView = v.findViewById(android.R.id.text2)

        override fun onItemSelected() {}
        override fun onItemClear() {}
        }**/
        inner class SpaceViewHolder(binding: SpaceBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                itemView.setOnClickListener {
                    if (isEditMode) {
                        disableEditMode()
                    }
                }
            }
        }
    }
    class DiffUtilCallback(private val old: List<Tile>, private val new: List<Tile>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return old.size
        }
        override fun getNewListSize(): Int {
            return new.size
        }
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]
            return oldItem == newItem
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]
            return oldItem.tilePackage == newItem.tilePackage
        }
    }
    class AccentDialog : DialogFragment() {

        private lateinit var item: Tile
        private lateinit var dao: TileDao
        private lateinit var mAdapter: NewStartAdapter

        private val viewIds = arrayOf(
            R.id.choose_color_lime, R.id.choose_color_green, R.id.choose_color_emerald,
            R.id.choose_color_cyan, R.id.choose_color_teal, R.id.choose_color_cobalt,
            R.id.choose_color_indigo, R.id.choose_color_violet, R.id.choose_color_pink,
            R.id.choose_color_magenta, R.id.choose_color_crimson, R.id.choose_color_red,
            R.id.choose_color_orange, R.id.choose_color_amber, R.id.choose_color_yellow,
            R.id.choose_color_brown, R.id.choose_color_olive, R.id.choose_color_steel,
            R.id.choose_color_mauve, R.id.choose_color_taupe
        )

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
        }
        fun configure(i: Tile, a: NewStartAdapter, d: TileDao) {
            item = i
            dao = d
            mAdapter = a
        }
        override fun onStart() {
            super.onStart()
            dialog?.apply {
                window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setTitle("TILE COLOR")
            }
        }
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            return inflater.inflate(R.layout.accent_dialog, container, false)
        }
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val back = view.findViewById<FrameLayout>(R.id.back_accent_menu)
            back.setOnClickListener { dismiss() }
            for(i in 0..<viewIds.size) {
                setOnClick(view.findViewById<ImageView>(viewIds[i]), i)
            }
        }
        private fun setOnClick(colorView: View, value: Int) {
            colorView.setOnClickListener {
                updateTileColor(value)
            }
        }
        private fun updateTileColor(color: Int) {
            lifecycleScope.launch(Dispatchers.IO) {
                item.tileColor = color
                dao.updateTile(item)
                withContext(Dispatchers.Main) {
                    dismiss()
                }
            }
        }
        override fun dismiss() {
            mAdapter.notifyItemChanged(item.tilePosition!!)
            mAdapter.showSettingsBottomSheet(item, item.tilePosition!!)
            super.dismiss()
        }
    }
}
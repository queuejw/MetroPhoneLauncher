package ru.dimon6018.metrolauncher.content

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Region
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
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.app.ActivityCompat
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
import kotlinx.coroutines.runBlocking
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
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.accentColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.checkStoragePermissions
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.getTileColorFromPrefs
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.getTileColorName
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.isScreenOn
import kotlin.properties.Delegates
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = StartScreenBinding.inflate(inflater, container, false)
        val view = binding.root
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        if(PREFS.isWallpaperUsed && activity != null) {
            setWallpaper()
        }
        binding.startFrame.setOnClickListener {
            mAdapter?.apply {
                if(isEditMode) {
                    disableEditMode()
                }
            }
        }
        return view
    }
    private fun setWallpaper() {
        if(checkStoragePermissions(requireActivity())) {
            binding.backgroundWallpaper.setImageDrawable(
                WallpaperManager.getInstance(
                    requireActivity()
                ).fastDrawable
            )
        } else {
            wallpaperDialog()
        }
    }
    private fun wallpaperDialog() {
        binding.root.animate().alpha(0.5f).setDuration(500).start()
        WPDialog(requireActivity()).apply {
            setTitle(getString(R.string.reset_warning_title))
            setMessage(getString(R.string.wallpaper_error))
            setPositiveButton(getString(R.string.allow)) {
                getPermission()
                dismiss()
            }
            setNegativeButton(getString(R.string.deny)) {
                PREFS.isWallpaperUsed = false
                PREFS.isParallaxEnabled = false
                dismiss()
                requireActivity().recreate()
            }
            setCancelable(false)
            setDismissListener {
                binding.root.animate().alpha(1f).setDuration(500).start()
            }
            show()
        }
    }
    private fun getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).setData(Uri.parse(String.format("package:%s", requireActivity().packageName)))
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1507
            )
        }
        WPDialog(requireActivity()).apply {
            setTitle(getString(R.string.tip))
            setMessage(getString(R.string.restart_required))
            setPositiveButton(getString(android.R.string.ok)) {
                requireActivity().recreate()
                dismiss()
            }
            setNegativeButton(getString(android.R.string.cancel)) {
                dismiss()
            }
            setTopDialog(true)
            setCancelable(false)
            show()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(context != null) {
            viewLifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                tiles = mainViewModel.getTileDao().getTilesList()
                val userTiles = mainViewModel.getTileDao().getUserTiles()
                lastItemPos = if(userTiles.isNotEmpty()) userTiles.last().appPos!! + 6 else tiles!!.size
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

    private fun configureRecyclerView() {
        binding.startAppsTiles.apply {
            layoutManager = mSpannedLayoutManager
            adapter = mAdapter
            if (PREFS.isWallpaperUsed && !PREFS.isParallaxEnabled) {
                addItemDecoration(Utils.MarginItemDecoration(this.context.resources.getDimensionPixelSize(R.dimen.tileMargin)))
            }
            mItemTouchHelper.attachToRecyclerView(this)
            if(PREFS.isWallpaperUsed && checkStoragePermissions(requireActivity())) {
                binding.backgroundWallpaper.visibility = View.VISIBLE
                addOnScrollListener(ParallaxScroll(binding.backgroundWallpaper, mAdapter!!))
            }
            addItemDecoration(BackgroundDecoration())
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
    private fun animate() {
        if(mAdapter == null || !startScreenReady) {
            return
        }
        if (isAppOpened) {
            runAnim()
        } else if(!screenIsOn) {
            runAnim()
        }
    }
    private fun runAnim() {
        if(PREFS.isTilesAnimEnabled) {
            if (!mAdapter!!.isTopRight && !mAdapter!!.isTopLeft && !mAdapter!!.isBottomRight && !mAdapter!!.isBottomLeft) {
                mAdapter!!.isTopRight = true
            }
            setEnterAnim()
        } else {
            hideTiles(true)
        }
    }
    override fun onResume() {
        animate()
        if(!screenIsOn) {
            if(binding.startAppsTiles.visibility == View.INVISIBLE) {
                binding.startAppsTiles.visibility = View.VISIBLE
            }
        }
        if(isAppOpened) {
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
                hideTiles(false)
                binding.startAppsTiles.visibility = View.INVISIBLE
            }
        }
        if(mAdapter?.isEditMode == true) {
            mAdapter?.disableEditMode()
        }
        isStartMenuOpened = false
    }
    private fun setEnterAnim() {
        mAdapter ?: return
        val rotationY = when {
            mAdapter!!.isTopLeft || mAdapter!!.isBottomLeft -> -90f
            mAdapter!!.isBottomRight -> 90f
            else -> 0f
        }
        for (i in 0 until binding.startAppsTiles.childCount) {
            val holder = binding.startAppsTiles.findViewHolderForAdapterPosition(i) ?: continue
            if(holder.itemViewType == mAdapter!!.spaceType) continue
            val animatorSet = AnimatorSet()
            val rotation = when {
                mAdapter!!.isTopLeft || mAdapter!!.isBottomRight -> Random.nextInt(25, 45).toFloat()
                mAdapter!!.isTopRight || mAdapter!!.isBottomLeft -> Random.nextInt(-45, -25).toFloat()
                else -> 0f
            }
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(holder.itemView, "rotationY", rotationY, 0f),
                ObjectAnimator.ofFloat(holder.itemView, "rotation", rotation, 0f),
                ObjectAnimator.ofFloat(holder.itemView, "translationX", Random.nextInt(-500, -250).toFloat(), 0f),
                ObjectAnimator.ofFloat(holder.itemView, "alpha", 0f, 1f)
            )
            animatorSet.duration = Random.nextLong(250, 600)
            animatorSet.start()
        }
    }
    private fun hideTiles(showTiles: Boolean) {
        CoroutineScope(mainDispatcher).launch {
            if (mAdapter == null || !PREFS.isTilesAnimEnabled) {
                cancel()
                return@launch
            }
            for (i in 0..<binding.startAppsTiles.childCount) {
                val itemView = binding.startAppsTiles.findViewHolderForAdapterPosition(i)?.itemView ?: continue
                itemView.animate().alpha(if(showTiles) 1f else 0f).start()
            }
            cancel()
        }
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
        } else {
            return
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
                            packageName.apply {
                                broadcastListUpdater(this, true)
                            }
                        }
                        else -> {
                            packageName.apply {
                                broadcastListUpdater(this, false)
                            }
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
                        if(it.appPackage == packageName) {
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
            var dataList = mainViewModel.getTileDao().getTilesList()
            var pos = 0
            for (i in 0..<dataList.size) {
                if (dataList[i].tileType == -1) {
                    pos = i
                    break
                }
            }
            val id = Random.nextLong(1000, 2000000)
            val item = Tile(
                pos, id, -1, 0,
                isSelected = false,
                tileSize = Utils.generateRandomTileSize(true),
                appLabel = activity?.packageManager?.getApplicationInfo(packageName, 0)?.loadLabel(requireActivity().packageManager!!).toString(),
                appPackage = packageName
            )
            mainViewModel.getTileDao().addTile(item)
            dataList = mainViewModel.getTileDao().getTilesList()
            withContext(mainDispatcher) {
                mAdapter?.setData(dataList)
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
            appPackage = ""
            tileColor = -1
            appLabel = ""
            id = this.id!! / 2
            mainViewModel.getTileDao().updateTile(this)
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

        private val animList: MutableList<ObjectAnimator> = ArrayList()
        private var transparentColor by Delegates.notNull<Int>()

        var isEditMode = false
        var isTopLeft = false
        var isTopRight = false
        var isBottomLeft = false
        var isBottomRight = false

        init {
            setHasStableIds(true)
            transparentColor = ContextCompat.getColor(context, R.color.transparent)
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
            binding.startAppsTiles.animate().scaleX(0.9f).scaleY(0.9f).setDuration(300).start()
            binding.backgroundWallpaper.animate().scaleX(0.9f).scaleY(0.82f).setDuration(300).start()
            if(PREFS.isParallaxEnabled || !PREFS.isWallpaperUsed) {
                binding.startFrame.setBackgroundColor(ContextCompat.getColor(
                        context, if(PREFS.isLightThemeUsed) android.R.color.background_light else android.R.color.background_dark
                    )
                )
            }
            for(anim in animList) {
                anim.start()
            }
            isEditMode = true
        }
        fun disableEditMode() {
            Log.d("EditMode", "exit edit mode")
            (requireActivity() as Main).configureViewPagerScroll(true)
            binding.startAppsTiles.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            binding.backgroundWallpaper.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            if(PREFS.isParallaxEnabled || !PREFS.isWallpaperUsed) {
                binding.startFrame.background = null
            }
            isEditMode = false
            clearItems()
            for(anim in animList) {
                anim.cancel()
            }
            for (i in 0..<binding.startAppsTiles.childCount) {
                val holder = binding.startAppsTiles.findViewHolderForAdapterPosition(i) ?: continue
                if(holder.itemViewType == spaceType) continue
                holder.itemView.animate().rotation(0f).setDuration(100).start()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when(viewType) {
                defaultTileType -> {
                    val binding = TileBinding.inflate(inflater, parent, false)
                    TileViewHolder(binding)
                }
                spaceType -> {
                    val binding = SpaceBinding.inflate(inflater, parent, false)
                    SpaceViewHolder(binding)
                }
                else -> {
                    val binding = SpaceBinding.inflate(inflater, parent, false)
                    SpaceViewHolder(binding)
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
            animator.apply {
                setDuration(400)
                repeatMode = ValueAnimator.REVERSE
                repeatCount = ValueAnimator.INFINITE
                setPropertyName("rotation")
                target = v
            }
            return animator
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when(holder.itemViewType) {
                defaultTileType -> bindDefaultTile(holder as TileViewHolder, position, list[position])
            }
        }
        private fun startDismissTilesAnim(item: Tile) {
            for (position in 0 until binding.startAppsTiles.childCount) {
                val holder = binding.startAppsTiles.findViewHolderForAdapterPosition(position) ?: continue
                if(holder.itemViewType == spaceType) continue
                val (rotationY: Float, rotationRange: IntRange) = when {
                    isTopLeft -> -90f to 25..45
                    isTopRight -> 90f to -45..-25
                    isBottomLeft -> -90f to -45..-25
                    isBottomRight -> 90f to 25..45
                    else -> continue
                }
                val animatorSet = AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(holder.itemView, "rotationY", 0f, rotationY),
                        ObjectAnimator.ofFloat(holder.itemView, "rotation", 0f, Random.nextInt(rotationRange.first, rotationRange.last).toFloat()),
                        ObjectAnimator.ofFloat(holder.itemView, "translationX", 0f, Random.nextInt(-500, -250).toFloat()),
                        ObjectAnimator.ofFloat(holder.itemView, "alpha", 1f, 0f)
                    )
                    duration = Random.nextLong(300, 500)
                }
                animatorSet.start()
            }
            startAppDelay(item.appPackage)
        }
        private fun startAppDelay(appPackage: String) {
            lifecycleScope.launch {
                delay(350)
                startApp(appPackage)
            }
        }
        private fun bindDefaultTile(holder: TileViewHolder, position: Int, item: Tile) {
            setTileSize(item, holder.binding.tileLabel)
            setTileIconSize(holder.binding.tileIcon, item.tileSize, context.resources)
            setTileColor(holder, item)
            setTileIcon(holder.binding.tileIcon, item)
            setTileEditModeAnim(holder, item, position)
        }
        private fun setTileIcon(imageView: ImageView, item: Tile) {
            try {
                imageView.load(mainViewModel.getIconFromCache(item.appPackage))
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
                if (PREFS.isWallpaperUsed) {
                    holder.binding.container.setBackgroundColor(
                        if (PREFS.isParallaxEnabled) ContextCompat.getColor(
                            context,
                            R.color.transparent
                        ) else accentColorFromPrefs(context)
                    )
                } else {
                    holder.binding.container.setBackgroundColor(accentColorFromPrefs(context))
                }
            }
        }
        private fun setTileEditModeAnim(holder: TileViewHolder, item: Tile, position: Int) {
            val anim = createBaseWobble(holder.itemView)
            if (item.isSelected == false) {
                if (position % 2 == 0) {
                    anim.setFloatValues(-1.2f, 1.2f)
                } else {
                    anim.setFloatValues(1.2f, -1.2f)
                }
                animList.add(anim)
            } else {
                try {
                    animList[position].cancel()
                    anim.cancel()
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                }
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
                    item.appPos = i
                    mainViewModel.getTileDao().updateTile(item)
                }
                val updatedList = mainViewModel.getTileDao().getTilesList()
                val userTiles = mainViewModel.getTileDao().getUserTiles()
                lastItemPos = if(userTiles.isNotEmpty()) userTiles.last().appPos!! + 6 else updatedList.size
                withContext(mainDispatcher) {
                    if (isEditMode) {
                        refreshData(updatedList)
                    }
                }
            }
        }
        private fun showPopupWindow(holder: TileViewHolder, item: Tile, position: Int) {
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
                clearItems()
                runBlocking {
                    notifyItemChanged(position)
                }
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
                                holder.binding.tileLabel.text = item.appLabel
                            }
                        }
                        "medium" -> {
                            item.tileSize = "big"
                            withContext(mainDispatcher) {
                                holder.binding.tileLabel.text = item.appLabel
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

        private fun clearItems() {
            lifecycleScope.launch(ioDispatcher) {
                list.forEach {
                    it.isSelected = false
                    mainViewModel.getTileDao().updateTile(it)
                }
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
                labelLayout.visibility = View.VISIBLE
            }
            labelChangeBtn.setOnClickListener {
                lifecycleScope.launch(ioDispatcher) {
                    if(editor.text.toString() == "") {
                        item.appLabel = originalLabel
                    } else {
                        item.appLabel = editor.text.toString()
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
                dialog.configure(item, this, mainViewModel.getTileDao())
                dialog.show(childFragmentManager, "accentDialog")
                bottomsheet.dismiss()
            }
            uninstall.setOnClickListener {
                startActivity(Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:" + item.appPackage)))
                bottomsheet.dismiss()
            }
            appInfo.setOnClickListener {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + item.appPackage)))
                bottomsheet.dismiss()
            }
        }
        private fun startApp(packageName: String) {
            isAppOpened = true
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
        @SuppressLint("ClickableViewAccessibility")
        inner class TileViewHolder(val binding: TileBinding) : RecyclerView.ViewHolder(binding.root), ItemTouchHelperViewHolder {

            private val gestureDetector: GestureDetector =
                GestureDetector(context, object : SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent) {
                        handleLongClick()
                    }
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        visualFeedback(itemView)
                        handleClick()
                        return true
                    }
                })
            init {
                binding.cardContainer.apply {
                    if (PREFS.coloredStroke) strokeColor = Utils.launcherAccentColor(requireActivity().theme)
                    strokeWidth = if (PREFS.isWallpaperUsed && !PREFS.isParallaxEnabled) context?.resources?.getDimensionPixelSize(R.dimen.tileStrokeWidthDisabled)!! else context.resources?.getDimensionPixelSize(R.dimen.tileStrokeWidth)!!
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
                    itemView.rotation = 0f
                    lifecycleScope.launch(ioDispatcher) {
                        item.isSelected = true
                        mainViewModel.getTileDao().updateTile(item)
                        withContext(mainDispatcher) {
                            notifyItemChanged(absoluteAdapterPosition)
                            showPopupWindow(this@TileViewHolder, item, absoluteAdapterPosition)
                        }
                    }
                } else {
                    if(PREFS.isTilesAnimEnabled) {
                        mAdapter?.startDismissTilesAnim(item)
                    } else {
                        startApp(item.appPackage)
                    }
                }
            }

            private fun handleLongClick() {
                if(!isEditMode && !PREFS.isStartBlocked) {
                    enableEditMode()
                }
            }
            private fun visualFeedback(view: View?) {
                if(view != null) {
                    val defaultAlpha = view.alpha
                    lifecycleScope.launch {
                        var newValue = defaultAlpha - 0.4f
                        if(newValue <= 0.1f) {
                            newValue = 0.2f
                        }
                        ObjectAnimator.ofFloat(view, "alpha", defaultAlpha, newValue).setDuration(100).start()
                        delay(30)
                        ObjectAnimator.ofFloat(view, "alpha", newValue, defaultAlpha).setDuration(100).start()
                    }
                }
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
                if(PREFS.isWallpaperUsed && !PREFS.isParallaxEnabled) {
                    itemView.setBackgroundColor(transparentColor)
                }
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
            return oldItem.appPackage == newItem.appPackage
        }
    }
    class AccentDialog : DialogFragment() {

        private lateinit var item: Tile
        private lateinit var dao: TileDao
        private lateinit var mAdapter: NewStartAdapter

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
            val lime = view.findViewById<ImageView>(R.id.choose_color_lime)
            val back = view.findViewById<FrameLayout>(R.id.back_accent_menu)
            back.setOnClickListener { dismiss() }
            lime.setOnClickListener {
                updateTileColor(0)
            }
            val green = view.findViewById<ImageView>(R.id.choose_color_green)
            green.setOnClickListener {
                updateTileColor(1)
            }
            val emerald = view.findViewById<ImageView>(R.id.choose_color_emerald)
            emerald.setOnClickListener {
                updateTileColor(2)
            }
            val cyan = view.findViewById<ImageView>(R.id.choose_color_cyan)
            cyan.setOnClickListener {
                updateTileColor(3)
            }
            val teal = view.findViewById<ImageView>(R.id.choose_color_teal)
            teal.setOnClickListener {
                updateTileColor(4)
            }
            val cobalt = view.findViewById<ImageView>(R.id.choose_color_cobalt)
            cobalt.setOnClickListener {
                updateTileColor(5)
            }
            val indigo = view.findViewById<ImageView>(R.id.choose_color_indigo)
            indigo.setOnClickListener {
                updateTileColor(6)
            }
            val violet = view.findViewById<ImageView>(R.id.choose_color_violet)
            violet.setOnClickListener {
                updateTileColor(7)
            }
            val pink = view.findViewById<ImageView>(R.id.choose_color_pink)
            pink.setOnClickListener {
                updateTileColor(8)
            }
            val magenta = view.findViewById<ImageView>(R.id.choose_color_magenta)
            magenta.setOnClickListener {
                updateTileColor(9)
            }
            val crimson = view.findViewById<ImageView>(R.id.choose_color_crimson)
            crimson.setOnClickListener {
                updateTileColor(10)
            }
            val red = view.findViewById<ImageView>(R.id.choose_color_red)
            red.setOnClickListener {
                updateTileColor(11)
            }
            val orange = view.findViewById<ImageView>(R.id.choose_color_orange)
            orange.setOnClickListener {
                updateTileColor(12)
            }
            val amber = view.findViewById<ImageView>(R.id.choose_color_amber)
            amber.setOnClickListener {
                updateTileColor(13)
            }
            val yellow = view.findViewById<ImageView>(R.id.choose_color_yellow)
            yellow.setOnClickListener {
                updateTileColor(14)
            }
            val brown = view.findViewById<ImageView>(R.id.choose_color_brown)
            brown.setOnClickListener {
                updateTileColor(15)
            }
            val olive = view.findViewById<ImageView>(R.id.choose_color_olive)
            olive.setOnClickListener {
                updateTileColor(16)
            }
            val steel = view.findViewById<ImageView>(R.id.choose_color_steel)
            steel.setOnClickListener {
                updateTileColor(17)
            }
            val mauve = view.findViewById<ImageView>(R.id.choose_color_mauve)
            mauve.setOnClickListener {
                updateTileColor(18)
            }
            val taupe = view.findViewById<ImageView>(R.id.choose_color_taupe)
            taupe.setOnClickListener {
                updateTileColor(19)
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
            mAdapter.notifyItemChanged(item.appPos!!)
            mAdapter.showSettingsBottomSheet(item, item.appPos!!)
            super.dismiss()
        }
    }
}
class ParallaxScroll(private val wallpaper: ImageView, private val adapter: NewStart.NewStartAdapter) : RecyclerView.OnScrollListener() {

    private val imageHeight = wallpaper.drawable.intrinsicHeight

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val recyclerViewHeight = recyclerView.height
        val offset = (imageHeight - recyclerViewHeight) / 2
        wallpaper.translationY = -(recyclerView.computeVerticalScrollOffset() * calculateParallax(adapter.itemCount)).coerceIn(-offset.toFloat(), offset.toFloat())
    }
    private fun calculateParallax(itemCount: Int): Float {
        val min = 0.1f
        val max = 0.5f
        return min + (max - min) * (itemCount - 10) / 100f
    }
}
@Suppress("DEPRECATION")
class BackgroundDecoration() : RecyclerView.ItemDecoration() {

    private val backgroundColor = if(PREFS.isLightThemeUsed) Color.WHITE else Color.BLACK

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        val left = parent.paddingLeft
        val top = parent.paddingTop
        val right = parent.width - parent.paddingRight
        val bottom = parent.height - parent.paddingBottom

        c.save()
        c.clipRect(left, top, right, bottom)

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            c.clipRect(child.left.toFloat(), child.top.toFloat(), child.right.toFloat(), child.bottom.toFloat(), Region.Op.DIFFERENCE)
        }

        c.drawColor(backgroundColor)
        c.restore()
    }
}
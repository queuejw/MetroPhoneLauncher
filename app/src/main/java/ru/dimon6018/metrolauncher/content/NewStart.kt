package ru.dimon6018.metrolauncher.content

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.AppDao
import ru.dimon6018.metrolauncher.content.data.AppData
import ru.dimon6018.metrolauncher.content.data.AppEntity
import ru.dimon6018.metrolauncher.helpers.ItemTouchCallback
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperAdapter
import ru.dimon6018.metrolauncher.helpers.ItemTouchHelperViewHolder
import ru.dimon6018.metrolauncher.helpers.OnStartDragListener
import java.util.Collections

class NewStart: Fragment(), OnStartDragListener {

    private var mRecyclerView: RecyclerView? = null
    private var mSpannedLayoutManager: SpannedGridLayoutManager? = null
    private var adapter: NewStartAdapter? = null
    private var tiles: MutableList<AppEntity>? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var appsDbCall: AppDao? = null
    private var appsDB: AppData? = null

    private lateinit var frame: ConstraintLayout

    private var allAppsButton: MaterialCardView? = null
    private lateinit var v: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        v = inflater.inflate(R.layout.start_screen, container, false)
        mRecyclerView = v.findViewById(R.id.start_apps_tiles)
        frame = v.findViewById(R.id.startFrame)
        CoroutineScope(Dispatchers.Default).launch {
            appsDbCall = AppData.getAppData(requireContext()).getAppDao()
            appsDB = AppData.getAppData(requireContext())
            tiles = appsDbCall!!.getJustApps()
            mSpannedLayoutManager = if (!PREFS!!.isMoreTilesEnabled) {
                SpannedGridLayoutManager(orientation = RecyclerView.VERTICAL, _rowCount = 8, _columnCount = 4)
            } else {
                SpannedGridLayoutManager(orientation = RecyclerView.VERTICAL, _rowCount = 12, _columnCount = 6)
            }
            mSpannedLayoutManager!!.itemOrderIsStable = true
            mSpannedLayoutManager!!.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
                when (tiles!![position].appSize) {
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
            adapter = NewStartAdapter(requireContext(), tiles!!)
            val callback: ItemTouchHelper.Callback = ItemTouchCallback(adapter!!)
            mItemTouchHelper = ItemTouchHelper(callback)
            setBackground()
            runBlocking {
                requireActivity().runOnUiThread {
                    allAppsButton = v.findViewById(R.id.allAppsButton)
                    allAppsButton!!.setOnClickListener {
                        (requireActivity() as Main).openAllApps()
                    }
                    mRecyclerView!!.layoutManager = mSpannedLayoutManager
                    mRecyclerView!!.adapter = adapter
                    mItemTouchHelper!!.attachToRecyclerView(mRecyclerView)
                    mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            if (adapter?.isEditMode == false) {
                                if (!recyclerView.canScrollVertically(-1)) {
                                    allAppsButton!!.visibility = View.INVISIBLE
                                } else {
                                    allAppsButton!!.visibility = View.VISIBLE
                                }
                            }
                        }
                    })
                    frame.setOnClickListener {
                        if (adapter?.isEditMode == true) {
                            adapter?.disableEditMode()
                            adapter?.stopWobble(true)
                        }
                    }
                }
            }
        }
        return v
    }
    private fun setBackground() {
        if(PREFS!!.isWallpaperUsed) {
            try {
                getPermission()
                val wallpaperManager = WallpaperManager.getInstance(context)
                val bmp = wallpaperManager.drawable
                requireActivity().runOnUiThread {
                    mRecyclerView?.background = bmp
                }
            } catch (e: Exception) {
                Log.e("Start", e.toString())
            }
        } else {
            mRecyclerView?.background = null
        }
    }
    private fun getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        .setData(Uri.parse(String.format("package:%s", activity?.packageName))), 1507)
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE), 1507)
            }
        } else {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1507)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            val new = appsDbCall?.getJustApps()
            if (new != null) {
                if (adapter?.list == new) {
                    cancel("old list == new list")
                }
            }
            runBlocking {
                requireActivity().runOnUiThread {
                    if (new != null) {
                        tiles = new
                        adapter?.setData(new)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if(adapter?.isEditMode == true) {
            adapter?.disableEditMode()
        }
    }
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null) {
            if (adapter?.isEditMode == true) {
                if (viewHolder.itemViewType == adapter?.spaceType) {
                    return
                }
                mItemTouchHelper!!.startDrag(viewHolder)
            }
        }
    }
    inner class NewStartAdapter(val context: Context, var list: MutableList<AppEntity>): RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

        private val tileType: Int = 0
        val spaceType: Int = 1

        var isEditMode = false
        private val backgroundColor = (frame.background as ColorDrawable).color
        private val mWobbleAnimators: MutableList<ObjectAnimator> = ArrayList()

        init {
            setHasStableIds(true)
        }
        fun setData(newData: MutableList<AppEntity>) {
            list = newData
            notifyDataSetChanged()
        }
        private fun enableEditMode() {
            if(isEditMode) {
                return
            }
            mRecyclerView!!.startAnimation(AnimationUtils.loadAnimation(context, R.anim.editmode_enter))
            mRecyclerView!!.scaleX = 0.9f
            mRecyclerView!!.scaleY = 0.9f
            isEditMode = true
            (requireActivity() as Main).hideNavBar()
            startWobbleAnimation()
        }
        fun disableEditMode() {
            if(!isEditMode) {
                return
            }
            mRecyclerView!!.startAnimation(AnimationUtils.loadAnimation(context, R.anim.editmode_dismiss))
            mRecyclerView!!.scaleX = 1f
            mRecyclerView!!.scaleY = 1f
            isEditMode = false
            (requireActivity() as Main).showNavBar()
            stopWobble(true)
        }
        private fun startWobbleAnimation() {
            for (i in 0..itemCount) {
                val v: View? = mRecyclerView?.getChildAt(i)
                if(v != null) {
                if (i % 2 == 0) animateWobble(v) else animateWobbleInverse(v)
                }
            }
        }
        fun stopWobble(resetRotation: Boolean) {
            for (wobbleAnimator in mWobbleAnimators) {
                wobbleAnimator.cancel()
            }
            mWobbleAnimators.clear()
            for (i in 0..itemCount) {
                val v: View? = mRecyclerView?.getChildAt(i)
                if(v != null) {
                    if (resetRotation) v.rotation = 0f
                } else {
                    Log.e("anim", "null $i")
                    break
                }
            }
        }
        private fun startSingleWobble(v: View, position: Int) {
            if (position % 2 == 0) animateWobble(v) else animateWobbleInverse(v)
        }
        private fun stopSingleWobble(v: View, position: Int) {
            mWobbleAnimators[position].cancel()
            v.clearAnimation()
            v.rotation = 0f
        }
        private fun restartWobble() {
            stopWobble(false)
            startWobbleAnimation()
        }
        private fun animateWobble(v: View) {
            val animator = createBaseWobble(v)
            animator.setFloatValues(-1.2f, 1.2f)
            animator.start()
            mWobbleAnimators.add(animator)
        }

        private fun animateWobbleInverse(v: View) {
            val animator = createBaseWobble(v)
            animator.setFloatValues(1.2f, -1.2f)
            animator.start()
            mWobbleAnimators.add(animator)
        }


        private fun createBaseWobble(v: View): ObjectAnimator {
            val animator = ObjectAnimator()
            animator.setDuration(220)
            animator.repeatMode = ValueAnimator.REVERSE
            animator.repeatCount = ValueAnimator.INFINITE
            animator.setPropertyName("rotation")
            animator.setTarget(v)
            return animator
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if(viewType == tileType) {
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
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder.itemViewType == spaceType) {
                    holder.itemView.setOnClickListener {
                        if (isEditMode) {
                            disableEditMode()
                        }
                    }
                    return
            }
            holder as TileViewHolder
            val item = list[position]
            if(isEditMode) {
                startSingleWobble(holder.itemView, position)
            }
            when (item.appSize) {
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
            holder.mCardContainer.strokeColor = backgroundColor
            holder.mContainer.setOnClickListener {
                if (isEditMode) {
                    val newItem = list[position]
                    showPopupWindow(holder, newItem)
                    stopSingleWobble(holder.itemView, position)
                } else {
                    val intent = context.packageManager!!.getLaunchIntentForPackage(item.appPackage)
                    intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
            holder.mContainer.setOnLongClickListener {
                if(!isEditMode) {
                    enableEditMode()
                }
                true
            }
            if (item.tileColor != -1) {
                holder.mContainer.setBackgroundColor(Application.getTileColorFromPrefs(item.tileColor!!, context))
            } else {
                if (PREFS!!.isWallpaperUsed) {
                    holder.mContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                } else {
                    holder.mContainer.setBackgroundColor(Application.accentColorFromPrefs(context))
                }
            }
            try {
                holder.mAppIcon.setImageBitmap(getAppIcon(item.appPackage, item.appSize, resources, context.packageManager))
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("Start Adapter", e.toString())
                holder.mAppIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_close))
                CoroutineScope(Dispatchers.IO).launch {
                    appsDbCall!!.removeApp(item)
                }
            }
        }
        private fun getAppIcon(appPackage: String, size: String, res: Resources, pm: PackageManager): Bitmap {
            val bmp: Bitmap
            when (size) {
                "small" -> {
                    bmp = if (PREFS!!.isMoreTilesEnabled) {
                        pm.getApplicationIcon(appPackage).toBitmap(res.getDimensionPixelSize(R.dimen.tile_small_moreTiles_on), res.getDimensionPixelSize(R.dimen.tile_small_moreTiles_on), PREFS!!.iconBitmapConfig())
                    } else {
                        pm.getApplicationIcon(appPackage).toBitmap(res.getDimensionPixelSize(R.dimen.tile_small_moreTiles_off), res.getDimensionPixelSize(R.dimen.tile_small_moreTiles_off), PREFS!!.iconBitmapConfig())
                    }
                }
                "medium" -> {
                    bmp = if (PREFS!!.isMoreTilesEnabled) {
                        pm.getApplicationIcon(appPackage).toBitmap(res.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_on), res.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_on), PREFS!!.iconBitmapConfig())
                    } else {
                        pm.getApplicationIcon(appPackage).toBitmap(res.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), res.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), PREFS!!.iconBitmapConfig())
                    }
                }
                "big" -> {
                    bmp = if (PREFS!!.isMoreTilesEnabled) {
                        pm.getApplicationIcon(appPackage).toBitmap(res.getDimensionPixelSize(R.dimen.tile_big_moreTiles_on), res.getDimensionPixelSize(R.dimen.tile_big_moreTiles_on), PREFS!!.iconBitmapConfig())
                    } else {
                        pm.getApplicationIcon(appPackage).toBitmap(res.getDimensionPixelSize(R.dimen.tile_big_moreTiles_off), res.getDimensionPixelSize(R.dimen.tile_big_moreTiles_off), PREFS!!.iconBitmapConfig())
                    }
                }
                else -> {
                    bmp = pm.getApplicationIcon(appPackage).toBitmap(res.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), res.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), PREFS!!.iconBitmapConfig())
                }
            }
            return bmp
        }
        override fun getItemViewType(position: Int): Int {
            return if(list[position].isPlaceholder == true) {
                spaceType
            } else {
                tileType
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
                    if (isEditMode) {
                        requireActivity().runOnUiThread {
                            notifyDataSetChanged()
                        }
                    }
                }
            }
        }
        private fun showPopupWindow(holder: TileViewHolder, item: AppEntity) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = when(item.appSize) {
                "big" -> {
                    if(!PREFS!!.isMoreTilesEnabled) {
                        inflater.inflate(R.layout.tile_window_big, holder.itemView as ViewGroup, false)
                    } else {
                        inflater.inflate(R.layout.tile_window_big_moretiles, holder.itemView as ViewGroup, false)
                    }
                }
                "medium" -> {
                    if(!PREFS!!.isMoreTilesEnabled) {
                        inflater.inflate(R.layout.tile_window_medium, holder.itemView as ViewGroup, false)
                    } else {
                        inflater.inflate(R.layout.tile_window_medium_moretiles, holder.itemView as ViewGroup, false)
                    }
                }
                "small" -> {
                    if(!PREFS!!.isMoreTilesEnabled) {
                        inflater.inflate(R.layout.tile_window_small, holder.itemView as ViewGroup, false)
                    } else {
                        inflater.inflate(R.layout.tile_window_small_moretiles, holder.itemView as ViewGroup, false)
                    }
                }
                else -> {
                    inflater.inflate(R.layout.tile_window_medium, holder.itemView as ViewGroup, false)
                }
            }
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow.animationStyle = R.style.enterStyle
            val resize = popupView.findViewById<MaterialCardView>(R.id.resize)
            val resizeIcon = popupView.findViewById<ImageView>(R.id.resizeIco)
            val settings = popupView.findViewById<MaterialCardView>(R.id.settings)
            val remove = popupView.findViewById<MaterialCardView>(R.id.remove)
            popupWindow.setOnDismissListener {
                notifyItemChanged(item.appPos!!)
            }
            popupWindow.showAsDropDown(holder.itemView, 0, ((-1 * holder.itemView.height)), Gravity.CENTER)
            val arrow = when(item.appSize) {
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
                    when (item.appSize) {
                        "small" -> {
                            item.appSize = "medium"
                            appsDbCall!!.updateApp(item)
                            holder.mTextView.post {
                                holder.mTextView.text = ""
                            }
                        }

                        "medium" -> {
                            item.appSize = "big"
                            appsDbCall!!.updateApp(item)
                            holder.mTextView.post {
                                holder.mTextView.text = item.appLabel
                            }
                        }

                        "big" -> {
                            item.appSize = "small"
                            appsDbCall!!.updateApp(item)
                        }
                    }
                    runBlocking {
                        requireActivity().runOnUiThread {
                            popupWindow.dismiss()
                            notifyItemChanged(item.appPos!!)
                        }
                    }
                }
            }
            remove.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    appsDbCall!!.removeApp(item)
                    list.remove(item)
                    runBlocking {
                        requireActivity().runOnUiThread {
                            notifyDataSetChanged()
                        }
                    }
                }
                popupWindow.dismiss()
            }
            settings.setOnClickListener {
                showSettingsBottomSheet(item)
                popupWindow.dismiss()
            }
        }
        fun showSettingsBottomSheet(item: AppEntity) {
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
                colorSub.setTextColor(Application.getTileColorFromPrefs(item.tileColor!!, context))
                colorSub.text = getString(R.string.tileSettings_color_sub, Application.getTileColorName(item.tileColor!!, context))
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
                            notifyItemRemoved(item.appPos!!)
                        }
                    }
                }
            }
            removeColor.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = -1
                    appsDbCall!!.updateApp(item)
                    requireActivity().runOnUiThread {
                        notifyItemRemoved(item.appPos!!)
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
                val intent = Intent(Intent.ACTION_DELETE)
                intent.setData(Uri.parse("package:" + item.appPackage))
                startActivity(intent)
                bottomsheet.dismiss()
            }
            bottomsheet.show()
        }
    }
    class TileViewHolder(v: View) : RecyclerView.ViewHolder(v), ItemTouchHelperViewHolder {
        val mCardContainer: MaterialCardView  = v.findViewById(R.id.cardContainer)
        val mContainer: FrameLayout = v.findViewById(R.id.container)
        val mTextView: TextView = v.findViewById(android.R.id.text1)
        val mAppIcon: ImageView = v.findViewById(android.R.id.icon1)

        override fun onItemSelected() {}
        override fun onItemClear() {}
    }
    class SpaceViewHolder(v: View) : RecyclerView.ViewHolder(v)

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
            adapter.showSettingsBottomSheet(item)
            super.dismiss()
        }
    }
}
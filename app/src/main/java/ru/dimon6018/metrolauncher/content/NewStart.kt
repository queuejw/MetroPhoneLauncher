package ru.dimon6018.metrolauncher.content

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
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

    private lateinit var frame: FrameLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.start_screen, container, false)
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
            runBlocking {
                requireActivity().runOnUiThread {
                    if(PREFS!!.isWallpaperUsed) {
                        try {
                            getPermission()
                            val wallpaperManager = WallpaperManager.getInstance(context)
                            frame = v.findViewById(R.id.startFrame)
                            frame.background = wallpaperManager.drawable
                        } catch (e: Exception) {
                            Log.e("Start", e.toString())
                        }
                    }
                    mRecyclerView = v.findViewById(R.id.start_apps_tiles)
                    mRecyclerView!!.layoutManager = mSpannedLayoutManager
                    mRecyclerView!!.adapter = adapter
                    mItemTouchHelper!!.attachToRecyclerView(mRecyclerView)
                }
            }
        }
        return v
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
        CoroutineScope(Dispatchers.Default).launch {
            val new = appsDbCall?.getJustApps()
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
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null) {
            if(viewHolder.itemViewType == adapter?.spaceType) {
                return
            }
            mItemTouchHelper!!.startDrag(viewHolder)
        }
    }
    inner class NewStartAdapter(val context: Context, private var list: MutableList<AppEntity>): RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

        private val tileType: Int = 0
        val spaceType: Int = 1
        init {
            setHasStableIds(true)
        }
        fun setData(newData: MutableList<AppEntity>) {
            list = newData
            notifyDataSetChanged()
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

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder.itemViewType == spaceType) {
                return
            }
            holder as TileViewHolder
            val item = list[position]
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
            holder.mContainer.setOnClickListener {
                val intent = context.packageManager!!.getLaunchIntentForPackage(item.appPackage)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            if(item.tileColor != -1) {
                holder.mContainer.setBackgroundColor(Application.getTileColorFromPrefs(item.tileColor!!, context))
            } else {
                if(PREFS!!.isWallpaperUsed) {
                    holder.mContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                } else {
                    holder.mContainer.setBackgroundColor(Application.accentColorFromPrefs(context))
                }
            }
            try {
                holder.mAppIcon.setImageBitmap(getAppIcon(item.appPackage, item.appSize))
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("Start Adapter", e.toString())
                holder.mAppIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_close))
                CoroutineScope(Dispatchers.IO).launch {
                    appsDbCall!!.removeApp(item)
                }
            }
            holder.mBtn.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val newItem = list[position]
                    runBlocking {
                        requireActivity().runOnUiThread {
                            showPopupWindow(holder, newItem)
                        }
                    }
                }
            }
        }
        private fun getAppIcon(appPackage: String, size: String): Bitmap {
            val bmp: Bitmap
            val pm = context.packageManager
            val res = context.resources
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
            notifyItemChanged(position)
        }

        override fun onDragAndDropCompleted() {
            CoroutineScope(Dispatchers.IO).launch {
                val itemsReserved = list
                for (i in 0 until itemsReserved.size) {
                    val item = itemsReserved[i]
                    item.appPos = i
                    appsDbCall?.insertItem(item)
                }
                runBlocking {
                    requireActivity().runOnUiThread {
                        notifyDataSetChanged()
                    }
                }
            }
        }
        private fun showPopupWindow(holder: TileViewHolder, item: AppEntity) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = if(item.appSize == "medium" && PREFS!!.isMoreTilesEnabled) {
                inflater.inflate(R.layout.tile_window_horiz, holder.itemView as ViewGroup, false)
            } else if(item.appSize == "small" ) {
                inflater.inflate(R.layout.tile_window_horiz, holder.itemView as ViewGroup, false)
            } else {
                inflater.inflate(R.layout.tile_window, holder.itemView as ViewGroup, false)
            }
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow.animationStyle = R.style.enterStyle
            val resize = popupView.findViewById<MaterialCardView>(R.id.resize)
            val settings = popupView.findViewById<MaterialCardView>(R.id.settings)
            val remove = popupView.findViewById<MaterialCardView>(R.id.remove)
            popupWindow.showAsDropDown(holder.itemView, 0, ((-1 * holder.itemView.height)), Gravity.CENTER)
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
                            notifyDataSetChanged()
                        }
                    }
                }
            }
            remove.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    appsDbCall!!.removeApp(item)
                }
                popupWindow.dismiss()
                notifyItemRemoved(item.appPos!!)
            }
            settings.setOnClickListener {
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
                label.text = item.appLabel
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
                        item.appLabel = editor.text.toString()
                        appsDbCall!!.updateApp(item)
                        activity!!.runOnUiThread {
                            bottomsheet.dismiss()
                            notifyDataSetChanged()
                        }
                    }
                }
                removeColor.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        item.tileColor = -1
                        appsDbCall!!.updateApp(item)
                        activity!!.runOnUiThread {
                            notifyDataSetChanged()
                        }
                    }
                    bottomsheet.dismiss()
                }
                changeColor.setOnClickListener {
                    AccentDialog.display(parentFragmentManager, appsDbCall!!, item, item.appPos!!)
                    bottomsheet.dismiss()
                }
                uninstall.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DELETE)
                    intent.setData(Uri.parse("package:" + item.appPackage))
                    startActivity(intent)
                    bottomsheet.dismiss()
                }
                popupWindow.dismiss()
                bottomsheet.show()
            }
        }
    }
    class TileViewHolder(v: View) : RecyclerView.ViewHolder(v), ItemTouchHelperViewHolder {
        val mContainer: FrameLayout
        val mTextView: TextView
        val mAppIcon: ImageView
        val mBtn: View
        init {
            mContainer = v.findViewById(R.id.container)
            mTextView = v.findViewById(android.R.id.text1)
            mAppIcon = v.findViewById(android.R.id.icon1)
            mBtn = v.findViewById(R.id.menuTrigger)
        }

        override fun onItemSelected() {}
        override fun onItemClear() {}
    }
    class SpaceViewHolder(v: View) : RecyclerView.ViewHolder(v)
    class AccentDialog : DialogFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
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
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val green = view.findViewById<ImageView>(R.id.choose_color_green)
            green.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 1
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val emerald = view.findViewById<ImageView>(R.id.choose_color_emerald)
            emerald.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 2
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val cyan = view.findViewById<ImageView>(R.id.choose_color_cyan)
            cyan.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 3
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val teal = view.findViewById<ImageView>(R.id.choose_color_teal)
            teal.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 4
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val cobalt = view.findViewById<ImageView>(R.id.choose_color_cobalt)
            cobalt.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 5
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val indigo = view.findViewById<ImageView>(R.id.choose_color_indigo)
            indigo.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 6
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val violet = view.findViewById<ImageView>(R.id.choose_color_violet)
            violet.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 7
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val pink = view.findViewById<ImageView>(R.id.choose_color_pink)
            pink.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 8
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val magenta = view.findViewById<ImageView>(R.id.choose_color_magenta)
            magenta.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 9
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val crimson = view.findViewById<ImageView>(R.id.choose_color_crimson)
            crimson.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 10
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val red = view.findViewById<ImageView>(R.id.choose_color_red)
            red.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 11
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val orange = view.findViewById<ImageView>(R.id.choose_color_orange)
            orange.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 12
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val amber = view.findViewById<ImageView>(R.id.choose_color_amber)
            amber.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 13
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val yellow = view.findViewById<ImageView>(R.id.choose_color_yellow)
            yellow.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 14
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val brown = view.findViewById<ImageView>(R.id.choose_color_brown)
            brown.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 15
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val olive = view.findViewById<ImageView>(R.id.choose_color_olive)
            olive.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 16
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val steel = view.findViewById<ImageView>(R.id.choose_color_steel)
            steel.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 17
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val mauve = view.findViewById<ImageView>(R.id.choose_color_mauve)
            mauve.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 18
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
            val taupe = view.findViewById<ImageView>(R.id.choose_color_taupe)
            taupe.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    item.tileColor = 19
                    dbCallDialog!!.updateApp(item)
                }
                dismiss()
            }
        }

        override fun dismiss() {
            parentFragment?.onResume()
            super.dismiss()
        }
        companion object {
            private const val TAG = "accentD"
            fun display(fragmentManager: FragmentManager?, db: AppDao, entity: AppEntity, position: Int): AccentDialog {
                POS = position
                dbCallDialog = db
                item = entity
                val accentDialog = AccentDialog()
                accentDialog.show(fragmentManager!!, TAG)
                return accentDialog
            }
            private var POS: Int? = null
            private lateinit var item: AppEntity
            private var dbCallDialog: AppDao? = null
        }
    }
}
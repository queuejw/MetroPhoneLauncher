package ru.dimon6018.metrolauncher.content

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import ir.alirezabdn.wp7progress.WP7ProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
import ru.dimon6018.metrolauncher.helpers.SpaceItemDecorator
import java.util.Collections


class Start : Fragment(), OnStartDragListener {
    private var mRecyclerView: RecyclerView? = null
    private var mAppListButton: MaterialCardView? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var mSpannedLayoutManager: SpannedGridLayoutManager? = null
    private var adapter: StartAdapter? = null

    private var loadingHolder: LinearLayout? = null
    private var progressBar: WP7ProgressBar? = null

    private var background: LinearLayout? = null

    var contxt: Context? = null

    private var dbCall: AppDao? = null
    private var db: AppData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.start_screen, container, false)
        contxt = context
        dbCall = AppData.getAppData(contxt!!).getAppDao()
        db = AppData.getAppData(contxt!!)
        progressBar = v.findViewById(R.id.progressBarStart)
        progressBar!!.setIndicatorRadius(5)
        progressBar!!.showProgressBar()
        loadingHolder = v.findViewById(R.id.loadingHolderStart)
        mAppListButton = v.findViewById(R.id.open_applist_btn)
        background = v.findViewById(R.id.startBackground)
        mRecyclerView = v.findViewById(R.id.start_apps_tiles)
        if (PREFS!!.isCustomBackgroundUsed) {
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
        CoroutineScope(Dispatchers.Default).launch {
            mSpannedLayoutManager = if (!PREFS!!.isMoreTilesEnabled) {
                SpannedGridLayoutManager(orientation = RecyclerView.VERTICAL, _rowCount = 8, _columnCount = 4, context = contxt!!)
            } else {
                SpannedGridLayoutManager(orientation = RecyclerView.VERTICAL, _rowCount = 12, _columnCount = 6, context = contxt!!)
            }
            mSpannedLayoutManager!!.itemOrderIsStable = true
            tileList = dbCall!!.getJustApps()
            adapter = StartAdapter(tileList!!, contxt!!, dbCall!!)
            val callback: ItemTouchHelper.Callback = ItemTouchCallback(adapter!!)
            mItemTouchHelper = ItemTouchHelper(callback)
            requireActivity().runOnUiThread {
                mRecyclerView!!.adapter = adapter
                mRecyclerView!!.addItemDecoration(SpaceItemDecorator(8, 8, 8, 8))
                mRecyclerView!!.layoutManager = mSpannedLayoutManager
                mItemTouchHelper!!.attachToRecyclerView(mRecyclerView)
                setLM()
                hideLoadingHolder()
            }
        }
    }
    private fun setLM() {
        mSpannedLayoutManager!!.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
            when (tileList!![position].appSize) {
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
    private fun hideLoadingHolder() {
        progressBar!!.hideProgressBar()
        loadingHolder!!.visibility = View.GONE
        mRecyclerView!!.visibility = View.VISIBLE
        mAppListButton!!.visibility = View.VISIBLE
    }
    override fun onResume() {
        super.onResume()
        observer()
    }
    private fun observer() {
        if(dbCall?.getApps()?.asLiveData()?.hasActiveObservers() == true) {
            return
        }
        dbCall?.getApps()?.asLiveData()?.observe(requireActivity()) {
            tileList = it
            adapter?.setNewData(it)
        }
    }
    private fun killObserver() {
        dbCall?.getApps()?.asLiveData()?.removeObservers(requireActivity())
    }
    override fun onPause() {
        super.onPause()
        killObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        killObserver()
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
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null) {
            mItemTouchHelper!!.startDrag(viewHolder)
        }
    }
    companion object {
        var tileList: MutableList<AppEntity>? = null
    }
    inner class StartAdapter(private var items: MutableList<AppEntity>, private val adapterContext: Context, private val dbAppsCall: AppDao) : Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

        private var packageManager: PackageManager? = null
        private var popupWindow: PopupWindow? = null

        private val tileType: Int = 0
        private val placeholderType: Int = 1

        init {
            setHasStableIds(true)
            packageManager = adapterContext.packageManager
        }

        override fun onItemMove(fromPosition: Int, toPosition: Int) {
            popupWindow?.dismiss()
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(items, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(items, i, i - 1)
                }
            }
            notifyItemMoved(fromPosition, toPosition)
        }
        override fun onDragAndDropCompleted() {
            CoroutineScope(Dispatchers.IO).launch {
                val itemsReserved = items
                for (i in 0 until itemsReserved.size) {
                    val item = itemsReserved[i]
                    item.appPos = i
                    dbAppsCall.insertItem(item)
                }
            }
        }
        override fun onItemDismiss(position: Int) {
            notifyDataSetChanged()
        }

        fun setNewData(newData: MutableList<AppEntity>) {
            items = newData
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
        inner class HolderItemViewHolder(v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return when(viewType) {
                tileType -> {
                    val v = inflater.inflate(R.layout.tile, parent, false)
                    NormalItemViewHolder(v)
                }
                placeholderType -> {
                    val v = inflater.inflate(R.layout.space, parent, false)
                    HolderItemViewHolder(v)
                }
                else -> {
                    val v = inflater.inflate(R.layout.space, parent, false)
                    HolderItemViewHolder(v)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when(holder.itemViewType) {
                tileType -> {
                    bindTile(holder as NormalItemViewHolder, position)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if(items[position].isPlaceholder == true) {
                placeholderType
            } else {
                tileType
            }
        }

        private fun bindTile(holder: NormalItemViewHolder, position: Int) {
            val item = items[position]
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
            if(PREFS!!.isCustomBackgroundUsed) {
                holder.mContainer.background = AppCompatResources.getDrawable(adapterContext, R.drawable.start_transparent)
                val view: View = holder.mContainer
                val bmpNew = Bitmap.createBitmap(BitmapFactory.decodeFile(PREFS!!.backgroundPath), view.x.toInt(), view.y.toInt(), view.width, view.height, null, true)
                holder.mContainer.background = bmpNew.toDrawable(resources)
            }
            holder.mContainer.setOnClickListener {
                val intent = packageManager!!.getLaunchIntentForPackage(item.appPackage)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            holder.mContainer.setOnLongClickListener {
                showPopupWindow(holder, position, adapterContext)
                true
            }
            if(item.tileColor != -1) {
                holder.mContainer.setBackgroundColor(Application.getTileColorFromPrefs(item.tileColor!!, adapterContext))
            } else {
                holder.mContainer.setBackgroundColor(Application.accentColorFromPrefs(adapterContext))
            }
            val bmpGen: Deferred<Bitmap> = CoroutineScope(Dispatchers.Default).async {
                return@async getAppIcon(item.appPackage, item.appSize)
            }
            runBlocking {
                holder.mAppIcon.setImageBitmap(bmpGen.await())
            }
        }
        private fun showPopupWindow(holder: NormalItemViewHolder, position: Int, context: Context) {
            val item = items[position]
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = if(item.appSize == "small") inflater.inflate(R.layout.tile_window_horiz, holder.itemView as ViewGroup, false) else inflater.inflate(R.layout.tile_window, holder.itemView as ViewGroup, false)
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow!!.animationStyle = R.style.enterStyle
            val resize = popupView.findViewById<MaterialCardView>(R.id.resize)
            val settings = popupView.findViewById<MaterialCardView>(R.id.settings)
            val remove = popupView.findViewById<MaterialCardView>(R.id.remove)
            popupWindow!!.showAsDropDown(holder.itemView, 0, ((-1 * holder.itemView.height)), Gravity.CENTER)
            resize.setOnClickListener {
                when (item.appSize) {
                    "small" -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            item.appSize = "medium"
                            dbAppsCall.updateApp(item)
                        }
                        holder.mTextView.post {
                            holder.mTextView.text = ""
                        }
                        item.appSize = "medium"
                    }
                    "medium" -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            item.appSize = "big"
                            dbAppsCall.updateApp(item)
                        }
                        item.appSize = "big"
                    }

                    "big" -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            item.appSize = "small"
                            dbAppsCall.updateApp(item)
                        }
                        item.appSize = "small"
                    }
                }
                popupWindow!!.dismiss()
                notifyDataSetChanged()
            }
            remove.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    dbAppsCall.removeApp(item)
                }
                popupWindow!!.dismiss()
                notifyItemRemoved(position)
            }
            settings.setOnClickListener {
                val bottomsheet = BottomSheetDialog(contxt!!)
                bottomsheet.setContentView(R.layout.tile_bottomsheet)
                bottomsheet.dismissWithAnimation = true
                val bottomSheetInternal = bottomsheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                BottomSheetBehavior.from<View?>(bottomSheetInternal!!).peekHeight = adapterContext.resources.getDimensionPixelSize(R.dimen.bottom_sheet_size)
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
                    colorSub.setTextColor(Application.getTileColorFromPrefs(item.tileColor!!, adapterContext))
                    colorSub.text = getString(R.string.tileSettings_color_sub, Application.getTileColorName(item.tileColor!!))
                }
                changeLabel.setOnClickListener {
                    labellayout.visibility = View.VISIBLE
                }
                labelChangeBtn.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        item.appLabel = editor.text.toString()
                        dbAppsCall.updateApp(item)
                        activity!!.runOnUiThread {
                            bottomsheet.dismiss()
                        }
                    }
                }
                removeColor.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        item.tileColor = -1
                        dbAppsCall.updateApp(item)
                    }
                    bottomsheet.dismiss()
                }
                changeColor.setOnClickListener {
                    AccentDialog.display(activity!!.supportFragmentManager, position)
                    bottomsheet.dismiss()
                }
                uninstall.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DELETE)
                    intent.setData(Uri.parse("package:" + item.appPackage))
                    startActivity(intent)
                    bottomsheet.dismiss()
                }
                popupWindow!!.dismiss()
                bottomsheet.show()
            }
        }
        private fun getAppIcon(appPackage: String, size: String): Bitmap {
            val bmp: Bitmap
            when (size) {
                "small" -> {
                    bmp = if (PREFS!!.isMoreTilesEnabled) {
                        packageManager!!.getApplicationIcon(appPackage).toBitmap(resources.getDimensionPixelSize(R.dimen.tile_small_moreTiles_on), resources.getDimensionPixelSize(R.dimen.tile_small_moreTiles_on), PREFS!!.iconBitmapConfig())
                    } else {
                        packageManager!!.getApplicationIcon(appPackage).toBitmap(resources.getDimensionPixelSize(R.dimen.tile_small_moreTiles_off), resources.getDimensionPixelSize(R.dimen.tile_small_moreTiles_off), PREFS!!.iconBitmapConfig())
                    }
                }

                "medium" -> {
                    bmp = if (PREFS!!.isMoreTilesEnabled) {
                        packageManager!!.getApplicationIcon(appPackage).toBitmap(resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_on), resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_on), PREFS!!.iconBitmapConfig())
                    } else {
                        packageManager!!.getApplicationIcon(appPackage).toBitmap(resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), PREFS!!.iconBitmapConfig())
                    }
                }

                "big" -> {
                    bmp = if (PREFS!!.isMoreTilesEnabled) {
                        packageManager!!.getApplicationIcon(appPackage).toBitmap(resources.getDimensionPixelSize(R.dimen.tile_big_moreTiles_on), resources.getDimensionPixelSize(R.dimen.tile_big_moreTiles_on), PREFS!!.iconBitmapConfig())
                    } else {
                        packageManager!!.getApplicationIcon(appPackage).toBitmap(resources.getDimensionPixelSize(R.dimen.tile_big_moreTiles_off), resources.getDimensionPixelSize(R.dimen.tile_big_moreTiles_off), PREFS!!.iconBitmapConfig())
                    }
                }
                else -> {
                    bmp = packageManager!!.getApplicationIcon(appPackage).toBitmap(resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), resources.getDimensionPixelSize(R.dimen.tile_medium_moreTiles_off), PREFS!!.iconBitmapConfig())
                }
            }
            return bmp
        }

        override fun getItemCount(): Int {
            return items.size
        }
        override fun getItemId(position: Int): Long {
            return items[position].appPos!!.toLong()
        }
    }
    class AccentDialog : DialogFragment() {

        private var dbCallDialog: AppDao? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
        }
        override fun onStart() {
            super.onStart()
            Runnable {
                dbCallDialog = AppData.getAppData(requireContext()).getAppDao()
            }.run()
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
            val item = tileList!![POS!!]
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

        companion object {
            private const val TAG = "accentD"
            fun display(fragmentManager: FragmentManager?, position: Int): AccentDialog {
                POS = position
                val accentDialog = AccentDialog()
                accentDialog.show(fragmentManager!!, TAG)
                return accentDialog
            }
            private var POS: Int? = null
        }
    }
}
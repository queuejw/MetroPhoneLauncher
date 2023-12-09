package ru.dimon6018.metrolauncher.content

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
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
import com.google.android.material.textfield.TextInputLayout
import ir.alirezabdn.wp7progress.WP7ProgressBar
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.AppDao
import ru.dimon6018.metrolauncher.content.data.AppData
import ru.dimon6018.metrolauncher.content.data.AppEntity
import ru.dimon6018.metrolauncher.content.data.Prefs
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
    var prefs: Prefs? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.start_screen, container, false)
        contxt = context
        isAdapterUpdateEnabled = true
        Runnable {
            db = AppData.getAppData(contxt!!)
            dbCall = db!!.getAppDao()
        }.run()
        prefs = Prefs(contxt)
        progressBar = v.findViewById(R.id.progressBarStart)
        progressBar!!.setIndicatorRadius(5)
        progressBar!!.showProgressBar()
        loadingHolder = v.findViewById(R.id.loadingHolderStart)
        mAppListButton = v.findViewById(R.id.open_applist_btn)
        background = v.findViewById(R.id.startBackground)
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
        Thread {
            pManager = contxt!!.packageManager
            tileList = dbCall!!.getJustApps()
            detectBrokenApps()
            mSpannedLayoutManager = if(!prefs!!.isMoreTilesEnabled) {
                SpannedGridLayoutManager(orientation = RecyclerView.VERTICAL, _rowCount = 8, _columnCount = 4, context = contxt!!)
            } else {
                SpannedGridLayoutManager(orientation = RecyclerView.VERTICAL, _rowCount = 12, _columnCount = 6, context = contxt!!)
            }
            mSpannedLayoutManager!!.itemOrderIsStable = true
            adapter = StartAdapter(tileList!!)
            val callback: ItemTouchHelper.Callback = ItemTouchCallback(adapter)
            setLM()
            requireActivity().runOnUiThread {
                mRecyclerView!!.layoutManager = mSpannedLayoutManager
                mItemTouchHelper = ItemTouchHelper(callback)
                mRecyclerView!!.adapter = adapter
                mRecyclerView!!.addItemDecoration(SpaceItemDecorator(8, 8, 8, 8))
                mItemTouchHelper!!.attachToRecyclerView(mRecyclerView)
                hideLoadingHolder()
            }
        }.start()
        dbCall!!.getApps().asLiveData().observe(requireActivity()) {
            tileList = it
            if(isAdapterUpdateEnabled == true) adapter?.setNewData(it)
        }
    }
    private fun detectBrokenApps() {
        var size = tileList!!.size
        while(size != 0) {
            size -= 1
            val appEntity = tileList!![size]
            try {
                pManager!!.getPackageInfo(appEntity.appPackage, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                Thread {
                    dbCall!!.removeApp(appEntity)
                }.start()
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
        adapter?.setNewData(tileList!!)
        super.onResume()
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
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper!!.startDrag(viewHolder)
    }
    companion object {
        var db: AppData? = null
        var dbCall: AppDao? = null
        var tileList: List<AppEntity>? = null
        var pManager: PackageManager? = null
        var isAdapterUpdateEnabled: Boolean? = null
    }
    inner class StartAdapter(private var items: List<AppEntity>) : Adapter<StartAdapter.NormalItemViewHolder>(), ItemTouchHelperAdapter {
        override fun onItemMove(fromPosition: Int, toPosition: Int) {
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
            saveAllPositions()
        }
        private fun saveAllPositions() {
            isAdapterUpdateEnabled = false
            Thread {
                var allItemsCount: Int = items.size
                while (allItemsCount != 0) {
                    allItemsCount -= 1
                    val item = items[allItemsCount]
                    item.appPos = allItemsCount
                    dbCall!!.updateApp(item)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    isAdapterUpdateEnabled = true
                }, 1000)
            }.start()
        }
        override fun onItemDismiss(position: Int) {
            notifyDataSetChanged()
        }

        fun setNewData(newData: List<AppEntity>) {
            items = newData
            notifyDataSetChanged()
        }
        inner class NormalItemViewHolder(v: View) : RecyclerView.ViewHolder(v), ItemTouchHelperViewHolder {
            val mContainer: FrameLayout
            val mTextView: TextView
            val mAppIcon: ImageView

            val mTileLayout: FrameLayout
            val mRemove: MaterialCardView
            val mResize: MaterialCardView
            val mSettings: MaterialCardView

            init {
                mContainer = v.findViewById(R.id.container)
                mTextView = v.findViewById(android.R.id.text1)
                mAppIcon = v.findViewById(android.R.id.icon1)

                mTileLayout = v.findViewById(R.id.tileControl)
                mRemove = v.findViewById(R.id.tileControl_remove)
                mResize = v.findViewById(R.id.tileControl_resize)
                mSettings = v.findViewById(R.id.tileControl_settings)
            }

            override fun onItemSelected() {}
            override fun onItemClear() {}
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NormalItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.tile, parent, false)
            return NormalItemViewHolder(v)
        }
        override fun onBindViewHolder(holder: NormalItemViewHolder, position: Int) {
            val item = items[position]
            var canOpenApp = true
            when(item.appSize) {
                "small" -> {
                    if(prefs!!.isMoreTilesEnabled) {
                        holder.mTextView.text = ""
                        holder.mRemove.scaleX = 0.5f
                        holder.mRemove.scaleY = 0.5f
                        holder.mResize.scaleX = 0.5f
                        holder.mResize.scaleY = 0.5f
                        holder.mSettings.scaleX = 0.5f
                        holder.mSettings.scaleY = 0.5f
                        holder.mTileLayout.scaleX = 1.5f
                        holder.mTileLayout.scaleY = 1.5f
                    } else {
                        holder.mRemove.scaleX = 0.7f
                        holder.mRemove.scaleY = 0.7f
                        holder.mResize.scaleX = 0.7f
                        holder.mResize.scaleY = 0.7f
                        holder.mSettings.scaleX = 0.7f
                        holder.mSettings.scaleY = 0.7f
                        holder.mTileLayout.scaleX = 1.15f
                        holder.mTileLayout.scaleY = 1.15f
                    }
                    holder.mTextView.text = ""
                    holder.mResize.rotation = -145f
                }
                "medium" -> {
                    if(prefs!!.isMoreTilesEnabled) {
                        holder.mTextView.text = ""
                    } else {
                        holder.mTextView.text = item.appLabel
                    }
                    holder.mRemove.scaleX = 1f
                    holder.mRemove.scaleY = 1f
                    holder.mResize.scaleX = 1f
                    holder.mResize.scaleY = 1f
                    holder.mResize.rotation = 180f
                    holder.mSettings.scaleX = 1f
                    holder.mSettings.scaleY = 1f
                    holder.mTileLayout.scaleX = 1f
                    holder.mTileLayout.scaleY = 1f
                }
                "big" -> {
                    holder.mTextView.text = item.appLabel
                    holder.mRemove.scaleX = 1f
                    holder.mRemove.scaleY = 1f
                    holder.mResize.scaleX = 1f
                    holder.mResize.scaleY = 1f
                    holder.mResize.rotation = 45f
                    holder.mSettings.scaleX = 1f
                    holder.mSettings.scaleY = 1f
                    holder.mTileLayout.scaleX = 1f
                    holder.mTileLayout.scaleY = 1f
                }
            }
            if(prefs!!.isCustomBackgroundUsed) {
                holder.mContainer.background = AppCompatResources.getDrawable(contxt!!, R.drawable.start_transparent)
                val view: View = holder.mContainer
                view.getViewTreeObserver().addOnGlobalLayoutListener {
                    val bmp = BitmapFactory.decodeFile(prefs!!.backgroundPath)
                    val bmpNew = Bitmap.createBitmap(bmp, view.x.toInt(), view.y.toInt(), view.width, view.height, null, true)
                    holder.mContainer.background = bmpNew.toDrawable(resources)
                    bmp.recycle()
                }
            }
            holder.mContainer.setOnClickListener {
                if(canOpenApp) {
                    val intent = pManager!!.getLaunchIntentForPackage(item.appPackage)
                    intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
            holder.mContainer.setOnLongClickListener {
                canOpenApp = false
                holder.mTileLayout.visibility = View.VISIBLE
                val hideAction = Runnable {
                    canOpenApp = true
                    holder.mTileLayout.visibility = View.INVISIBLE
                }
                holder.mTileLayout.postDelayed(hideAction, 5500)
                false
            }
             try {
                 holder.mAppIcon.setImageDrawable(pManager!!.getApplicationIcon(item.appPackage))
            } catch (e: PackageManager.NameNotFoundException) {
                 Thread {
                     dbCall!!.removeApp(item)
                 }.start()
            }
            if(item.tileColor != -1) {
                 holder.mContainer.setBackgroundColor(Application.getTileColorFromPrefs(item.tileColor!!))
            } else {
                holder.mContainer.setBackgroundColor(Application.getAccentColorFromPrefs())
            }
            holder.mResize.setOnClickListener {
                when (item.appSize) {
                    "small" -> {
                        Thread {
                            val appEntity = AppEntity(item.appPos, item.id, item.tileColor,"medium", item.appLabel, item.appPackage)
                            dbCall!!.updateApp(appEntity)
                        }.start()
                        holder.mTextView.post {
                            holder.mTextView.text = ""
                        }
                        item.appSize = "medium"
                    }
                    "medium" -> {
                        Thread {
                            val appEntity = AppEntity(item.appPos, item.id, item.tileColor,"big", item.appLabel, item.appPackage)
                            dbCall!!.updateApp(appEntity)
                        }.start()
                        item.appSize = "big"
                    }

                    "big" -> {
                        Thread {
                            val appEntity = AppEntity(item.appPos, item.id, item.tileColor,"small", item.appLabel, item.appPackage)
                            dbCall!!.updateApp(appEntity)
                        }.start()
                        item.appSize = "small"
                    }
                }
                notifyDataSetChanged()
            }
            holder.mRemove.setOnClickListener {
                Thread {
                    dbCall!!.removeApp(dbCall!!.getAppById(item.id!!))
                }.start()
                notifyItemRemoved(position)
            }
            holder.mSettings.setOnClickListener {
                val bottomsheet = BottomSheetDialog(contxt!!)
                bottomsheet.setContentView(R.layout.tile_bottomsheet)
                bottomsheet.dismissWithAnimation = true
                val bottomSheetInternal = bottomsheet.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                BottomSheetBehavior.from<View?>(bottomSheetInternal!!).peekHeight = context!!.resources.getDimensionPixelSize(R.dimen.bottom_sheet_size)
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
                    colorSub.setTextColor(Application.getTileColorFromPrefs(item.tileColor!!))
                    colorSub.text = getString(R.string.tileSettings_color_sub, Application.getTileColorName(item.tileColor!!))
                }
                changeLabel.setOnClickListener {
                    labellayout.visibility = View.VISIBLE
                }
                labelChangeBtn.setOnClickListener {
                    Thread {
                        val appEntity = AppEntity(item.appPos, item.id, item.tileColor,item.appSize, editor.text.toString(), item.appPackage)
                        dbCall!!.updateApp(appEntity)
                        activity!!.runOnUiThread {
                            bottomsheet.dismiss()
                        }
                    }.start()
                }
                removeColor.setOnClickListener {
                    Thread {
                        val appEntity = AppEntity(item.appPos, item.id, -1,item.appSize, item.appLabel, item.appPackage)
                        dbCall!!.updateApp(appEntity)
                    }.start()
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
                bottomsheet.show()
            }
        }
        override fun getItemCount(): Int {
            return items.size
        }
        override fun getItemId(position: Int): Long {
            return items[position].appPos!!.toLong()
        }
    }
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
            dialog?.setTitle("ACCENT")
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
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 0,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val green = view.findViewById<ImageView>(R.id.choose_color_green)
            green.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 1,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val emerald = view.findViewById<ImageView>(R.id.choose_color_emerald)
            emerald.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 2,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val cyan = view.findViewById<ImageView>(R.id.choose_color_cyan)
            cyan.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 3,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val teal = view.findViewById<ImageView>(R.id.choose_color_teal)
            teal.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 4,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val cobalt = view.findViewById<ImageView>(R.id.choose_color_cobalt)
            cobalt.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 5,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val indigo = view.findViewById<ImageView>(R.id.choose_color_indigo)
            indigo.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 6,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val violet = view.findViewById<ImageView>(R.id.choose_color_violet)
            violet.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 7,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val pink = view.findViewById<ImageView>(R.id.choose_color_pink)
            pink.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 8,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val magenta = view.findViewById<ImageView>(R.id.choose_color_magenta)
            magenta.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 9,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val crimson = view.findViewById<ImageView>(R.id.choose_color_crimson)
            crimson.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 10,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val red = view.findViewById<ImageView>(R.id.choose_color_red)
            red.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 11,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val orange = view.findViewById<ImageView>(R.id.choose_color_orange)
            orange.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 12,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val amber = view.findViewById<ImageView>(R.id.choose_color_amber)
            amber.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 13,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val yellow = view.findViewById<ImageView>(R.id.choose_color_yellow)
            yellow.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 14,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val brown = view.findViewById<ImageView>(R.id.choose_color_brown)
            brown.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 15,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val olive = view.findViewById<ImageView>(R.id.choose_color_olive)
            olive.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 16,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val steel = view.findViewById<ImageView>(R.id.choose_color_steel)
            steel.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 17,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val mauve = view.findViewById<ImageView>(R.id.choose_color_mauve)
            mauve.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 18,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
                dismiss()
            }
            val taupe = view.findViewById<ImageView>(R.id.choose_color_taupe)
            taupe.setOnClickListener {
                Thread {
                    val appEntity = AppEntity(item.appPos, item.id, 19,item.appSize, item.appLabel, item.appPackage)
                    dbCall!!.updateApp(appEntity)
                }.start()
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
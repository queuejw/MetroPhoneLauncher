package ru.dimon6018.metrolauncher.content

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import ir.alirezabdn.wp7progress.WP7ProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import leakcanary.LeakCanary
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.setUpApps
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.Start.Companion.tileList
import ru.dimon6018.metrolauncher.content.data.App
import ru.dimon6018.metrolauncher.content.data.AppDao
import ru.dimon6018.metrolauncher.content.data.AppData
import ru.dimon6018.metrolauncher.content.data.AppEntity
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.helpers.WPDialog
import java.util.Collections
import java.util.Locale
import kotlin.random.Random


class AllApps : Fragment(R.layout.all_apps_screen) {
    private var mApps: ArrayList<App>? = null

    private var recyclerView: RecyclerView? = null
    private var search: TextInputLayout? = null
    private var appAdapter: AppAdapter? = null
    private var searchBtn: MaterialCardView? = null
    private var searchBtnBack: MaterialCardView? = null
    private var loadingHolder: LinearLayout? = null
    private var progressBar: WP7ProgressBar? = null
    private var contxt: Context? = null

    private var dbCall: AppDao? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.all_apps_screen, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar!!.setIndicatorRadius(5)
        progressBar!!.showProgressBar()
        recyclerView = view.findViewById(R.id.app_list)
        searchBtn = view.findViewById(R.id.searchButton)
        searchBtnBack = view.findViewById(R.id.searchBackBtn)
        search = view.findViewById(R.id.search)
        loadingHolder = view.findViewById(R.id.loadingHolder)
        setRecyclerPadding(resources.getDimensionPixelSize(R.dimen.recyclerViewPadding))
        searchBtn!!.setOnClickListener { searchFunction() }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contxt = activity
        if(contxt == null) {
            return
        }
        CoroutineScope(Dispatchers.Default).launch {
            dbCall = AppData.getAppData(contxt!!).getAppDao()
            mApps = ArrayList()
            val imageLoader = ImageLoader.Builder(contxt!!)
                    .interceptorDispatcher(Dispatchers.Default)
                    .memoryCache {
                        MemoryCache.Builder(contxt!!)
                                .maxSizePercent(0.25)
                                .build()
                    }
                    .diskCache {
                        DiskCache.Builder()
                                .directory(contxt!!.cacheDir.resolve("cache"))
                                .maxSizePercent(0.25)
                                .build()
                    }
                    .build()
            appAdapter = AppAdapter(mApps!!, contxt!!.packageManager, dbCall!!, contxt!!, imageLoader)
            appsList = setUpApps(contxt!!.packageManager)
            getHeaderListLatter(appsList)
            requireActivity().runOnUiThread {
                recyclerView!!.setLayoutManager(LinearLayoutManager(contxt))
                recyclerView!!.adapter = appAdapter
                OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
                searchBtnBack!!.setOnClickListener {
                    searchBtn!!.visibility = View.VISIBLE
                    search!!.visibility = View.GONE
                    searchBtnBack!!.visibility = View.GONE
                    setRecyclerPadding(resources.getDimensionPixelSize(R.dimen.recyclerViewPadding))
                    CoroutineScope(Dispatchers.Default).launch {
                        mApps = null
                        mApps = ArrayList()
                        appsList = setUpApps(contxt!!.packageManager)
                        getHeaderListLatter(appsList)
                        runBlocking {
                            requireActivity().runOnUiThread {
                                appAdapter!!.setNewFilteredList(mApps!!)
                            }
                        }
                    }
                }
                hideLoadingHolder()
            }
        }
    }
    private fun hideLoadingHolder() {
        progressBar!!.hideProgressBar()
        progressBar = null
        loadingHolder!!.visibility = View.GONE
        loadingHolder = null
        recyclerView!!.visibility = View.VISIBLE
    }
    private fun setRecyclerPadding(pad: Int) {
        recyclerView!!.setPadding(pad, 0, 0 ,0)
    }
    private fun searchFunction() {
        searchBtn!!.visibility = View.GONE
        search!!.visibility = View.VISIBLE
        search!!.isFocusable = true
        searchBtnBack!!.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Default).launch {
            requireActivity().runOnUiThread {
                setRecyclerPadding(0)
                removeHeaders()
            }
            (search!!.editText as? AutoCompleteTextView)?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    filterText(s.toString())
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {
                }

                override fun afterTextChanged(s: Editable) {
                }
            })
        }
    }
    private fun filterText(text: String) {
        val filteredlist: ArrayList<App> = ArrayList()
        for (item in appsList!!) {
            if (item.appLabel!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isNotEmpty()) {
            appAdapter?.setNewFilteredList(filteredlist)
        }
    }

    private fun getHeaderListLatter(newApps: List<App>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (newApps != null) {
                Collections.sort(newApps, Comparator.comparing { app: App -> app.appLabel!![0].toString().uppercase(Locale.getDefault()) })
            }
        } else {
            if (newApps != null) {
                Collections.sort(newApps) { app1: App, app2: App -> app1.appLabel!![0].toString().uppercase(Locale.getDefault()).compareTo(app2.appLabel!![0].toString().uppercase(Locale.getDefault())) }
            }
        }
        var lastHeader: String? = ""
        for (app in newApps!!) {
            val header = app.appLabel!![0].toString().uppercase(Locale.getDefault())
            if (!TextUtils.equals(lastHeader, header)) {
                lastHeader = header
                val head = App()
                head.appLabel = header
                head.isSection = true
                mApps!!.add(head)
            }
            mApps!!.add(app)
        }
    }
    private fun removeHeaders() {
        var temp = mApps!!.size
        while (temp != 0) {
            temp -= 1
            val item = mApps!![temp]
            if (item.isSection) {
                mApps!!.remove(item)
            }
        }
        appAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        appAdapter?.notifyDataSetChanged()
    }
    inner class AppAdapter internal constructor(private var adapterApps: MutableList<App>, private val packageManager: PackageManager, private val dbCall: AppDao, private val context: Context, private val imageLoader: ImageLoader) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
        private var iconSize = resources.getDimensionPixelSize(R.dimen.iconAppsListSize)

        private val appsListReserved: MutableList<App> = adapterApps

        fun setNewFilteredList(appListFiltered: MutableList<App>) {
            adapterApps = appListFiltered
            notifyDataSetChanged()
        }

        fun restoreAppList() {
            adapterApps = appsListReserved
            getHeaderListLatter(appsList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == Companion.SECTION_VIEW) {
                SectionHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.abc, parent, false))
            } else {
                AppHolder(LayoutInflater.from(parent.context).inflate(R.layout.app, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (Companion.SECTION_VIEW == getItemViewType(position)) {
                val sectionHeaderViewHolder = holder as SectionHeaderViewHolder
                val sectionItem: App = adapterApps[position]
                sectionHeaderViewHolder.headerTitleTextview.text = sectionItem.appLabel
                return
            }
            val holder1 = holder as AppHolder
            val app: App = adapterApps[position]
            try {
                val bmp = packageManager.getApplicationIcon(app.appPackage!!).toBitmap(iconSize, iconSize, PREFS!!.iconBitmapConfig())
                val request = ImageRequest.Builder(context)
                        .data(bmp)
                        .crossfade(true)
                        .target(holder.icon)
                        .build()
                imageLoader.enqueue(request)
            } catch (e: PackageManager.NameNotFoundException) {
                adapterApps.removeAt(position)
                notifyDataSetChanged()
            }
            holder1.label.text = app.appLabel
            holder1.itemView.setOnClickListener {
                runApp(app.appPackage!!)
            }
            holder1.itemView.setOnLongClickListener {
                showPopupWindow(holder1.itemView, app.appPackage!!, app.appLabel!!)
                true
            }
        }
        private fun showPopupWindow(view: View, appPackage: String, label: String) {
            val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.all_apps_window, null, false)
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            view.scaleX = 1.2f
            view.scaleY = 1.2f
            recyclerView!!.scaleX = 0.95f
            recyclerView!!.scaleY = 0.95f
            val popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow.animationStyle = R.style.enterStyle
            popupWindow.showAsDropDown(view, 0, 0)
            val pin = popupView.findViewById<MaterialCardView>(R.id.pinApp)
            val uninstall = popupView.findViewById<MaterialCardView>(R.id.uninstallApp)
            pin.setOnClickListener {
                insertNewApp(label, appPackage)
                popupWindow.dismiss()
                activity!!.onBackPressed()
            }
            uninstall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.setData(Uri.parse("package:$appPackage"))
                popupWindow.dismiss()
                startActivity(intent)
            }
            popupWindow.setOnDismissListener {
                view.scaleX = 1f
                view.scaleY = 1f
                recyclerView!!.scaleX = 1f
                recyclerView!!.scaleY = 1f
            }
        }
        private fun runApp(packag: String) {
            when (packag) {
                "ru.dimon6018.metrolauncher" -> {
                    startActivity(Intent(requireActivity(), SettingsActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
                else -> {
                    val intent = contxt!!.packageManager.getLaunchIntentForPackage(packag)
                    intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }
        private fun insertNewApp(text: String, packag: String) {
            CoroutineScope(Dispatchers.Default).launch {
                var pos = dbCall.getJustAppsWithoutPlaceholders(false).size
                pos += 1
                if(dbCall.getApp(pos).isPlaceholder == false) {
                    pos += 1
                }
                val id = Random.nextInt(1000, 20000)
                val item = AppEntity(pos, id, -1, false, "small", text, packag)
                dbCall.insertItem(item)
                tileList = dbCall.getJustApps()
            }
        }
        override fun getItemViewType(position: Int): Int {
            return if (adapterApps[position].isSection) {
                Companion.SECTION_VIEW
            } else {
                Companion.CONTENT_VIEW
            }
        }

        override fun getItemCount(): Int {
            return adapterApps.size
        }

        inner class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView
            val label: TextView

            init {
                icon = itemView.findViewById(R.id.app_icon)
                label = itemView.findViewById(R.id.app_label)
            }
        }

        inner class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var headerTitleTextview: MaterialTextView

            init {
                headerTitleTextview = itemView.findViewById(R.id.abc_label)
            }
        }
    }
    companion object {
        const val SECTION_VIEW = 0
        const val CONTENT_VIEW = 1

        private var appsList: MutableList<App>? = null
    }
}
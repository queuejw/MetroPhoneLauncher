package ru.dimon6018.metrolauncher.content

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
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
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.disk.DiskCache
import coil.load
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import ir.alirezabdn.wp7progress.WP7ProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.recompressIcon
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.App
import ru.dimon6018.metrolauncher.content.data.AppDao
import ru.dimon6018.metrolauncher.content.data.AppData
import ru.dimon6018.metrolauncher.content.data.AppEntity
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.helpers.WPDialog
import java.lang.NullPointerException
import java.util.Collections
import java.util.Locale
import kotlin.random.Random

class NewAllApps: Fragment() {

    private var recyclerView: RecyclerView? = null
    private var search: TextInputLayout? = null
    private var adapter: AppAdapter? = null
    private var searchBtn: MaterialCardView? = null
    private var searchBtnBack: MaterialCardView? = null
    private var loadingHolder: LinearLayout? = null
    private var progressBar: WP7ProgressBar? = null
    private var appList: MutableList<App>? = null

    private var isSearching = false
    private var pm: PackageManager? = null

    private var contextFragment: Context? = null
    private var currentActivity: Activity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.all_apps_screen, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar!!.showProgressBar()
        contextFragment = requireContext()
        currentActivity = requireActivity()
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.app_list)
        searchBtn = view.findViewById(R.id.allAppsButton)
        searchBtnBack = view.findViewById(R.id.searchBackBtn)
        search = view.findViewById(R.id.search)
        loadingHolder = view.findViewById(R.id.loadingHolder)
        searchBtn!!.setOnClickListener { searchFunction() }
        setRecyclerPadding(resources.getDimensionPixelSize(R.dimen.recyclerViewPadding))
        CoroutineScope(Dispatchers.Default).launch {
            val dbCall = AppData.getAppData(contextFragment!!).getAppDao()
            pm = contextFragment!!.packageManager
            appList = getHeaderListLatter(Application.setUpApps(pm!!))
            adapter = AppAdapter(appList!!, contextFragment!!.resources, dbCall, contextFragment!!)
            val lm = LinearLayoutManager(contextFragment)
            currentActivity?.runOnUiThread {
                recyclerView!!.layoutManager = lm
                recyclerView!!.adapter = adapter
                OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
                searchBtnBack!!.setOnClickListener {
                    disableSearch()
                }
            }
            runBlocking {
                currentActivity?.runOnUiThread {
                    progressBar!!.hideProgressBar()
                    progressBar = null
                    loadingHolder!!.visibility = View.GONE
                    loadingHolder = null
                    recyclerView!!.visibility = View.VISIBLE
                }
            }
        }
    }
    override fun onPause() {
        if(isSearching) {
            disableSearch()
        }
        super.onPause()
    }
    private fun disableSearch() {
        isSearching = false
        searchBtn!!.visibility = View.VISIBLE
        search!!.visibility = View.GONE
        searchBtnBack!!.visibility = View.GONE
        setRecyclerPadding(resources.getDimensionPixelSize(R.dimen.recyclerViewPadding))
        CoroutineScope(Dispatchers.IO).launch {
            if(pm == null) {
                pm = currentActivity?.packageManager
            }
            appList = getHeaderListLatter(Application.setUpApps(pm!!))
            runBlocking {
                currentActivity?.runOnUiThread {
                    adapter?.setData(appList!!, true)
                }
            }
        }
    }
    private fun setRecyclerPadding(pad: Int) {
        recyclerView!!.setPadding(pad, 0, 0 ,0)
    }
    private fun searchFunction() {
        isSearching = true
        searchBtn!!.visibility = View.GONE
        search!!.visibility = View.VISIBLE
        search!!.isFocusable = true
        searchBtnBack!!.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            currentActivity?.runOnUiThread {
                setRecyclerPadding(0)
                removeHeaders()
            }
            (search!!.editText as? AutoCompleteTextView)?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    filterText(s.toString())
                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {}
            })
        }
    }
    private fun filterText(text: String) {
        val filteredlist: ArrayList<App> = ArrayList()
        val locale = Locale.getDefault()
        if(appList == null) {
            try {
                appList = Application.setUpApps(pm!!)
                adapter?.setData(appList!!, true)
            } catch (e: NullPointerException) {
                if (contextFragment != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        Application.saveError(e.toString(), BSOD.getData(contextFragment!!))
                    }
                    WPDialog(contextFragment!!).setTopDialog(false)
                            .setMessage(getString(R.string.error))
                            .setPositiveButton(getString(android.R.string.ok), null)
                            .show()
                }
            }
            return
        }
        for (item in appList!!) {
            if (item.appLabel!!.lowercase(locale).contains(text.lowercase(locale))) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isNotEmpty()) {
            adapter?.setData(filteredlist, true)
        }
    }
    private fun removeHeaders() {
        if(appList == null) {
            return
        }
        var temp = appList!!.size
        while (temp != 0) {
            temp -= 1
            val item = appList!![temp]
            if(item.type == 1) {
                appList!!.remove(item)
            }
        }
        adapter?.setData(appList!!,true)
    }
    private fun getHeaderListLatter(newApps: MutableList<App>): MutableList<App> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(newApps, Comparator.comparing { app: App -> app.appLabel!![0].lowercase(Locale.getDefault()) })
        } else {
            newApps.sortWith { app1: App, app2: App -> app1.appLabel!![0].lowercase(Locale.getDefault()).compareTo(app2.appLabel!![0].lowercase(Locale.getDefault())) }
        }
        var lastHeader: String? = ""
        val list: MutableList<App> = ArrayList()
        for (i in 0..<newApps.size) {
            val app = newApps[i]
            val header = app.appLabel!![0].lowercase(Locale.getDefault())
            if (!TextUtils.equals(lastHeader, header)) {
                lastHeader = header
                val head = App()
                head.appLabel = header
                head.type = 1
                list.add(head)
            }
            list.add(app)
        }
        return list
    }
    inner class AppAdapter(private var list: MutableList<App>, resources: Resources, private val dbCall: AppDao, val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val letter: Int = 0
        private val appHolder: Int = 1
        private var iconSize = resources.getDimensionPixelSize(R.dimen.iconAppsListSize)
        private val imageLoader = ImageLoader.Builder(context)
        .memoryCache {
            MemoryCache.Builder(context)
                    .maxSizePercent(0.1)
                    .build()
        }
        .diskCache {
            DiskCache.Builder()
                    .directory(context.cacheDir.resolve("cache"))
                    .maxSizePercent(0.05)
                    .build()
        }
        .diskCachePolicy(CachePolicy.ENABLED)
        .bitmapConfig(PREFS!!.iconBitmapConfig())
        .dispatcher(Dispatchers.Default)
        .build()

        fun setData(new: MutableList<App>, refresh: Boolean) {
            list = new
            if(refresh) {
                notifyDataSetChanged()
            }
        }
        private fun regenerate() {
            val newAppList = getHeaderListLatter(Application.setUpApps(pm!!))
            setData(newAppList, true)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == letter) {
                LetterHolder(LayoutInflater.from(parent.context).inflate(R.layout.abc, parent, false))
            } else {
                AppHolder(LayoutInflater.from(parent.context).inflate(R.layout.app, parent, false))
            }
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val app = list[position]
            when(holder.itemViewType) {
                letter -> {
                    (holder as LetterHolder).textView.text = app.appLabel
                    return
                }
                appHolder -> {
                    bindAppHolder((holder as AppHolder), app)
                    return
                }
            }
        }
        private fun bindAppHolder(holder: AppHolder, app: App) {
            try {
                val request = ImageRequest.Builder(context)
                        .data(pm?.getApplicationIcon(app.appPackage!!)!!.toBitmap(iconSize, iconSize))
                        .target(holder.icon)
                        .build()
                imageLoader.enqueue(request)
            } catch (e: PackageManager.NameNotFoundException) {
            }
            holder.label.text = app.appLabel
            holder.itemView.setOnClickListener {
                runApp(app.appPackage!!)
            }
            holder.itemView.setOnLongClickListener {
                showPopupWindow(holder.itemView, app.appPackage!!, app.appLabel!!)
                true
            }
        }
        private fun showPopupWindow(view: View, appPackage: String, label: String) {
            val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.all_apps_window, null, false)
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow.isFocusable = true
            popupView.elevation = 4f
            popupWindow.animationStyle = R.style.enterStyle
            popupWindow.showAsDropDown(view, 0, 0)
            val pin = popupView.findViewById<MaterialCardView>(R.id.pinApp)
            val uninstall = popupView.findViewById<MaterialCardView>(R.id.uninstallApp)
            pin.setOnClickListener {
                insertNewApp(label, appPackage)
                popupWindow.dismiss()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            uninstall.setOnClickListener {
                popupWindow.dismiss()
                startActivity(Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:$appPackage")))
            }
            popupWindow.setOnDismissListener {
            }
        }
        private fun runApp(packag: String) {
            when (packag) {
                "ru.dimon6018.metrolauncher" -> {
                    startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                }
                else -> {
                    startActivity(Intent(pm!!.getLaunchIntentForPackage(packag)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
        private fun insertNewApp(text: String, packag: String) {
            CoroutineScope(Dispatchers.IO).launch {
                val dBlist = dbCall.getJustApps()
                var pos = 0
                for (i in 0..<dBlist.size) {
                    if (dBlist[i].tileType == -1) {
                        pos = i
                        break
                    }
                }
                val id = Random.nextLong(1000, 2000000)
                val item = AppEntity(pos, id, -1, 0,
                    isSelected = false,
                    appSize = "small",
                    appLabel = text,
                    appPackage = packag
                )
                dbCall.insertItem(item)
                runBlocking {
                    currentActivity?.runOnUiThread {
                        (currentActivity as Main).openStart()
                    }
                }
            }
        }
        override fun getItemCount(): Int {
            return list.size
        }
        override fun getItemViewType(position: Int): Int {
            return when(list[position].type) {
                0 -> appHolder
                1 -> letter
                else -> appHolder
            }
        }
    }
    class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val label: MaterialTextView = itemView.findViewById(R.id.app_label)
    }
    class LetterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: MaterialTextView = itemView.findViewById(R.id.abc_label)
    }
}
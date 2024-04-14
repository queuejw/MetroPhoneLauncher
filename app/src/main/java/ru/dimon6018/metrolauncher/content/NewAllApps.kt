package ru.dimon6018.metrolauncher.content

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.target
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
import ru.dimon6018.metrolauncher.Application.Companion.isAppOpened
import ru.dimon6018.metrolauncher.Application.Companion.isStartMenuOpened
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.App
import ru.dimon6018.metrolauncher.content.data.AppDao
import ru.dimon6018.metrolauncher.content.data.AppData
import ru.dimon6018.metrolauncher.content.data.AppEntity
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.helpers.IconPackManager
import ru.dimon6018.metrolauncher.helpers.WPDialog
import java.util.Collections
import java.util.Locale
import kotlin.random.Random
class NewAllApps: Fragment() {

    private var recyclerView: RecyclerView? = null

    private var recyclerViewAlphabet: RecyclerView? = null
    private var alphabetLayout: LinearLayout? = null

    private var search: TextInputLayout? = null

    private var adapter: AppAdapter? = null
    private var adapterAlphabet: AlphabetAdapter? = null

    private var searchBtn: MaterialCardView? = null
    private var searchBtnBack: MaterialCardView? = null
    private var loadingHolder: LinearLayout? = null
    private var progressBar: WP7ProgressBar? = null
    private var appList: MutableList<App>? = null

    private var isSearching = false
    private var pm: PackageManager? = null

    private var contextFragment: Context? = null
    private var currentActivity: Activity? = null

    private var isAlphabetVisible = false

    var scrollPoints: MutableList<Int> = ArrayList()

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
        recyclerViewAlphabet = view.findViewById(R.id.alphabet_list)
        alphabetLayout = view.findViewById(R.id.alphabetLayout)
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
            adapter = AppAdapter(appList!!, contextFragment!!.resources, dbCall)
            val lm = LinearLayoutManager(contextFragment)
            setAlphabetRecyclerView()
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
    private fun setAlphabetRecyclerView() {
        adapterAlphabet = AlphabetAdapter(getAlphabetList())
        val lm = GridLayoutManager(contextFragment!!, 4)
        currentActivity?.runOnUiThread {
            alphabetLayout!!.setOnClickListener {
                hideAlphabet()
            }
            recyclerViewAlphabet!!.setOnClickListener {
                hideAlphabet()
            }
            recyclerViewAlphabet!!.adapter = adapterAlphabet
            recyclerViewAlphabet!!.layoutManager = lm
            recyclerViewAlphabet!!.addItemDecoration(MarginItemDecoration(8))
        }
    }
    private fun showAlphabet() {
        recyclerView!!.alpha = 0.7f
        isAlphabetVisible = true
        alphabetLayout!!.visibility = View.VISIBLE
        adapterAlphabet?.setNewData(getAlphabetList())
        (currentActivity as Main).hideNavBar()
    }
    private fun hideAlphabet() {
        recyclerView!!.alpha = 1f
        isAlphabetVisible = false
        recyclerViewAlphabet!!.scrollToPosition(0)
        alphabetLayout!!.visibility = View.GONE
        (currentActivity as Main).showNavBar()
    }
    private fun getAlphabetList(): MutableList<AlphabetLetter> {
        val alphabetList: MutableList<AlphabetLetter> = ArrayList()
        var ch = 'A'
        while (ch <= 'Z') {
            val a = AlphabetLetter()
            a.letter = ch.lowercase()
            alphabetList.add(a)
            ch++
        }
        if (Locale.getDefault().language.equals("ru")) {
            val alphabet: CharArray? = Character.toChars('А'.code)
            for (i in 0..31) {
                val a = AlphabetLetter()
                a.letter = alphabet!![0].lowercase()
                alphabetList.add(a)
                alphabet[0]++
            }
        }
        if (appList != null) {
            var pos = 0
            var posInList = 0
            for(i in appList!!) {
                alphabetList.forEach {
                    if (i.appLabel == it.letter) {
                        it.isActive = true
                        it.posInList = posInList
                        scrollPoints.add(posInList, pos)
                        posInList += 1
                        return@forEach
                    }
                }
                pos += 1
            }
        }
        return alphabetList
    }
    override fun onPause() {
        if(isSearching) {
            disableSearch()
        }
        if(isAlphabetVisible) {
            hideAlphabet()
        }
        super.onPause()
    }

    override fun onResume() {
        if(isAppOpened && !isStartMenuOpened) {
            Log.d("resumeStart", "start enter animation")
            //TODO add normal animation
            isAppOpened = false
        }
        super.onResume()
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
            removeHeaders()
            currentActivity?.runOnUiThread {
                setRecyclerPadding(0)
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
        currentActivity?.runOnUiThread {
            adapter?.setData(appList!!, true)
        }
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
    inner class AppAdapter(private var list: MutableList<App>, resources: Resources, private val dbCall: AppDao): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val letter: Int = 0
        private val appHolder: Int = 1
        private var iconSize = resources.getDimensionPixelSize(R.dimen.iconAppsListSize)
        private var iconManager: IconPackManager? = null
        private val imageLoader = ImageLoader.Builder(contextFragment!!)
        .memoryCache {
            MemoryCache.Builder()
                    .maxSizePercent(contextFragment!!, 0.25)
                    .build()
        }
        .diskCache {
            DiskCache.Builder()
                    .maxSizePercent(0.25)
                    .build()
        }
        .build()
        init {
            if (PREFS!!.iconPackPackage != "") {
                iconManager = IconPackManager()
                iconManager!!.setContext(context)
            }
        }
        fun setData(new: MutableList<App>, refresh: Boolean) {
            list = new
            if(refresh) {
                notifyDataSetChanged()
            }
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
                    holder.itemView.setOnClickListener {
                        showAlphabet()
                    }
                }
                appHolder -> {
                    bindAppHolder((holder as AppHolder), app, position)
                }
            }
        }
        private fun bindAppHolder(holder: AppHolder, app: App, position: Int) {
            try {
                val request = ImageRequest.Builder(contextFragment!!)
                        .data(if(PREFS!!.iconPackPackage == "null") pm?.getApplicationIcon(app.appPackage!!)!!.toBitmap(iconSize, iconSize) else iconManager?.getIconPackWithName(PREFS!!.iconPackPackage)?.getDrawableIconForPackage(app.appPackage, pm?.getApplicationIcon(app.appPackage!!))?.toBitmap(iconSize, iconSize)!!)
                        .target(holder.icon)
                        .build()
                imageLoader.enqueue(request)
            } catch (e: PackageManager.NameNotFoundException) {
                list.remove(app)
                recyclerView!!.stopScroll()
                notifyItemRemoved(position)
            }
            holder.label.text = app.appLabel
            holder.itemView.setOnClickListener {
                try {
                    isAppOpened = true
                    runApp(app.appPackage!!)
                } catch (e: Exception) {
                    Toast.makeText(contextFragment!!, getString(R.string.app_opening_error), Toast.LENGTH_SHORT).show()
                    recyclerView!!.stopScroll()
                    list.remove(app)
                    notifyItemRemoved(position)
                }
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
    inner class AlphabetAdapter(private var alphabetList: MutableList<AlphabetLetter>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val activeLetter = 10
        private val activeDrawable = Application.launcherAccentColor(currentActivity!!.theme).toDrawable()
        private val disabledLetter = 10
        private val disabledDrawable = ContextCompat.getColor(contextFragment!!, R.color.darkGray).toDrawable()
        private val size = resources.getDimensionPixelSize(R.dimen.alphabetHolderSize)
        private val params = ViewGroup.LayoutParams(size, size)

        fun setNewData(new: MutableList<AlphabetLetter>) {
            alphabetList = new
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return AlphabetLetterHolder(LayoutInflater.from(parent.context).inflate(R.layout.abc_alphabet, parent, false))
        }

        override fun getItemCount(): Int {
            return alphabetList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = alphabetList[position]
            holder as AlphabetLetterHolder
            holder.textView.text = item.letter
            holder.itemView.layoutParams = params
            if(item.isActive) {
                holder.backgroundView.background = activeDrawable
            } else {
                holder.backgroundView.background = disabledDrawable
            }
            if(item.isActive) {
                holder.itemView.setOnClickListener {
                    startAnimator()
                    val scroll = scrollPoints[item.posInList]
                    if(scroll > adapter!!.itemCount) {
                        recyclerView!!.smoothScrollToPosition(adapter!!.itemCount)
                    } else {
                        recyclerView!!.smoothScrollToPosition(scroll)
                    }
                }
            }
        }
        private fun startAnimator() {
            hideAlphabet()
        }
        override fun getItemViewType(position: Int): Int {
            return if(alphabetList[position].isActive) activeLetter else disabledLetter
        }
    }
    class AlphabetLetter {
        var letter: String = ""
        var isActive: Boolean = false
        var posInList: Int = 0
    }
    class AlphabetLetterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: MaterialTextView = itemView.findViewById(R.id.alphabetLetter)
        var backgroundView: View = itemView.findViewById(R.id.alphabetBackground)
    }
    class MarginItemDecoration(private val spaceSize: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView,
                state: RecyclerView.State
        ) {
            with(outRect) {
                top = spaceSize
                left = spaceSize
                right = spaceSize
                bottom = spaceSize
            }
        }
    }
}
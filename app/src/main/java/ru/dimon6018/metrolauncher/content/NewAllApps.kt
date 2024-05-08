package ru.dimon6018.metrolauncher.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import ir.alirezabdn.wp7progress.WP7ProgressBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isAppOpened
import ru.dimon6018.metrolauncher.Application.Companion.isStartMenuOpened
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.apps.App
import ru.dimon6018.metrolauncher.content.data.apps.AppDao
import ru.dimon6018.metrolauncher.content.data.apps.AppData
import ru.dimon6018.metrolauncher.content.data.apps.AppEntity
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.helpers.IconPackManager
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.receivers.PackageChangesReceiver
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.generateRandomTileSize
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.recompressIcon
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.saveError
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setUpApps
import java.util.Collections
import java.util.Locale
import kotlin.random.Random


class NewAllApps: Fragment() {

    private var recyclerView: RecyclerView? = null

    private var recyclerViewAlphabet: RecyclerView? = null
    private var alphabetLayout: LinearLayout? = null

    private var search: TextInputLayout? = null

    private lateinit var appAdapter: AppAdapter
    private lateinit var adapterAlphabet: AlphabetAdapter
    private lateinit var pm: PackageManager

    private var searchBtn: MaterialCardView? = null
    private var searchBtnBack: MaterialCardView? = null
    private var settingsBtn: MaterialCardView? = null

    private var loadingHolder: LinearLayout? = null
    private var progressBar: WP7ProgressBar? = null
    private var loadingText: TextView? = null
    private var appList: MutableList<App>? = null

    private var isSearching = false

    private var contextFragment: Context? = null

    private var isAlphabetVisible = false

    var scrollPoints: MutableList<Int> = ArrayList()

    private var packageBroadcastReceiver: BroadcastReceiver? = null

    private val hashCache = ArrayMap<String, Icon?>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextFragment = context
        pm = context.packageManager
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.all_apps_screen, container, false)
        val frame = view.findViewById<FrameLayout>(R.id.frame)
        if(PREFS!!.isAllAppsBackgroundEnabled) {
            frame.background = ContextCompat.getColor(contextFragment!!, R.color.transparent).toDrawable()
        } else {
            frame.background = if(PREFS!!.isLightThemeUsed) ContextCompat.getColor(contextFragment!!, android.R.color.background_light).toDrawable() else ContextCompat.getColor(contextFragment!!, android.R.color.background_dark).toDrawable()
        }
        progressBar = view.findViewById(R.id.progressBar)
        loadingText = view.findViewById(R.id.loadingText)
        progressBar!!.showProgressBar()
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
        settingsBtn = view.findViewById(R.id.settingsBtn)
        settingsBtn!!.setOnClickListener {
            activity?.startActivity(Intent(requireActivity(), SettingsActivity::class.java))
        }
        if(!PREFS!!.isSettingsBtnEnabled) {
            settingsBtn!!.visibility = View.GONE
        } else {
            settingsBtn!!.visibility = View.VISIBLE
        }
        searchBtn!!.setOnClickListener { searchFunction() }
        setRecyclerPadding(resources.getDimensionPixelSize(R.dimen.recyclerViewPadding))
        lifecycleScope.launch(Dispatchers.Default) {
            if(contextFragment == null) {
                contextFragment = context
            }
            val dbCall = AppData.getAppData(contextFragment!!).getAppDao()
            appList = getHeaderListLatter(setUpApps(pm, contextFragment!!))
            var iconManager: IconPackManager? = null
            var isCustomIconsInstalled = false
            if (PREFS!!.iconPackPackage != "null") {
                iconManager = IconPackManager()
                iconManager.setContext(contextFragment!!)
                isCustomIconsInstalled = true
            }
            val iconSize = contextFragment!!.resources.getDimensionPixelSize(R.dimen.iconAppsListSize)
            appList?.forEach {
                if (it.type != 1) {
                    var bmp = if (!isCustomIconsInstalled) recompressIcon(
                        pm.getApplicationIcon(it.appPackage!!).toBitmap(iconSize, iconSize),
                        75
                    )
                    else
                        recompressIcon(
                            iconManager?.getIconPackWithName(PREFS!!.iconPackPackage)
                                ?.getDrawableIconForPackage(it.appPackage!!, null)
                                ?.toBitmap(iconSize, iconSize), 75
                        )
                    if (bmp == null) {
                        bmp = recompressIcon(
                            pm.getApplicationIcon(it.appPackage!!).toBitmap(iconSize, iconSize),
                            75
                        )
                    }
                    hashCache[it.appPackage] = bmp
                }
            }
            appAdapter = AppAdapter(appList!!, dbCall)
            val lm = LinearLayoutManager(contextFragment)
            setAlphabetRecyclerView()
            withContext(Dispatchers.Main) {
                searchBtnBack!!.setOnClickListener {
                    disableSearch()
                }
                recyclerView?.apply {
                    layoutManager = lm
                    adapter = appAdapter
                    OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
                    visibility = View.VISIBLE
                }
                progressBar?.hideProgressBar()
                loadingText?.visibility = View.GONE
            }
        }
        registerBroadcast()
    }
    private fun registerBroadcast() {
        Log.d("AllApps", "register")
        packageBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d("AllApps", "on receive")
                val packageName = intent.getStringExtra("package")
                val action = intent.getIntExtra("action", 42)
                // End early if it has anything to do with us.
                if (! packageName.isNullOrEmpty() && packageName.contains(requireContext().packageName)) return
                if (action == PackageChangesReceiver.PACKAGE_REMOVED || action == PackageChangesReceiver.PACKAGE_INSTALLED || action == PackageChangesReceiver.PACKAGE_UPDATED) {
                    broadcastListUpdater()
                }
            }
        }
        // We want this fragment to receive the package change broadcast,
        // since otherwise it won't be notified when there are changes to that.
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
    private fun broadcastListUpdater() {
        appList = getHeaderListLatter(setUpApps(pm, contextFragment!!))
        appAdapter.setData(appList!!, true)
    }
    private fun unregisterBroadcast() {
        packageBroadcastReceiver?.apply {
            requireActivity().unregisterReceiver(packageBroadcastReceiver)
            packageBroadcastReceiver = null
        } ?: run {
            Log.d("AllApps", "unregisterBroadcast() was called to a null receiver.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterBroadcast()
    }
    private fun setAlphabetRecyclerView() {
        if(contextFragment == null) {
            contextFragment = context
        }
        adapterAlphabet = AlphabetAdapter(getAlphabetList())
        val lm = GridLayoutManager(contextFragment!!, 4)
        activity?.runOnUiThread {
            alphabetLayout?.setOnClickListener {
                hideAlphabet()
            }
            recyclerViewAlphabet?.apply {
                layoutManager = lm
                adapter = adapterAlphabet
                addItemDecoration(Utils.MarginItemDecoration(8))
                setOnClickListener {
                    hideAlphabet()
                }
            }
        }
    }
    private fun showAlphabet() {
        recyclerView!!.alpha = 0.7f
        isAlphabetVisible = true
        alphabetLayout!!.visibility = View.VISIBLE
        adapterAlphabet.setNewData(getAlphabetList())
    }
    private fun hideAlphabet() {
        recyclerView!!.alpha = 1f
        isAlphabetVisible = false
        recyclerViewAlphabet!!.scrollToPosition(0)
        alphabetLayout!!.visibility = View.GONE
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
        if (getSupportedRuLang()) {
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
    private fun getSupportedRuLang(): Boolean {
        return when(Locale.getDefault().language) {
            "ru" -> true
            "ru_BY" -> true
            "ru_KZ" -> true
            "be" -> true
            "be_BY" -> true
            else -> false
        }
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
        if(!PREFS!!.isSettingsBtnEnabled) {
            settingsBtn!!.visibility = View.GONE
        } else {
            settingsBtn!!.visibility = View.VISIBLE
        }
        setRecyclerPadding(resources.getDimensionPixelSize(R.dimen.recyclerViewPadding))
        progressBar!!.showProgressBar()
        recyclerView?.alpha = 0.5f
        lifecycleScope.launch(Dispatchers.IO) {
            appList = getHeaderListLatter(setUpApps(pm, contextFragment!!))
            withContext(Dispatchers.Main) {
                appAdapter.setData(appList!!, true)
                progressBar!!.hideProgressBar()
                recyclerView?.alpha = 1f
            }
        }
    }
    private fun setRecyclerPadding(pad: Int) {
        recyclerView!!.setPadding(pad, 0, 0 ,0)
    }
    private fun searchFunction() {
        isSearching = true
        searchBtn!!.visibility = View.GONE
        settingsBtn!!.visibility = View.GONE
        search!!.visibility = View.VISIBLE
        search!!.isFocusable = true
        searchBtnBack!!.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            removeHeaders()
            activity?.runOnUiThread {
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
                appList = setUpApps(pm, contextFragment!!)
                appAdapter.setData(appList!!, true)
            } catch (e: NullPointerException) {
                if (contextFragment != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        saveError(e.toString(), BSOD.getData(contextFragment!!))
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
            appAdapter.setData(filteredlist, true)
        }
    }
    private fun removeHeaders() {
        if(appList == null) {
            return
        }
        progressBar!!.showProgressBar()
        lifecycleScope.launch(Dispatchers.IO) {
            var temp = appList!!.size
            while (temp != 0) {
                temp -= 1
                val item = appList!![temp]
                if (item.type == 1) {
                    appList!!.remove(item)
                }
            }
            withContext(Dispatchers.Main) {
                appAdapter.setData(appList!!, true)
                progressBar!!.hideProgressBar()
            }
        }
    }
    private fun getHeaderListLatter(newApps: MutableList<App>): MutableList<App> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(newApps, Comparator.comparing { app: App -> app.appLabel!![0].lowercase(Locale.getDefault()) })
        } else {
            newApps.sortWith { app1: App, app2: App -> app1.appLabel!![0].lowercase(Locale.getDefault()).compareTo(app2.appLabel!![0].lowercase(Locale.getDefault())) }
        }
        if(!PREFS!!.isAlphabetEnabled) {
            return newApps
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
    open inner class AppAdapter(var list: MutableList<App>, private val dbCall: AppDao): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val letter: Int = 0
        private val appHolder: Int = 1
        private var accentColor: Int = 0

        init {
            if (PREFS!!.isAllAppsBackgroundEnabled) {
                accentColor = launcherAccentColor(requireActivity().theme)
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
                    bindAppHolder((holder as AppHolder), app)
                }
            }
        }
        private fun bindAppHolder(holder: AppHolder, app: App) {
            holder.icon.setImageIcon(hashCache[app.appPackage])
            holder.label.text = app.appLabel
        }
        private fun showPopupWindow(view: View, appPackage: String, label: String) {
            val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.all_apps_window, null, false)
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow.isFocusable = true
            popupWindow.animationStyle = R.style.enterStyle
            popupWindow.showAsDropDown(view, 0, 0)
            val pin = popupView.findViewById<MaterialCardView>(R.id.pinApp)
            val uninstall = popupView.findViewById<MaterialCardView>(R.id.uninstallApp)
            val info = popupView.findViewById<MaterialCardView>(R.id.infoApp)
            pin.setOnClickListener {
                insertNewApp(label, appPackage)
                popupWindow.dismiss()
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
            uninstall.setOnClickListener {
                popupWindow.dismiss()
                startActivity(Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:$appPackage")))
            }
            info.setOnClickListener {
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:$appPackage")))
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
                    startActivity(Intent(pm.getLaunchIntentForPackage(packag)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
                    tileSize = generateRandomTileSize(true),
                    appLabel = text,
                    appPackage = packag
                )
                dbCall.insertItem(item)
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
        inner class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.app_icon)
            val label: MaterialTextView = itemView.findViewById(R.id.app_label)
            init {
                if(PREFS!!.isAllAppsBackgroundEnabled) {
                    label.setTextColor(accentColor)
                }
                itemView.setOnClickListener {
                    val app = list[absoluteAdapterPosition]
                    try {
                        isAppOpened = true
                        runApp(app.appPackage!!)
                    } catch (e: Exception) {
                        Toast.makeText(contextFragment!!, getString(R.string.app_opening_error), Toast.LENGTH_SHORT).show()
                        recyclerView!!.stopScroll()
                        list.remove(app)
                        notifyItemRemoved(absoluteAdapterPosition)
                    }
                }
                itemView.setOnLongClickListener {
                    val app = list[absoluteAdapterPosition]
                    showPopupWindow(itemView, app.appPackage!!, app.appLabel!!)
                    true
                }
            }
        }
        inner class LetterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var textView: MaterialTextView = itemView.findViewById(R.id.abc_label)
        }
    }
    inner class AlphabetAdapter(private var alphabetList: MutableList<AlphabetLetter>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val activeLetter = 10
        private val activeDrawable = launcherAccentColor(activity!!.theme).toDrawable()
        private val disabledLetter = 10
        private val disabledDrawable = ContextCompat.getColor(contextFragment!!, R.color.darkGray).toDrawable()
        private val size = contextFragment!!.resources.getDimensionPixelSize(R.dimen.alphabetHolderSize)
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
                    if(scroll > appAdapter.itemCount) {
                        recyclerView!!.smoothScrollToPosition(appAdapter.itemCount)
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
}
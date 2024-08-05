package ru.dimon6018.metrolauncher.content

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import ir.alirezabdn.wp7progress.WP7ProgressBar
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.isAppOpened
import ru.dimon6018.metrolauncher.Application.Companion.isStartMenuOpened
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.MainViewModel
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.app.App
import ru.dimon6018.metrolauncher.content.data.tile.Tile
import ru.dimon6018.metrolauncher.content.data.tile.TileDao
import ru.dimon6018.metrolauncher.content.data.tile.TileData
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import ru.dimon6018.metrolauncher.helpers.MetroRecyclerView
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.generateRandomTileSize
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentColor
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setUpApps
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sortApps
import java.util.Locale
import kotlin.random.Random

class NewAllApps: Fragment() {

    private lateinit var recyclerView: MetroRecyclerView
    private lateinit var recyclerViewLM: LinearLayoutManager
    private lateinit var recyclerViewAlphabet: RecyclerView
    private lateinit var alphabetLayout: LinearLayout
    private lateinit var search: TextInputLayout
    private lateinit var searchBtn: MaterialCardView
    private lateinit var searchBtnBack: MaterialCardView
    private lateinit var settingsBtn: MaterialCardView

    private lateinit var progressBar: WP7ProgressBar

    private var appAdapter: AppAdapter? = null
    private var adapterAlphabet: AlphabetAdapter? = null

    private var appList: MutableList<App>? = null

    private var isSearching = false
    private var isAlphabetVisible = false

    var scrollPoints: MutableList<Int> = ArrayList()

    private var packageBroadcastReceiver: BroadcastReceiver? = null

    private var isListLoaded = false
    private var isBroadcasterRegistered = false

    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.all_apps_screen, container, false)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val frame: FrameLayout = view.findViewById(R.id.frame)
        if(PREFS!!.isAllAppsBackgroundEnabled) {
            frame.background = ContextCompat.getColor(requireContext(), R.color.transparent).toDrawable()
        } else {
            frame.background = if(PREFS!!.isLightThemeUsed) ContextCompat.getColor(requireContext(), android.R.color.background_light).toDrawable() else ContextCompat.getColor(requireContext(), android.R.color.background_dark).toDrawable()
        }
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.showProgressBar()
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
        settingsBtn = view.findViewById(R.id.settingsBtn)
        val loadingText: MaterialTextView = view.findViewById(R.id.loadingText)
        settingsBtn.setOnClickListener {
            if(isListLoaded) {
                activity?.apply { startActivity(Intent(this, SettingsActivity::class.java)) }
            }
        }
        if(!PREFS!!.isSettingsBtnEnabled) {
            settingsBtn.visibility = View.GONE
        } else {
            settingsBtn.visibility = View.VISIBLE
        }
        searchBtn.setOnClickListener { searchFunction() }
        setRecyclerPadding(resources.getDimensionPixelSize(R.dimen.recyclerViewPadding))
        lifecycleScope.launch(defaultDispatcher) {
            val dbCall = TileData.getTileData(requireContext()).getTileDao()
            appList = getHeaderListLatter(mainViewModel.getAppList())
            appAdapter = AppAdapter(appList!!, dbCall)
            recyclerViewLM = LinearLayoutManager(requireContext())
            setAlphabetRecyclerView()
            withContext(mainDispatcher) {
                searchBtnBack.setOnClickListener {
                    if(isListLoaded) {
                        disableSearch()
                    }
                }
                recyclerView.apply {
                    layoutManager = recyclerViewLM
                    adapter = appAdapter
                    OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
                    visibility = View.VISIBLE
                }
                loadingText.visibility = View.GONE
                progressBar.hideProgressBar()
                progressBar.visibility = View.GONE
                isListLoaded = true
            }
            cancel("done")
        }
        if (PREFS!!.prefs.getBoolean("tip2Enabled", true)) {
            WPDialog(requireActivity()).setTopDialog(true)
                .setTitle(getString(R.string.tip))
                .setMessage(getString(R.string.tip2))
                .setPositiveButton(getString(android.R.string.ok), null)
                .show()
            PREFS!!.prefs.edit().putBoolean("tip2Enabled", false).apply()
        }
        registerBroadcast()
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag", "InlinedApi")
    private fun registerBroadcast() {
        Log.d("AllApps", "register")
        if(!isBroadcasterRegistered) {
            isBroadcasterRegistered = true
            packageBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    Log.d("AllApps", "on receive")
                    val packageName = intent.getStringExtra("package")
                    // End early if it has anything to do with us.
                    if (packageName.isNullOrEmpty()) return
                    packageName.apply {
                        broadcastListUpdater(context)
                    }
                }
            }
            // We want this fragment to receive the package change broadcast,
            // since otherwise it won't be notified when there are changes to that.
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
        }
    }
    private fun broadcastListUpdater(context: Context) {
        Log.d("AllApps", "update list")
        appList = getHeaderListLatter(setUpApps(context.packageManager, context))
        appAdapter?.setData(appList!!, true)
    }
    private fun unregisterBroadcast() {
        Log.d("AllApps", "unreg broadcaster")
        isBroadcasterRegistered = false
        packageBroadcastReceiver?.apply {
            requireActivity().unregisterReceiver(packageBroadcastReceiver)
            packageBroadcastReceiver = null
        } ?: run {
            Log.d("AllApps", "unregisterBroadcast() was called to a null receiver.")
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }
    private suspend fun setAlphabetRecyclerView() {
        adapterAlphabet = AlphabetAdapter(getAlphabetList(), requireContext())
        val lm = GridLayoutManager(requireContext(), 4)
        withContext(mainDispatcher) {
            alphabetLayout.setOnClickListener {
                hideAlphabet()
            }
            recyclerViewAlphabet.apply {
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
        adapterAlphabet!!.setNewData(getAlphabetList())
        if(PREFS!!.isAlphabetAnimEnabled) {
            ObjectAnimator.ofFloat(recyclerView, "alpha", 1f, 0.7f).setDuration(300).start()
        }
        isAlphabetVisible = true
        animateAlphabet(true, 0)
        alphabetLayout.visibility = View.VISIBLE
        if(PREFS!!.isAlphabetAnimEnabled) {
            CoroutineScope(mainDispatcher).launch {
                animateAlphabet(false, 250)
            }
        }
    }
    private fun animateAlphabet(closing: Boolean, duration: Long) {
        for (i in 0..<recyclerViewAlphabet.childCount) {
            val view = recyclerViewAlphabet.getChildAt(i)
            if (view != null) {
                if (closing) {
                    ObjectAnimator.ofFloat(view, "rotationX", 0f, 90f).setDuration(duration).start()
                } else {
                    ObjectAnimator.ofFloat(view, "rotationX", 90f, 0f).setDuration(duration).start()
                }
            }
        }
    }
    private fun hideAlphabet() {
        if(PREFS!!.isAlphabetAnimEnabled) {
            ObjectAnimator.ofFloat(recyclerView, "alpha", 0.7f, 1f).setDuration(300).start()
        }
        isAlphabetVisible = false
        if(PREFS!!.isAlphabetAnimEnabled) {
            CoroutineScope(mainDispatcher).launch {
                animateAlphabet(true, 250)
                delay(250)
                alphabetLayout.visibility = View.INVISIBLE
                recyclerViewAlphabet.scrollToPosition(0)
                cancel()
            }
        } else {
            alphabetLayout.visibility = View.INVISIBLE
            recyclerViewAlphabet.scrollToPosition(0)
        }
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
        if (Utils.getSupportedRuLang()) {
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
        if(appAdapter?.isWindowVisible == true) {
            appAdapter?.popupWindow?.dismiss()
            appAdapter?.popupWindow = null
        }
        super.onPause()
    }

    override fun onResume() {
        if(isAppOpened && !isStartMenuOpened) {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
        registerBroadcast()
        super.onResume()
        if(recyclerView.alpha != 1f) {
            if (PREFS!!.isAAllAppsAnimEnabled) {
                recyclerView.apply {
                    val anim = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
                    anim.duration = 100
                    anim.start()
                    anim.doOnEnd {
                        recyclerView.alpha = 1f
                    }
                }
            } else {
                recyclerView.alpha = 1f
            }
        }
    }
    private fun disableSearch() {
        if(!isListLoaded) {
            return
        }
        isSearching = false
        searchBtn.visibility = View.VISIBLE
        search.visibility = View.GONE
        searchBtnBack.visibility = View.GONE
        if(!PREFS!!.isSettingsBtnEnabled) {
            settingsBtn.visibility = View.GONE
        } else {
            settingsBtn.visibility = View.VISIBLE
        }
        setRecyclerPadding(resources.getDimensionPixelSize(R.dimen.recyclerViewPadding))
        progressBar.showProgressBar()
        recyclerView.alpha = 0.5f
        lifecycleScope.launch(defaultDispatcher) {
            if(context != null) {
                appList = getHeaderListLatter(mainViewModel.getAppList())
            }
            withContext(mainDispatcher) {
                appAdapter?.setData(appList!!, true)
                recyclerView.alpha = 1f
                progressBar.hideProgressBar()
                progressBar.visibility = View.GONE
            }
        }
    }
    private fun setRecyclerPadding(pad: Int) {
        recyclerView.setPadding(pad, 0, 0 ,0)
    }
    private fun searchFunction() {
        if(!isListLoaded) {
            return
        }
        isSearching = true
        searchBtn.visibility = View.GONE
        settingsBtn.visibility = View.GONE
        search.visibility = View.VISIBLE
        search.isFocusable = true
        searchBtnBack.visibility = View.VISIBLE
        lifecycleScope.launch(defaultDispatcher) {
            removeHeaders()
            withContext(mainDispatcher) {
                setRecyclerPadding(0)
            }
            (search.editText as? AutoCompleteTextView)?.addTextChangedListener(object : TextWatcher {
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
        if (appList != null) {
            for (item in appList!!) {
                if (item.appLabel!!.lowercase(locale).contains(text.lowercase(locale))) {
                    filteredlist.add(item)
                }
            }
            if (filteredlist.isNotEmpty()) {
                appAdapter?.setData(filteredlist, true)
            }
        } else {
            context?.apply { Toast.makeText(this, this.getString(R.string.search_error), Toast.LENGTH_SHORT).show() }
        }
    }
    private fun removeHeaders() {
        if(appList == null || !isListLoaded) {
            return
        }
        progressBar.showProgressBar()
        lifecycleScope.launch(defaultDispatcher) {
            var temp = appList!!.size
            while (temp != 0) {
                temp -= 1
                val item = appList!![temp]
                if (item.type == 1) {
                    appList!!.remove(item)
                }
            }
            withContext(mainDispatcher) {
                appAdapter?.setData(appList!!, true)
                progressBar.hideProgressBar()
                progressBar.visibility = View.GONE
            }
        }
    }
    private fun getHeaderListLatter(newApps: ArrayList<App>): MutableList<App> {
        sortApps(newApps)
        var lastHeader: String? = ""
        val list: MutableList<App> = ArrayList()
        newApps.forEach {
            val header = it.appLabel!![0].lowercase(Locale.getDefault())
            if (!TextUtils.equals(lastHeader, header)) {
                lastHeader = header
                val head = App()
                head.appLabel = header
                head.type = 1
                list.add(head)
            }
            list.add(it)
        }
        return list
    }
    open inner class AppAdapter(var list: MutableList<App>, private val dbCall: TileDao): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val letter: Int = 0
        private val appHolder: Int = 1

        var popupWindow: PopupWindow? = null
        var isWindowVisible = false

        @SuppressLint("NotifyDataSetChanged")
        fun setData(new: MutableList<App>, refresh: Boolean) {
            if (refresh) {
                val diffCallback = AppDiffCallback(list, new)
                val diffResult = DiffUtil.calculateDiff(diffCallback)
                list = new
                diffResult.dispatchUpdatesTo(this)
            } else {
                list = new
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
                    bindLetterHolder((holder as LetterHolder), app.appLabel!!)
                }
                appHolder -> {
                    bindAppHolder((holder as AppHolder), app)
                }
            }
        }
        private fun bindLetterHolder(holder: LetterHolder, label: String) {
            holder.textView.text = label
        }
        private fun bindAppHolder(holder: AppHolder, app: App) {
            holder.icon.setImageDrawable(mainViewModel.getIconFromCache(app.appPackage!!))
            holder.label.text = app.appLabel
        }
        private fun showPopupWindow(view: View, app: App) {
            recyclerView.isScrollEnabled = false
            (requireActivity() as Main).configureViewPagerScroll(false)
            val inflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.all_apps_window, recyclerView, false)
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            popupWindow = PopupWindow(popupView, width, height, true)
            popupWindow?.isFocusable = true
            popupWindow?.animationStyle = R.style.enterStyle
            popupView.pivotY = 1f
            val anim = ObjectAnimator.ofFloat(popupView, "scaleY", 0f, 0.01f)
            val anim2 = ObjectAnimator.ofFloat(popupView, "scaleX", 0f, 1f)
            val anim3 =  ObjectAnimator.ofFloat(popupView, "scaleY", 0.01f, 1f)
            anim.setDuration(1)
            anim.doOnEnd {
                anim2.setDuration(200)
                anim2.doOnEnd {
                    anim3.setDuration(400)
                    anim3.start()
                }
                anim2.start()
            }
            anim.start()
            fadeList(app, false)
            PopupWindowCompat.showAsDropDown(popupWindow!!, view, 0, 0, Gravity.NO_GRAVITY)
            isWindowVisible = true
            val pin = popupView.findViewById<MaterialCardView>(R.id.pinApp)
            val uninstall = popupView.findViewById<MaterialCardView>(R.id.uninstallApp)
            val info = popupView.findViewById<MaterialCardView>(R.id.infoApp)
            var isAppAlreadyPinned = false
            lifecycleScope.launch(defaultDispatcher) {
                val dbList = dbCall.getTilesList()
                dbList.forEach {
                    if (it.appPackage == app.appPackage) {
                        isAppAlreadyPinned = true
                        return@forEach
                    }
                }
                withContext(mainDispatcher) {
                    if(isAppAlreadyPinned) {
                        pin.isEnabled = false
                        pin.alpha = 0.5f
                    } else {
                        pin.isEnabled = true
                        pin.alpha = 1f
                        pin.setOnClickListener {
                            insertNewApp(app)
                            popupWindow?.dismiss()
                            activity?.onBackPressedDispatcher?.onBackPressed()
                        }
                    }
                }
            }
            uninstall.setOnClickListener {
                popupWindow?.dismiss()
                startActivity(Intent(Intent.ACTION_DELETE).setData(Uri.parse("package:${app.appPackage}")))
            }
            info.setOnClickListener {
                isAppOpened = true
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:${app.appPackage}")))
            }
            popupWindow?.setOnDismissListener {
                (requireActivity() as Main).configureViewPagerScroll(true)
                recyclerView.isScrollEnabled = true
                fadeList(app, true)
                isWindowVisible = false
                popupWindow = null
            }
        }
        private fun fadeList(app: App, restoreAll: Boolean) {
            val first = recyclerViewLM.findFirstVisibleItemPosition()
            val last = recyclerViewLM.findLastVisibleItemPosition()
            if (restoreAll) {
                for (i in first..last) {
                    val itemView = recyclerView.findViewHolderForAdapterPosition(i)?.itemView
                    if (itemView != null) {
                        ObjectAnimator.ofFloat(itemView, "alpha", 0.5f, 1f).setDuration(500).start()
                    }
                }
            } else {
                for (i in first..last) {
                    if (list[i] == app) {
                        continue
                    }
                    val itemView = recyclerView.findViewHolderForAdapterPosition(i)?.itemView
                    if (itemView != null) {
                        ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0.5f).setDuration(500).start()
                    }
                }
            }
        }
        private fun startDismissAnim(item: App) {
            if (appAdapter == null || !PREFS!!.isAAllAppsAnimEnabled) {
                startAppDelay(item)
                return
            }
            val animatorSetDismiss = AnimatorSet()
            for(i in 0..<recyclerView.childCount) {
                val itemView = recyclerView.getChildAt(i) ?: continue
                animatorSetDismiss.playTogether(
                    ObjectAnimator.ofFloat(itemView, "rotationY", 0f, -90f),
                    ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0f)
                )
                animatorSetDismiss.duration = (200 + (i * 2)).toLong()
                animatorSetDismiss.start()
            }
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(recyclerView, "rotationY", 0f, -90f),
                ObjectAnimator.ofFloat(recyclerView, "translationX", 0f, -600f),
                ObjectAnimator.ofFloat(recyclerView, "alpha", 1f, 0f)
            )
            animatorSet.duration = 325
            animatorSet.start()
            animatorSet.doOnEnd {
                recyclerView.alpha = 0f
                ObjectAnimator.ofFloat(recyclerView, "rotationY", 0f, 0f).start()
                ObjectAnimator.ofFloat(recyclerView, "translationX", 0f, 0f).start()
            }
            startAppDelay(item)
        }
        private fun hideTilesStartScreen() {

        }
        private fun startAppDelay(item: App) {
            CoroutineScope(mainDispatcher).launch {
                hideTilesStartScreen()
                delay(300)
                if(context != null) {
                    runApp(item.appPackage!!, requireContext().packageManager)
                    delay(100)
                    val animatorSetItems = AnimatorSet()
                    animatorSetItems.duration = 100
                    for(i in 0..<recyclerView.childCount) {
                        val itemView = recyclerView.getChildAt(i) ?: continue
                        animatorSetItems.playTogether(
                            ObjectAnimator.ofFloat(itemView, "rotationY", -90f, 0f),
                            ObjectAnimator.ofFloat(itemView, "rotation", 45f, 0f),
                            ObjectAnimator.ofFloat(itemView, "translationX", -500f, 0f),
                            ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f)
                        )
                        animatorSetItems.start()
                    }
                }
                cancel()
            }
        }
        private fun runApp(app: String, pm: PackageManager) {
            isAppOpened = true
            when (app) {
                "ru.dimon6018.metrolauncher" -> {
                    activity?.apply { startActivity(Intent(this, SettingsActivity::class.java)) }
                }
                else -> {
                    startActivity(Intent(pm.getLaunchIntentForPackage(app)))
                }
            }
        }
        private fun insertNewApp(app: App) {
            lifecycleScope.launch(defaultDispatcher) {
                val dataList = dbCall.getTilesList()
                dataList.forEach {
                    if(it.appPackage == app.appPackage) {
                        //db already has this app. we must stop this
                        return@launch
                    }
                }
                var pos = 0
                for (i in 0..<dataList.size) {
                    if (dataList[i].tileType == -1) {
                        pos = i
                        break
                    }
                }
                val id = Random.nextLong(1000, 2000000)
                val item = Tile(pos, id, -1, 0,
                    isSelected = false,
                    tileSize = generateRandomTileSize(true),
                    appLabel = app.appLabel!!,
                    appPackage = app.appPackage!!
                )
                dbCall.addTile(item)
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
        inner class AppDiffCallback(
            private val oldList: List<App>,
            private val newList: List<App>
        ) : DiffUtil.Callback() {

            override fun getOldListSize() = oldList.size

            override fun getNewListSize() = newList.size

            override fun areItemsTheSame(old: Int, new: Int): Boolean {
                return oldList[old].appPackage == newList[new].appPackage
            }

            override fun areContentsTheSame(old: Int, new: Int): Boolean {
                return oldList[old] == newList[new]
            }
        }
        inner class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.app_icon)
            val label: MaterialTextView = itemView.findViewById(R.id.app_label)
            init {
                Utils.setViewInteractAnimation(itemView)
                itemView.setOnClickListener {
                    visualFeedback(itemView)
                    try {
                        if(PREFS!!.isAAllAppsAnimEnabled) {
                            startDismissAnim(list[absoluteAdapterPosition])
                        } else {
                            if(context != null) {
                                runApp(list[absoluteAdapterPosition].appPackage!!, requireContext().packageManager)
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), getString(R.string.app_opening_error), Toast.LENGTH_SHORT).show()
                        recyclerView.stopScroll()
                        list.remove(list[absoluteAdapterPosition])
                        notifyItemRemoved(absoluteAdapterPosition)
                    }
                }
                itemView.setOnLongClickListener {
                    showPopupWindow(itemView, list[absoluteAdapterPosition])
                    true
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
        }
        inner class LetterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var textView: MaterialTextView = itemView.findViewById(R.id.abc_label)
            init {
                itemView.setOnClickListener {
                    showAlphabet()
                }
            }
        }
    }
    inner class AlphabetAdapter(private var alphabetList: MutableList<AlphabetLetter>, context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val activeLetter = 10
        private val disabledLetter = 11

        private val activeDrawable: ColorDrawable by lazy {
            if(activity != null) {
                launcherAccentColor(activity!!.theme).toDrawable()
            } else {
                context.getColor(android.R.color.white).toDrawable()
            }
        }
        private val disabledDrawable: ColorDrawable by lazy {
            ContextCompat.getColor(context, R.color.darkGray).toDrawable()
        }

        private val size = context.resources.getDimensionPixelSize(R.dimen.alphabetHolderSize)
        private val params = ViewGroup.LayoutParams(size, size)
        @SuppressLint("NotifyDataSetChanged")
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
            setHolderView(holder, item.isActive)
            setOnClick(holder, item)
        }
        private fun setHolderView(holder: AlphabetLetterHolder, isActive: Boolean) {
            holder.itemView.layoutParams = params
            if(isActive) {
                holder.backgroundView.background = activeDrawable
            } else {
                holder.backgroundView.background = disabledDrawable
            }
        }
        private fun setOnClick(holder: AlphabetLetterHolder, item: AlphabetLetter) {
            if(item.isActive) {
                holder.itemView.setOnClickListener {
                    hideAlphabet()
                    val scroll = scrollPoints[item.posInList]
                    if(scroll > appAdapter?.itemCount!!) {
                        recyclerView.smoothScrollToPosition(appAdapter?.itemCount!!)
                    } else {
                        recyclerView.smoothScrollToPosition(scroll)
                    }
                }
            }
        }
        override fun getItemViewType(position: Int): Int {
            return if(alphabetList[position].isActive) activeLetter else disabledLetter
        }
    }
    inner class AlphabetLetter {
        var letter: String = ""
        var isActive: Boolean = false
        var posInList: Int = 0
    }
    inner class AlphabetLetterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: MaterialTextView = itemView.findViewById(R.id.alphabetLetter)
        var backgroundView: View = itemView.findViewById(R.id.alphabetBackground)
    }
}
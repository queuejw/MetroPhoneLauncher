package ru.queuejw.mpl.content

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.IOverScrollDecor
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.Application.Companion.customFont
import ru.queuejw.mpl.Application.Companion.customLightFont
import ru.queuejw.mpl.Application.Companion.isAppOpened
import ru.queuejw.mpl.Main
import ru.queuejw.mpl.MainViewModel
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.data.app.App
import ru.queuejw.mpl.content.data.tile.Tile
import ru.queuejw.mpl.content.settings.SettingsActivity
import ru.queuejw.mpl.databinding.AppBinding
import ru.queuejw.mpl.databinding.LauncherAllAppsScreenBinding
import ru.queuejw.mpl.helpers.receivers.PackageChangesReceiver
import ru.queuejw.mpl.helpers.ui.WPDialog
import ru.queuejw.mpl.helpers.utils.Utils
import kotlin.random.Random

class AllApps : Fragment() {

    private lateinit var recyclerViewLM: LinearLayoutManager

    private var appAdapter: AppAdapter? = null

    private var packageBroadcastReceiver: BroadcastReceiver? = null

    private var isSearching = false
    private var isBroadcasterRegistered = false

    private lateinit var mainViewModel: MainViewModel

    private var _binding: LauncherAllAppsScreenBinding? = null
    private val binding get() = _binding!!

    private val bottomDecor by lazy {
        Utils.BottomOffsetDecoration(
            requireContext().resources.getDimensionPixelSize(
                R.dimen.recyclerViewPadding
            )
        )
    }
    private val scrollDecor: IOverScrollDecor by lazy {
        OverScrollDecoratorHelper.setUpOverScroll(
            binding.appList,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LauncherAllAppsScreenBinding.inflate(inflater, container, false)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setUi()
        return binding.root
    }

    private fun setUi() {
        if (PREFS.isSettingsBtnEnabled) {
            binding.settingsBtn.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    activity?.let { it.startActivity(Intent(it, SettingsActivity::class.java)) }
                }
            }
        }
        binding.searchBtn.setOnClickListener {
            searchFunction()
        }
        binding.searchBackBtn.setOnClickListener {
            disableSearch()
        }
        lifecycleScope.launch(Dispatchers.Default) {
            prepareRecyclerView()
            prepareData()
        }
        Utils.applyWindowInsets(binding.root)
    }

    private suspend fun prepareRecyclerView() {
        recyclerViewLM = LinearLayoutManager(requireActivity())
        withContext(Dispatchers.Main) {
            binding.appList.apply {
                layoutManager = recyclerViewLM
                addItemDecoration(bottomDecor)
                scrollDecor.attach()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (PREFS.customFontInstalled) customFont?.let {
            binding.searchTextview.typeface = it
            binding.noResults.typeface = it
        }
        if (PREFS.prefs.getBoolean("tip2Enabled", true)) {
            tipDialog()
            PREFS.prefs.edit().putBoolean("tip2Enabled", false).apply()
        }
    }

    private suspend fun prepareData() {
        appAdapter = AppAdapter(mainViewModel.getAppList())
        withContext(Dispatchers.Main) {
            configureRecyclerView()
        }
    }

    private fun tipDialog() {
        WPDialog(requireContext()).setTopDialog(true)
            .setTitle(getString(R.string.tip))
            .setMessage(getString(R.string.tip2))
            .setPositiveButton(getString(android.R.string.ok), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configureRecyclerView() {
        binding.appList.apply {
            adapter = appAdapter
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "InlinedApi")
    private fun registerBroadcast() {
        if (!isBroadcasterRegistered) {
            isBroadcasterRegistered = true
            packageBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val packageName = intent.getStringExtra("package")
                    // End early if it has anything to do with us.
                    if (packageName.isNullOrEmpty()) return
                    val action = intent.getIntExtra("action", 42)
                    packageName.apply {
                        when (action) {
                            PackageChangesReceiver.PACKAGE_INSTALLED -> {
                                val bool = PREFS.iconPackPackage != "null"
                                (requireActivity() as Main).generateIcon(packageName, bool)
                                broadcastListUpdater(context)
                            }

                            PackageChangesReceiver.PACKAGE_REMOVED -> {
                                //I don't think that's gonna work.
                                mainViewModel.removeIconFromCache(packageName)
                                broadcastListUpdater(context)
                            }

                            else -> {
                                broadcastListUpdater(context)
                            }
                        }
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
        mainViewModel.setAppList(Utils.setUpApps(context))
        appAdapter?.setData(mainViewModel.getAppList())
    }

    private fun unregisterBroadcast() {
        isBroadcasterRegistered = false
        packageBroadcastReceiver?.apply {
            requireActivity().unregisterReceiver(packageBroadcastReceiver)
            packageBroadcastReceiver = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }

    override fun onPause() {
        if (isSearching && !PREFS.showKeyboardWhenOpeningAllApps) {
            disableSearch()
        }
        if (appAdapter?.isWindowVisible == true) {
            appAdapter?.popupWindow?.dismiss()
            appAdapter?.popupWindow = null
        }
        super.onPause()
    }

    override fun onResume() {
        registerBroadcast()
        super.onResume()
        if (binding.appList.visibility != View.VISIBLE) binding.appList.visibility = View.VISIBLE
        if (PREFS.showKeyboardWhenOpeningAllApps) searchFunction()
        if (binding.appList.alpha != 1f) {
            if (PREFS.isAAllAppsAnimEnabled) {
                binding.appList.apply {
                    val anim = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
                    anim.duration = 100
                    anim.start()
                    anim.doOnEnd {
                        this.alpha = 1f
                    }
                }
            } else {
                binding.appList.alpha = 1f
            }
        }
    }

    private fun disableSearch() {
        if (!isSearching) return
        isSearching = false
        binding.noResults.visibility = View.GONE
        scrollDecor.attach()
        binding.apply {
            hideKeyboard(search.editText as? AutoCompleteTextView)
            (search.editText as? AutoCompleteTextView)?.apply {
                text.clear()
                clearFocus()
            }
            searchLayout.animate().translationY(-300f).setDuration(200).withEndAction {
                searchLayout.visibility = View.GONE
                searchBtn.apply {
                    visibility = View.VISIBLE
                    animate().alpha(1f).setDuration(200).start()
                }
            }.start()
            settingsBtn.visibility = if (!PREFS.isSettingsBtnEnabled) View.GONE else View.VISIBLE
            appList.apply {
                alpha = 0.5f
                animate().translationX(0f).setDuration(200).start()
                isVerticalScrollBarEnabled = true
            }
        }
        appAdapter?.setData(mainViewModel.getAppList())
        binding.appList.alpha = 1f
        binding.appList.smoothScrollToPosition(0)
    }

    private fun searchFunction() {
        if (isSearching) return
        appAdapter ?: return
        isSearching = true
        scrollDecor.detach()
        binding.apply {
            searchLayout.apply {
                visibility = View.VISIBLE
                animate().translationY(0f).setDuration(200).start()
            }
            searchBtn.animate().alpha(0f).setDuration(100).withEndAction {
                searchBtn.visibility = View.GONE
            }.start()
            settingsBtn.visibility = View.GONE
            appList.apply {
                animate().translationX(
                    -requireContext().resources.getDimensionPixelSize(R.dimen.recyclerViewSearchPadding)
                        .toFloat()
                ).setDuration(200).start()
                isVerticalScrollBarEnabled = false
            }
        }
        if (PREFS.showKeyboardWhenSearching) {
            showKeyboard(binding.search.editText as? AutoCompleteTextView)
        }
        if (PREFS.allAppsKeyboardActionEnabled) {
            (binding.search.editText as? AutoCompleteTextView)?.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO && appAdapter!!.list.isNotEmpty()) {
                    runApp(
                        appAdapter!!.list.first().appPackage!!,
                        requireActivity().packageManager
                    )
                    (binding.search.editText as? AutoCompleteTextView)?.text!!.clear()
                    disableSearch()
                    true
                } else {
                    false
                }
            }
        }
        (binding.search.editText as? AutoCompleteTextView)?.addTextChangedListener(object :
            TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterText(s.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun showKeyboard(view: View?) {
        if (view != null) {
            if (view.requestFocus()) {
                val input =
                    ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                input?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun hideKeyboard(view: View?) {
        if (view != null) {
            if (view.requestFocus()) {
                val input =
                    ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
                input?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    private fun filterText(searchText: String) {
        val filteredList: MutableList<App> = ArrayList()
        val locale = Utils.getDefaultLocale()
        mainViewModel.getAppList().forEach {
            if (it.appLabel!!.lowercase(locale).contains(searchText.lowercase(locale))) {
                filteredList.add(it)
            }
        }
        appAdapter?.setData(filteredList)
        binding.noResults.apply {
            if (filteredList.isEmpty()) {
                visibility = View.VISIBLE
                val string = context.getString(R.string.no_results_for) + " " + searchText
                val spannable: Spannable = SpannableString(string)
                spannable.setSpan(
                    ForegroundColorSpan(Utils.launcherAccentColor(requireActivity().theme)),
                    string.indexOf(searchText),
                    string.indexOf(searchText) + searchText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setText(spannable, TextView.BufferType.SPANNABLE)
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun runApp(app: String, pm: PackageManager) {
        isAppOpened = true
        when (app) {
            context?.packageName -> activity?.apply {
                startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )
            }

            else -> startActivity(Intent(pm.getLaunchIntentForPackage(app)))
        }
    }

    open inner class AppAdapter(var list: MutableList<App>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val appHolder = 0

        var popupWindow: PopupWindow? = null
        var isWindowVisible = false


        inner class AppHolder(val binding: AppBinding) : RecyclerView.ViewHolder(binding.root) {
            init {
                Utils.setViewInteractAnimation(itemView)
                if (PREFS.customFontInstalled) customFont?.let { binding.appLabel.typeface = it }

                itemView.setOnClickListener {
                    click()
                }
                itemView.setOnLongClickListener {
                    showPopupWindow(itemView, list[absoluteAdapterPosition])
                    true
                }
            }
            private fun click() {
                if (PREFS.isAAllAppsAnimEnabled) {
                    startDismissAnim(list[absoluteAdapterPosition])
                } else {
                    if (context != null) {
                        runApp(
                            list[absoluteAdapterPosition].appPackage!!,
                            requireContext().packageManager
                        )
                    }
                }
            }
        }

        fun setData(new: MutableList<App>) {
            val diffCallback = AppDiffCallback(list, new)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            list = new
            diffResult.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return AppHolder(AppBinding.inflate(inflater, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            bindAppHolder(holder as AppHolder, list[position])
        }

        private fun bindAppHolder(holder: AppHolder, app: App) {
            holder.binding.appIcon.load(mainViewModel.getIconFromCache(app.appPackage!!))
            holder.binding.appLabel.text = app.appLabel
        }

        fun isPopupInTop(anchorView: View, popupHeight: Int): Boolean {
            val location = IntArray(2)
            anchorView.getLocationOnScreen(location)
            val anchorY = location[1]
            val displayMetrics = anchorView.context.resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val popupY = anchorY - popupHeight
            return popupY < screenHeight / 2
        }

        fun getPopupHeight(popupWindow: PopupWindow): Int {
            val contentView = popupWindow.contentView
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            return contentView.measuredHeight
        }

        private fun showPopupWindow(view: View, app: App) {
            binding.appList.isScrollEnabled = false
            (requireActivity() as Main).configureViewPagerScroll(false)
            val inflater =
                view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.all_apps_window, binding.appList, false)
            popupWindow = PopupWindow(
                popupView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
            )
            popupWindow!!.isFocusable = true
            val popupHeight = getPopupHeight(popupWindow!!)
            val top = isPopupInTop(view, popupHeight)
            popupView.pivotY = if (top) 0f else popupHeight.toFloat()
            val pinLabel = popupView.findViewById<MaterialTextView>(R.id.pin_app_label)
            val infoLabel = popupView.findViewById<MaterialTextView>(R.id.app_info_label)
            val uninstallLabel = popupView.findViewById<MaterialTextView>(R.id.uninstall_label)
            (if (PREFS.customLightFontPath != null) customLightFont else customFont)?.let {
                pinLabel.typeface = it
                infoLabel.typeface = it
                uninstallLabel.typeface = it
            }
            val anim = ObjectAnimator.ofFloat(popupView, "scaleY", 0f, 0.01f).setDuration(1)
            val anim2 = ObjectAnimator.ofFloat(popupView, "scaleX", 0f, 1f).setDuration(200)
            val anim3 = ObjectAnimator.ofFloat(popupView, "scaleY", 0.01f, 1f).setDuration(400)
            anim.doOnEnd {
                anim2.doOnEnd {
                    anim3.start()
                }
                anim2.start()
            }
            anim.start()
            fadeList(app, false)
            popupWindow!!.showAsDropDown(
                view,
                0,
                if (top) 0 else (-popupHeight + -view.height),
                Gravity.CENTER
            )
            isWindowVisible = true

            val pin = popupView.findViewById<MaterialCardView>(R.id.pin_app)
            val info = popupView.findViewById<MaterialCardView>(R.id.infoApp)
            val uninstall = popupView.findViewById<MaterialCardView>(R.id.uninstallApp)

            var isAppAlreadyPinned = false
            lifecycleScope.launch(Dispatchers.Default) {
                val dbList = mainViewModel.getViewModelTileDao().getTilesList()
                dbList.forEach {
                    if (it.tilePackage == app.appPackage) {
                        isAppAlreadyPinned = true
                        return@forEach
                    }
                }
                withContext(Dispatchers.Main) {
                    if (isAppAlreadyPinned) {
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
                startActivity(
                    Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:${app.appPackage}"))
                )
            }
            popupWindow?.setOnDismissListener {
                (requireActivity() as Main).configureViewPagerScroll(true)
                binding.appList.isScrollEnabled = true
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
                    val itemView = binding.appList.findViewHolderForAdapterPosition(i)?.itemView
                    itemView?.animate()?.alpha(1f)?.scaleY(1f)?.scaleX(1f)?.setDuration(500)
                        ?.start()
                }
            } else {
                for (i in first..last) {
                    val itemView = binding.appList.findViewHolderForAdapterPosition(i)?.itemView
                    if (list[i] == app) continue
                    itemView?.animate()?.alpha(0.5f)?.scaleY(0.95f)?.scaleX(0.95f)?.setDuration(500)
                        ?.start()
                }
            }
        }

        private fun startDismissAnim(item: App) {
            if (appAdapter == null || !PREFS.isAAllAppsAnimEnabled) {
                startAppDelay(item)
                return
            }
            val animatorSetDismiss = AnimatorSet()
            for (i in 0 until binding.appList.childCount) {
                val itemView = binding.appList.getChildAt(i) ?: continue
                animatorSetDismiss.playTogether(
                    ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0f),
                    ObjectAnimator.ofFloat(itemView, "rotationY", 0f, -90f)
                )
                animatorSetDismiss.duration = (200 + (i * 2)).toLong()
                animatorSetDismiss.start()
            }
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(binding.appList, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(binding.appList, "rotationY", 0f, -90f),
                ObjectAnimator.ofFloat(binding.appList, "translationX", 0f, -600f)
            )
            animatorSet.duration = 325
            animatorSet.start()
            animatorSet.doOnEnd {
                binding.appList.alpha = 0f
                ObjectAnimator.ofFloat(binding.appList, "rotationY", 0f, 0f).start()
                ObjectAnimator.ofFloat(binding.appList, "translationX", 0f, 0f).start()
            }
            startAppDelay(item)
        }

        private fun startAppDelay(item: App) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                context?.let {
                    runApp(item.appPackage!!, it.packageManager)
                    delay(100)
                    val animatorSetItems = AnimatorSet()
                    animatorSetItems.duration = 100
                    for (i in 0 until binding.appList.childCount) {
                        val itemView = binding.appList.getChildAt(i) ?: continue
                        animatorSetItems.playTogether(
                            ObjectAnimator.ofFloat(itemView, "alpha", 0f, 1f),
                            ObjectAnimator.ofFloat(itemView, "rotationY", -90f, 0f),
                            ObjectAnimator.ofFloat(itemView, "rotation", 45f, 0f),
                            ObjectAnimator.ofFloat(itemView, "translationX", -500f, 0f)
                        )
                        animatorSetItems.start()
                    }
                }
                cancel()
            }
        }

        private fun insertNewApp(app: App) {
            lifecycleScope.launch(Dispatchers.Default) {
                val dataList = mainViewModel.getViewModelTileDao().getTilesList()
                dataList.forEach {
                    if (it.tilePackage == app.appPackage) {
                        //db already has this app. we must stop this
                        return@launch
                    }
                }
                var pos = 0
                for (i in 0 until dataList.size) {
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
                    tileLabel = app.appLabel!!,
                    tilePackage = app.appPackage!!
                )
                mainViewModel.getViewModelTileDao().addTile(item)
            }
        }

        override fun getItemCount(): Int = list.size

        override fun getItemViewType(position: Int): Int {
            return appHolder
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
    }
}
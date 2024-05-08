package ru.dimon6018.metrolauncher.content.oobe.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import ir.alirezabdn.wp7progress.WP7ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.apps.App
import ru.dimon6018.metrolauncher.content.data.apps.AppData
import ru.dimon6018.metrolauncher.content.data.apps.AppEntity
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.helpers.IconPackManager
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.generateRandomTileSize
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.recompressIcon
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setUpApps
import kotlin.random.Random

class AppsFragment: Fragment() {

    private var recyclerView: RecyclerView? = null
    private var loading: WP7ProgressBar? = null
    private var fragmentContext: Context? = null
    private val hashCache = ArrayMap<String, Icon?>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_apps, container, false)
        fragmentContext = requireContext()
        WelcomeActivity.setText(requireActivity(), getString(R.string.configureApps))
        recyclerView = view.findViewById(R.id.oobeRecycler)
        loading = view.findViewById(R.id.oobeAppsLoadingBar)
        loading!!.showProgressBar()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val back: MaterialButton = view.findViewById(R.id.back)
        val next: MaterialButton = view.findViewById(R.id.next)
        val call = AppData.getAppData(fragmentContext!!).getAppDao()
        lifecycleScope.launch(Dispatchers.Default) {
            selectedItems = ArrayList()
            val appList = setUpApps(fragmentContext!!.packageManager, fragmentContext!!)
            val mAdapter = AppAdapter(appList, fragmentContext!!)
            val lm = LinearLayoutManager(fragmentContext)
            var iconManager: IconPackManager? = null
            var isCustomIconsInstalled = false
            if (PREFS!!.iconPackPackage != "null") {
                iconManager = IconPackManager()
                iconManager.setContext(fragmentContext!!)
                isCustomIconsInstalled = true
            }
            val iconSize =
                fragmentContext!!.resources.getDimensionPixelSize(R.dimen.iconAppsListSize)
            val pm = fragmentContext!!.packageManager
            appList.forEach {
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
            withContext(Dispatchers.Main) {
                recyclerView?.apply {
                    layoutManager = lm
                    adapter = mAdapter
                    OverScrollDecoratorHelper.setUpOverScroll(
                        this,
                        OverScrollDecoratorHelper.ORIENTATION_VERTICAL
                    )
                }
            }
            runBlocking {
                withContext(Dispatchers.Main) {
                    loading!!.hideProgressBar()
                    loading!!.visibility = View.GONE
                }
            }
        }
        back.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, AdFragment(), "oobe")
            }
        }
        next.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Default) {
                var pos = 0
                for (i in selectedItems!!) {
                    val id = Random.nextLong(1000, 2000000)
                    val entity = AppEntity(
                        pos, id, -1, 0,
                        isSelected = false,
                        tileSize = generateRandomTileSize(false),
                        appLabel = i.appLabel!!,
                        appPackage = i.appPackage!!
                    )
                    call.insertItem(entity)
                    pos += 1
                }
                runBlocking {
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, AlmostDoneFragment(), "oobe")
                    }
                }
            }
        }
    }

    companion object {
        var selectedItems: MutableList<App>? = null
        var latestItem: Int? = null
    }

    inner class AppAdapter(private var adapterApps: MutableList<App>, private val context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return OOBEAppHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.oobe_app_item, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return adapterApps.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = adapterApps[position]
            holder as OOBEAppHolder
            try {
                holder.icon.setImageIcon(hashCache[item.appPackage])
            } catch (e: PackageManager.NameNotFoundException) {
                holder.icon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_os_android
                    )
                )
                adapterApps.remove(item)
                notifyItemRemoved(position)
            }
            holder.label.text = item.appLabel
            holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                latestItem = position
                if (isChecked) {
                    item.selected = true
                    if (!selectedItems!!.contains(item)) {
                        selectedItems!!.add(item)
                    }
                } else {
                    item.selected = false
                    if (selectedItems!!.contains(item)) {
                        selectedItems!!.remove(item)
                    }
                }
            }
            if (latestItem != null) {
                holder.checkbox.isChecked =
                    latestItem == position || adapterApps[position].selected
            }
        }
    }
    inner class OOBEAppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val label: MaterialTextView = itemView.findViewById(R.id.app_label)
        val checkbox: MaterialCheckBox = itemView.findViewById(R.id.app_checkbox)
    }
}
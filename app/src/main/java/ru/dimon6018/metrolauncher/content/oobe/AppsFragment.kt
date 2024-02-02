package ru.dimon6018.metrolauncher.content.oobe

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.setUpApps
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.AllApps
import ru.dimon6018.metrolauncher.content.data.App
import ru.dimon6018.metrolauncher.content.data.AppDao
import ru.dimon6018.metrolauncher.content.data.AppData
import ru.dimon6018.metrolauncher.content.data.AppEntity
import kotlin.random.Random

class AppsFragment: Fragment() {

    private var recyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_apps, container, false)
        val back: MaterialButton = view.findViewById(R.id.back)
        val next: MaterialButton = view.findViewById(R.id.next)
        WelcomeActivity.setText(requireActivity(), getString(R.string.configureApps))
        val context = requireContext()
        var call: AppDao = AppData.getAppData(context).getAppDao()
        recyclerView = view.findViewById(R.id.oobeRecycler)
        CoroutineScope(Dispatchers.Default).launch {
            selectedItems = ArrayList()
            val appList = setUpApps(requireContext().packageManager)
            call = AppData.getAppData(context).getAppDao()
            val imageLoader = ImageLoader.Builder(context)
                    .interceptorDispatcher(Dispatchers.Default)
                    .memoryCache {
                        MemoryCache.Builder(context)
                                .maxSizePercent(0.1)
                                .build()
                    }
                    .diskCache {
                        DiskCache.Builder()
                                .directory(context.cacheDir.resolve("cache"))
                                .maxSizePercent(0.1)
                                .build()
                    }
                    .build()
            val adapter = AppAdapter(appList, context.packageManager, context, imageLoader)
            requireActivity().runOnUiThread {
                recyclerView!!.layoutManager = LinearLayoutManager(context)
                recyclerView!!.adapter = adapter
                OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
            }
        }
        back.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
            }
        }
        next.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                for (i in 0..<selectedItems!!.size) {
                    val item = selectedItems!![i]
                    val pos = call.getJustAppsWithoutPlaceholders(false).size
                    val id = Random.nextInt(1000, 20000)
                    val entity = AppEntity(pos, id, -1, false, "small", item.appLabel!!, item.appPackage!!)
                    call.insertItem(entity)
                }
                runBlocking {
                    requireActivity().supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, AlmostDoneFragment(), "oobe")
                    }
                }
            }
        }
        return view
    }
    companion object {
        var selectedItems: MutableList<App>? = null
        var latestItem: Int? = null
    }
}
class AppAdapter internal constructor(private var adapterApps: MutableList<App>, private val packageManager: PackageManager, private val context: Context, private val imageLoader: ImageLoader) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    private var iconSize = context.resources.getDimensionPixelSize(R.dimen.iconAppsListSize)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OOBEAppHolder(LayoutInflater.from(parent.context).inflate(R.layout.oobe_app_item, parent, false))
    }

    override fun getItemCount(): Int {
        return adapterApps.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = adapterApps[position]
        holder as OOBEAppHolder
        try {
            val bmp = packageManager.getApplicationIcon(item.appPackage!!).toBitmap(iconSize, iconSize, Application.PREFS!!.iconBitmapConfig())
            val request = ImageRequest.Builder(context)
                    .data(bmp)
                    .crossfade(true)
                    .target(holder.icon)
                    .build()
            imageLoader.enqueue(request)
        } catch (e: PackageManager.NameNotFoundException) {
            val request = ImageRequest.Builder(context)
                    .data(R.drawable.ic_os_android)
                    .crossfade(true)
                    .target(holder.icon)
                    .build()
            imageLoader.enqueue(request)
        }
        holder.label.text = item.appLabel
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            AppsFragment.latestItem = position
            if (isChecked) {
                item.selected = true
                AppsFragment.selectedItems!!.add(item)
            } else {
                item.selected = false
                AppsFragment.selectedItems!!.remove(item)
            }
        }
        if (AppsFragment.latestItem != null) {
            holder.checkbox.isChecked = AppsFragment.latestItem == position || adapterApps[position].selected
        }
    }
}
class OOBEAppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: ImageView
    val label: TextView
    val checkbox: MaterialCheckBox

    init {
        icon = itemView.findViewById(R.id.app_icon)
        label = itemView.findViewById(R.id.app_label)
        checkbox = itemView.findViewById(R.id.app_checkbox)
    }
}
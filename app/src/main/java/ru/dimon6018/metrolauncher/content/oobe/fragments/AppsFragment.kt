package ru.dimon6018.metrolauncher.content.oobe.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.collection.SparseArrayCompat
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.app.App
import ru.dimon6018.metrolauncher.content.data.tile.Tile
import ru.dimon6018.metrolauncher.content.data.tile.TileData
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.helpers.IconPackManager
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.generateRandomTileSize
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setUpApps
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sortApps
import kotlin.random.Random

class AppsFragment: Fragment() {

    private var recyclerView: RecyclerView? = null
    private var loading: WP7ProgressBar? = null
    private val hashCache = SparseArrayCompat<Bitmap?>()
    private var main: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.oobe_fragment_apps, container, false)
        main = view
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
        val selectAll: MaterialButton = view.findViewById(R.id.oobeSelectAll)
        val removeAll: MaterialButton = view.findViewById(R.id.oobeRemoveAll)
        val call = TileData.getTileData(requireContext()).getTileDao()
        lifecycleScope.launch(Dispatchers.Default) {
            selectedItems = ArrayList()
            val appList = sortApps(setUpApps(requireContext().packageManager, requireContext()))
            val mAdapter = AppAdapter(appList, requireContext())
            val lm = LinearLayoutManager(requireContext())
            var iconManager: IconPackManager? = null
            var isCustomIconsInstalled = false
            if (PREFS!!.iconPackPackage != "null") {
                iconManager = IconPackManager(requireContext())
                isCustomIconsInstalled = true
            }
            val iconSize =
                requireContext().resources.getDimensionPixelSize(R.dimen.iconAppsListSize)
            val pm = requireContext().packageManager
            appList.forEach {
                if (it.type != 1) {
                    val bmp = if (!isCustomIconsInstalled)
                        pm.getApplicationIcon(it.appPackage!!).toBitmap(iconSize, iconSize)
                    else
                            iconManager?.getIconPackWithName(PREFS!!.iconPackPackage)
                                ?.getDrawableIconForPackage(it.appPackage!!, null)
                                ?.toBitmap(iconSize, iconSize)
                    hashCache.append(it.id, bmp)
                }
            }
            withContext(Dispatchers.Main) {
                recyclerView?.apply {
                    layoutManager = lm
                    adapter = mAdapter
                }
                back.setOnClickListener {
                    lifecycleScope.launch {
                        enterAnimation(true)
                        delay(200)
                        requireActivity().supportFragmentManager.commit {
                            replace(R.id.fragment_container_view, AdFragment(), "oobe")
                        }
                    }
                }
                next.setOnClickListener {
                    enterAnimation(true)
                    lifecycleScope.launch(Dispatchers.Default) {
                        var pos = 0
                        for (i in selectedItems!!) {
                            val id = Random.nextLong(1000, 2000000)
                            val entity = Tile(
                                pos, id, -1, 0,
                                isSelected = false,
                                tileSize = generateRandomTileSize(false),
                                appLabel = i.appLabel!!,
                                appPackage = i.appPackage!!
                            )
                            call.addTile(entity)
                            pos += 1
                        }
                        withContext(Dispatchers.Main) {
                            requireActivity().supportFragmentManager.commit {
                                replace(R.id.fragment_container_view, AlmostDoneFragment(), "oobe")
                            }
                        }
                    }
                }
                selectAll.setOnClickListener {
                    mAdapter.selectAll()
                }
                removeAll.setOnClickListener {
                    mAdapter.removeAll()
                }
                loading!!.hideProgressBar()
                loading!!.visibility = View.GONE
            }
        }
    }
    private fun enterAnimation(exit: Boolean) {
        if(main == null) {
            return
        }
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 0f, -1000f),
                ObjectAnimator.ofFloat(main!!, "alpha", 1f, 0f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 1000f, 0f),
                ObjectAnimator.ofFloat(main!!, "alpha", 0f, 1f),
            )
        }
        animatorSet.setDuration(300)
        animatorSet.start()
    }

    override fun onResume() {
        enterAnimation(false)
        super.onResume()
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
        @SuppressLint("NotifyDataSetChanged")
        fun selectAll() {
            adapterApps.forEach {
                it.selected = true
                if (!selectedItems!!.contains(it)) {
                    selectedItems!!.add(it)
                }
            }
            notifyDataSetChanged()
        }
        @SuppressLint("NotifyDataSetChanged")
        fun removeAll() {
            adapterApps.forEach {
                it.selected = false
                if (selectedItems!!.contains(it)) {
                    selectedItems!!.remove(it)
                }
            }
            notifyDataSetChanged()
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = adapterApps[position]
            holder as OOBEAppHolder
            try {
                holder.icon.setImageBitmap(hashCache.get(item.id))
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
            holder.checkbox.isChecked = adapterApps[position].selected
        }
    }
    inner class OOBEAppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val label: MaterialTextView = itemView.findViewById(R.id.app_label)
        val checkbox: MaterialCheckBox = itemView.findViewById(R.id.app_checkbox)
    }
}
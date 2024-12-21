package ru.queuejw.mpl.content.oobe.fragments

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.data.app.App
import ru.queuejw.mpl.content.data.tile.Tile
import ru.queuejw.mpl.content.data.tile.TileData
import ru.queuejw.mpl.content.oobe.OOBEActivity
import ru.queuejw.mpl.databinding.OobeAppItemBinding
import ru.queuejw.mpl.databinding.OobeFragmentAppsBinding
import ru.queuejw.mpl.helpers.iconpack.IconPackManager
import ru.queuejw.mpl.helpers.utils.Utils
import ru.queuejw.mpl.helpers.utils.Utils.Companion.generatePlaceholder
import ru.queuejw.mpl.helpers.utils.Utils.Companion.setUpApps
import kotlin.random.Random

class AppsFragment : Fragment() {

    private val hashCache = SparseArrayCompat<Drawable?>()
    private val saveAppsCoroutine = CoroutineScope(Dispatchers.IO)

    private var _binding: OobeFragmentAppsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OobeFragmentAppsBinding.inflate(inflater, container, false)
        binding.oobeAppsLoadingBar.showProgressBar()
        lifecycleScope.launch(Dispatchers.IO) {
            PREFS.prefs.edit().putBoolean("placeholdersGenerated", true).apply()
            generatePlaceholder(TileData.getTileData(requireContext()).getTileDao(), 64)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch(Dispatchers.Default) {
            selectedItems = ArrayList<App>()
            val appList = setUpApps(requireContext())
            val mAdapter = AppAdapter(appList)
            val lm = LinearLayoutManager(requireContext())
            var iconManager: IconPackManager? = null
            var isCustomIconsInstalled = false
            if (PREFS.iconPackPackage != "null") {
                iconManager = IconPackManager(requireContext())
                isCustomIconsInstalled = true
            }
            requireContext().resources.getDimensionPixelSize(R.dimen.iconAppsListSize)
            val pm = requireContext().packageManager
            appList.forEach {
                if (it.type != 1) {
                    val bmp = if (!isCustomIconsInstalled)
                        pm.getApplicationIcon(it.appPackage!!)
                    else
                        iconManager?.getIconPackWithName(PREFS.iconPackPackage)
                            ?.getDrawableIconForPackage(it.appPackage!!, null)
                    hashCache.append(it.id, bmp)
                }
            }
            withContext(Dispatchers.Main) {
                binding.oobeRecycler.apply {
                    layoutManager = lm
                    adapter = mAdapter
                }
                binding.oobeSelectAll.setOnClickListener {
                    mAdapter.selectAll()
                }
                binding.oobeRemoveAll.setOnClickListener {
                    mAdapter.removeAll()
                }
                binding.oobeAppsLoadingBar.apply {
                    hideProgressBar()
                    visibility = View.GONE
                }
            }
        }
        (requireActivity() as OOBEActivity).apply {
            nextFragment = 6
            previousFragment = 2
            updateNextButtonText(this.getString(R.string.next))
            updatePreviousButtonText(this.getString(R.string.back))
            enableAllButtons()
            animateBottomBarFromFragment()
            setText(getString(R.string.configureApps))
        }
    }

    private fun addApps() {
        selectedItems ?: return
        if(selectedItems!!.isEmpty()) return
        saveAppsCoroutine.launch {
            val call = TileData.getTileData(requireContext()).getTileDao()
            var pos = 0
            for (i in selectedItems!!) {
                val id = Random.nextLong(1000, 2000000)
                val entity = Tile(
                    pos, id, -1, 0,
                    isSelected = false,
                    tileSize = Utils.generateRandomTileSize(false),
                    tileLabel = i.appLabel!!,
                    tilePackage = i.appPackage!!
                )
                call.updateTile(entity)
                pos += 1
            }
        }
    }

    override fun onStop() {
        super.onStop()
        addApps()
    }

    companion object {
        var selectedItems: MutableList<App>? = null
        var latestItem: Int? = null
    }

    inner class AppAdapter(private var adapterApps: MutableList<App>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return OOBEAppHolder(
                OobeAppItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun getItemCount(): Int {
            return adapterApps.size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun selectAll() {
            adapterApps.forEach {
                it.selected = true
                if (!selectedItems!!.contains(it)) selectedItems!!.add(it)
            }
            notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        fun removeAll() {
            adapterApps.forEach {
                it.selected = false
                if (selectedItems!!.contains(it)) selectedItems!!.remove(it)
            }
            notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = adapterApps[position]
            holder as OOBEAppHolder
            holder.binding.appIcon.setImageDrawable(hashCache[item.id])
            holder.binding.appLabel.text = item.appLabel
            holder.binding.appCheckbox.setOnCheckedChangeListener { _, isChecked ->
                latestItem = position
                if (isChecked) {
                    item.selected = true
                    if (!selectedItems!!.contains(item)) selectedItems!!.add(item)
                } else {
                    item.selected = false
                    if (selectedItems!!.contains(item)) selectedItems!!.remove(item)
                }
            }
            holder.binding.appCheckbox.isChecked = adapterApps[position].selected
        }
    }

    inner class OOBEAppHolder(val binding: OobeAppItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
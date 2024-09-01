package ru.dimon6018.metrolauncher.content.oobe.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.SparseArrayCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.app.App
import ru.dimon6018.metrolauncher.content.data.tile.Tile
import ru.dimon6018.metrolauncher.content.data.tile.TileDao
import ru.dimon6018.metrolauncher.content.data.tile.TileData
import ru.dimon6018.metrolauncher.content.oobe.WelcomeActivity
import ru.dimon6018.metrolauncher.databinding.OobeAppItemBinding
import ru.dimon6018.metrolauncher.databinding.OobeFragmentAppsBinding
import ru.dimon6018.metrolauncher.helpers.IconPackManager
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.generateRandomTileSize
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.setUpApps
import kotlin.random.Random

class AppsFragment: Fragment() {

    private val hashCache = SparseArrayCompat<Drawable?>()

    private var _binding: OobeFragmentAppsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OobeFragmentAppsBinding.inflate(inflater, container, false)
        (requireActivity() as WelcomeActivity).setText(getString(R.string.configureApps))
        binding.oobeAppsLoadingBar.showProgressBar()
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val call = TileData.getTileData(requireContext()).getTileDao()
        lifecycleScope.launch(Dispatchers.Default) {
            selectedItems = ArrayList()
            val appList = setUpApps(requireContext().packageManager, requireContext())
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
                binding.back.setOnClickListener {
                    lifecycleScope.launch {
                        enterAnimation(true)
                        delay(200)
                        requireActivity().supportFragmentManager.commit {
                            replace(R.id.fragment_container_view, ConfigureFragment(), "oobe")
                        }
                    }
                }
                binding.next.setOnClickListener {
                    if(selectedItems!!.isEmpty()) {
                        WPDialog(requireContext()).apply {
                            setTopDialog(true)
                            setTitle(getString(R.string.reset_warning_title))
                            setMessage(getString(R.string.oobe_apps_warn))
                            setPositiveButton(getString(R.string.no)) {
                                dismiss()
                            }
                            setNegativeButton(getString(R.string.yes)) {
                                addApps(call)
                                enterAnimation(true)
                                dismiss()
                            }
                            show()
                        }
                    } else {
                        addApps(call)
                        enterAnimation(true)
                    }
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
    }
    private fun addApps(call: TileDao) {
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
                call.updateTile(entity)
                pos += 1
            }
            withContext(Dispatchers.Main) {
                requireActivity().supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, AlmostDoneFragment(), "oobe")
                }
            }
        }
    }
    private fun enterAnimation(exit: Boolean) {
        val main = binding.root
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 0f, -1000f),
                ObjectAnimator.ofFloat(main, "alpha", 1f, 0f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 1000f, 0f),
                ObjectAnimator.ofFloat(main, "alpha", 0f, 1f),
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

    inner class AppAdapter(private var adapterApps: MutableList<App>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return OOBEAppHolder(OobeAppItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
            holder.binding.appIcon.setImageDrawable(hashCache[item.id])
            holder.binding.appLabel.text = item.appLabel
            holder.binding.appCheckbox.setOnCheckedChangeListener { _, isChecked ->
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
            holder.binding.appCheckbox.isChecked = adapterApps[position].selected
        }
    }
    inner class OOBEAppHolder(val binding: OobeAppItemBinding) : RecyclerView.ViewHolder(binding.root)
}
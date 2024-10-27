package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsIconBinding
import ru.dimon6018.metrolauncher.helpers.iconpack.IconPackManager
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets


class IconSettingsActivity: AppCompatActivity() {

    private val iconPackManager: IconPackManager by lazy {
        IconPackManager(this)
    }
    private var iconPackArrayList: ArrayList<IconPackManager.IconPack> = ArrayList()

    private var mAdapter: IconPackAdapterList? = null

    private var isIconPackListEmpty = false
    private var isListVisible = false
    private var isError = false

    private var dialog: WPDialog? = null

    private var appList = ArrayList<IconPackItem>()

    private lateinit var binding: LauncherSettingsIconBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsIconBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        createDialog()
        initView()
        setIconPacks()
        applyWindowInsets(binding.root)
    }

    private fun createDialog() {
        dialog = WPDialog(this).setTopDialog(true)
            .setTitle(getString(R.string.tip))
            .setMessage(getString(R.string.tipIconPackError))
            .setPositiveButton(getString(android.R.string.ok), null)
    }

    private fun initView() {
        binding.settingsInclude.chooseIconPack.setOnClickListener {
            setIconPacks()
            if(!isIconPackListEmpty) {
                if(!isListVisible) {
                    isListVisible = true
                    binding.settingsInclude.iconPackList.visibility = View.VISIBLE
                } else {
                    isListVisible = false
                    binding.settingsInclude.iconPackList.visibility = View.GONE
                }
            } else {
                dialog?.show()
            }
            setUi()
        }
        binding.settingsInclude.removeIconPack.setOnClickListener {
            PREFS.apply {
                iconPackPackage = "null"
                iconPackChanged = true
                isPrefsChanged = true
            }
            setUi()
        }
        binding.settingsInclude.downloadIconPacks.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/queuejw/mpl_updates/releases/download/release/Lawnicons.apk")))
        }
    }
    private fun setIconPacks() {
        isError = false
        iconPackArrayList = iconPackManager.getAvailableIconPacks(true)
        isIconPackListEmpty = iconPackArrayList.isEmpty()
        setUi()
        appList.clear()
        if(iconPackArrayList.isNotEmpty()) {
            for (i in iconPackArrayList) {
                val app = IconPackItem()
                app.appPackage = i.packageName!!
                app.name = i.name!!
                appList.add(app)
            }
        }
        if(mAdapter != null) {
            mAdapter = null
        }
        mAdapter = IconPackAdapterList(appList)
        binding.settingsInclude.iconPackList.apply {
            layoutManager = LinearLayoutManager(this@IconSettingsActivity)
            adapter = mAdapter
        }
    }

    private fun setUi() {
        if (isIconPackListEmpty) {
            binding.settingsInclude.currentIconPackText.visibility = View.GONE
            binding.settingsInclude.currentIconPackError.apply {
                visibility = View.VISIBLE
                text = if(isError) getString(R.string.error) else getString(R.string.iconpack_error)
            }
            binding.settingsInclude.removeIconPack.visibility = View.GONE
        } else {
            val label = if(PREFS.iconPackPackage == "null") {
                binding.settingsInclude.currentIconPackText.visibility = View.GONE
                binding.settingsInclude.removeIconPack.visibility = View.GONE
                binding.settingsInclude.currentIconPackError.apply {
                    visibility = View.VISIBLE
                    text = getString(R.string.iconpack_error)
                }
                "null"
            } else {
                runCatching {
                    binding.settingsInclude.currentIconPackText.visibility = View.VISIBLE
                    binding.settingsInclude.currentIconPackError.visibility = View.GONE
                    binding.settingsInclude.removeIconPack.visibility = View.VISIBLE
                    packageManager!!.getApplicationLabel(packageManager.getApplicationInfo(PREFS.iconPackPackage!!, 0)).toString()
                }.getOrElse {
                    binding.settingsInclude.currentIconPackText.visibility = View.GONE
                    binding.settingsInclude.currentIconPackError.apply {
                        visibility = View.VISIBLE
                        text = getString(R.string.iconpack_error)
                    }
                    "null"
                }
            }
            binding.settingsInclude.currentIconPackText.text = getString(R.string.current_iconpack, label)
        }
        binding.settingsInclude.chooseIconPack.text =  if(isListVisible) getString(android.R.string.cancel) else getString(R.string.choose_icon_pack)
    }
    private fun enterAnimation(exit: Boolean) {
        if (!PREFS.isTransitionAnimEnabled) {
            return
        }
        val main = binding.root
        val animatorSet = AnimatorSet().apply {
            playTogether(
                createObjectAnimator(main, "translationX", if (exit) 0f else -300f, if (exit) -300f else 0f),
                createObjectAnimator(main, "rotationY", if (exit) 0f else 90f, if (exit) 90f else 0f),
                createObjectAnimator(main, "alpha", if (exit) 1f else 0f, if (exit) 0f else 1f),
                createObjectAnimator(main, "scaleX", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f),
                createObjectAnimator(main, "scaleY", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f)
            )
            duration = 400
        }
        animatorSet.start()
    }
    private fun createObjectAnimator(target: Any, property: String, startValue: Float, endValue: Float): ObjectAnimator {
        return ObjectAnimator.ofFloat(target, property, startValue, endValue)
    }

    override fun onResume() {
        enterAnimation(false)
        super.onResume()
    }

    override fun onPause() {
        enterAnimation(true)
        super.onPause()
    }
    inner class IconPackAdapterList(private var list: MutableList<IconPackItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var iconSize = resources.getDimensionPixelSize(R.dimen.iconAppsListSize)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return IconPackHolder(LayoutInflater.from(parent.context).inflate(R.layout.app, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder as IconPackHolder
            val item = list[position]
            holder.label.text = item.name
            holder.icon.setImageBitmap(packageManager.getApplicationIcon(item.appPackage).toBitmap(iconSize, iconSize))
            holder.itemView.setOnClickListener {
                PREFS.apply {
                    iconPackPackage = item.appPackage
                    iconPackChanged = true
                    isPrefsChanged = true
                }
                binding.settingsInclude.iconPackList.visibility = View.GONE
                isListVisible = false
                setUi()
            }
        }
    }
    class IconPackHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.app_icon)
        val label: MaterialTextView = itemView.findViewById(R.id.app_label)
    }
    class IconPackItem {
        var name: String = ""
        var appPackage: String = ""
    }
}
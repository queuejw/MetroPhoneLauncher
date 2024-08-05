package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.IconPackManager
import ru.dimon6018.metrolauncher.helpers.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme


class IconSettingsActivity: AppCompatActivity() {

    private lateinit var chooseBtn: MaterialButton
    private lateinit var currentPackTextView: MaterialTextView
    private lateinit var currentPackErrorTextView: MaterialTextView
    private lateinit var removePack: MaterialTextView
    private lateinit var downloadBtn: MaterialButton

    private var iconPackManager: IconPackManager? = null
    private var iconPackArrayList: ArrayList<IconPackManager.IconPack> = ArrayList()

    private lateinit var mRecyclerView: RecyclerView
    private var mAdapter: IconPackAdapterList? = null

    private var isIconPackListEmpty = false
    private var isListVisible = false
    private var isError = false
    private var packageMgr: PackageManager? = null

    private var dialog: WPDialog? = null

    private lateinit var main: CoordinatorLayout
    private var appList = ArrayList<IconPackItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_icon)
        packageMgr = packageManager
        iconPackManager = IconPackManager(this)
        chooseBtn = findViewById(R.id.chooseIconPack)
        removePack = findViewById(R.id.removeIconPack)
        currentPackTextView = findViewById(R.id.currentIconPackText)
        currentPackErrorTextView = findViewById(R.id.currentIconPackError)
        downloadBtn = findViewById(R.id.downloadIconPacks)
        mRecyclerView = findViewById(R.id.iconPackList)
        dialog = WPDialog(this).setTopDialog(true)
            .setTitle(getString(R.string.tip))
            .setMessage(getString(R.string.tipIconPackError))
            .setPositiveButton(getString(android.R.string.ok), null)
        chooseBtn.setOnClickListener {
            setIconPacks()
            if(!isIconPackListEmpty) {
                if(!isListVisible) {
                    isListVisible = true
                    mRecyclerView.visibility = View.VISIBLE
                } else {
                    isListVisible = false
                    mRecyclerView.visibility = View.GONE
                }
            } else {
                dialog?.show()
            }
            setUi()
        }
        removePack.setOnClickListener {
            PREFS!!.setIconPack("null")
            PREFS!!.isPrefsChanged = true
            setUi()
        }
        downloadBtn.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/queuejw/lawnicons-m/releases/latest")))
        }
        setIconPacks()
        main = findViewById(R.id.coordinator)
        applyWindowInsets(main)
    }

    private fun setIconPacks() {
        if (iconPackManager == null) {
            isError = true
            return
        }
        isError = false
        iconPackArrayList = iconPackManager!!.getAvailableIconPacks(true)
        isIconPackListEmpty = iconPackArrayList.isEmpty()
        setUi()
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
        mRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@IconSettingsActivity)
            adapter = mAdapter
        }
    }

    private fun setUi() {
        if (isIconPackListEmpty) {
            currentPackTextView.visibility = View.GONE
            currentPackErrorTextView.visibility = View.VISIBLE
            if(isError) {
                currentPackErrorTextView.text = getString(R.string.error)
            } else {
                currentPackErrorTextView.text = getString(R.string.iconpack_error)
            }
            removePack.visibility = View.GONE
        } else {
            val label = if(PREFS!!.iconPackPackage == "null") {
                currentPackTextView.visibility = View.GONE
                currentPackErrorTextView.visibility = View.VISIBLE
                currentPackErrorTextView.text = getString(R.string.iconpack_error)
                removePack.visibility = View.GONE
                "null"
            } else {
                try {
                    currentPackTextView.visibility = View.VISIBLE
                    currentPackErrorTextView.visibility = View.GONE
                    removePack.visibility = View.VISIBLE
                    packageMgr!!.getApplicationLabel(packageMgr!!.getApplicationInfo(PREFS!!.iconPackPackage!!, 0))
                } catch (e: PackageManager.NameNotFoundException) {
                    currentPackTextView.visibility = View.GONE
                    currentPackErrorTextView.visibility = View.VISIBLE
                    currentPackErrorTextView.text = getString(R.string.iconpack_error)
                    "null"
                }
            }
            currentPackTextView.text = getString(R.string.current_iconpack, label)
        }
        if(isListVisible) {
            chooseBtn.text = getString(android.R.string.cancel)
        } else {
            chooseBtn.text = getString(R.string.choose_icon_pack)
        }
    }
    private fun enterAnimation(exit: Boolean) {
        if(!PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 0f, 300f),
                ObjectAnimator.ofFloat(main, "rotationY", 0f, 90f),
                ObjectAnimator.ofFloat(main, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(main, "scaleX", 1f, 0.5f),
                ObjectAnimator.ofFloat(main, "scaleY", 1f, 0.5f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(main, "rotationY", 90f, 0f),
                ObjectAnimator.ofFloat(main, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(main, "scaleX", 0.5f, 1f),
                ObjectAnimator.ofFloat(main, "scaleY", 0.5f, 1f)
            )
        }
        animatorSet.setDuration(400)
        animatorSet.start()
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
                PREFS!!.setIconPack(item.appPackage)
                PREFS!!.isPrefsChanged = true
                mRecyclerView.visibility = View.GONE
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
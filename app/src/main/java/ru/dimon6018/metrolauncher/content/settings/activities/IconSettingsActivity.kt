package ru.dimon6018.metrolauncher.content.settings.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.Application.Companion.recompressIcon
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.IconPackManager


class IconSettingsActivity: AppCompatActivity() {

    private var chooseBtn: MaterialButton? = null
    private var currentPackTextView: TextView? = null
    private var currentPackErrorTextView: TextView? = null
    private var removePack: TextView? = null

    private var downloadBtn: MaterialButton? = null

    private var iconPackManager: IconPackManager? = null
    private var iconPackArrayList: ArrayList<IconPackManager.IconPack> = ArrayList()

    private var mRecyclerView: RecyclerView? = null
    private var adapter: IconPackAdapterList? = null

    private var isIconPackListEmpty = false
    private var isListVisible = false
    private var isError = false
    private var packageMgr: PackageManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_icon)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        applyWindowInsets(coord)
        packageMgr = packageManager
        iconPackManager = IconPackManager()
        iconPackManager?.setContext(this)
        chooseBtn = findViewById(R.id.chooseIconPack)
        removePack = findViewById(R.id.removeIconPack)
        currentPackTextView = findViewById(R.id.currentIconPackText)
        currentPackErrorTextView = findViewById(R.id.currentIconPackError)
        downloadBtn = findViewById(R.id.downloadIconPacks)
        mRecyclerView = findViewById(R.id.iconPackList)
        chooseBtn!!.setOnClickListener {
            if(!isIconPackListEmpty) {
                if(!isListVisible) {
                    isListVisible = true
                    mRecyclerView!!.visibility = View.VISIBLE
                } else {
                    isListVisible = false
                    mRecyclerView!!.visibility = View.GONE
                }
            }
            setUi()
        }
        removePack!!.setOnClickListener {
            PREFS!!.setIconPack("null")
            setUi()
        }
        downloadBtn!!.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/queuejw/lawnicons-m/releases/latest")))
        }
        setIconPacks()
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
        val appList = ArrayList<IconPackItem>()
        if(iconPackArrayList.isNotEmpty()) {
            for (i in iconPackArrayList) {
                val app = IconPackItem()
                app.appPackage = i.packageName!!
                app.name = i.name!!
                appList.add(app)
            }
        }
        adapter = IconPackAdapterList(appList)
        val lm = LinearLayoutManager(this)
        mRecyclerView!!.layoutManager = lm
        mRecyclerView!!.adapter = adapter
    }

    private fun setUi() {
        if (isIconPackListEmpty) {
            currentPackTextView!!.visibility = View.GONE
            currentPackErrorTextView!!.visibility = View.VISIBLE
            if(isError) {
                currentPackErrorTextView!!.text = "error"
            } else {
                currentPackErrorTextView!!.text = getString(R.string.iconpack_error)
            }
            removePack!!.visibility = View.GONE
        } else {
            val label = if(PREFS!!.iconPackPackage == "null") {
                currentPackTextView!!.visibility = View.GONE
                currentPackErrorTextView!!.visibility = View.VISIBLE
                currentPackErrorTextView!!.text = getString(R.string.iconpack_error)
                removePack!!.visibility = View.GONE
                "null"
            } else {
                try {
                    currentPackTextView!!.visibility = View.VISIBLE
                    currentPackErrorTextView!!.visibility = View.GONE
                    removePack!!.visibility = View.VISIBLE
                    packageMgr!!.getApplicationLabel(packageMgr!!.getApplicationInfo(PREFS!!.iconPackPackage!!, 0))
                } catch (e: PackageManager.NameNotFoundException) {
                    currentPackTextView!!.visibility = View.GONE
                    currentPackErrorTextView!!.visibility = View.VISIBLE
                    currentPackErrorTextView!!.text = getString(R.string.iconpack_error)
                    "null"
                }
            }
            currentPackTextView!!.text = getString(R.string.current_iconpack, label)
        }
        if(isListVisible) {
            chooseBtn!!.text = getString(android.R.string.cancel)
        } else {
            chooseBtn!!.text = getString(R.string.choose_icon_pack)
        }
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
            holder.icon.setImageIcon(recompressIcon(packageManager.getApplicationIcon(item.appPackage).toBitmap(iconSize, iconSize), 25))
            holder.itemView.setOnClickListener {
                PREFS!!.setIconPack(item.appPackage)
                PREFS!!.setPrefsChanged(true)
                mRecyclerView!!.visibility = View.GONE
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
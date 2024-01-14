package ru.dimon6018.metrolauncher.content

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import ir.alirezabdn.wp7progress.WP7ProgressBar
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.Start.Companion.tileList
import ru.dimon6018.metrolauncher.content.data.App
import ru.dimon6018.metrolauncher.content.data.AppEntity
import ru.dimon6018.metrolauncher.content.settings.SettingsActivity
import java.util.Collections
import java.util.Locale
import kotlin.random.Random


class AllApps : Fragment(R.layout.all_apps_screen) {
    private var mApps: ArrayList<App>? = null

    private var recyclerView: RecyclerView? = null
    private var search: TextInputLayout? = null
    private var appAdapter: AppAdapter? = null
    private var searchBtn: MaterialCardView? = null
    private var searchBtnBack: MaterialCardView? = null
    private var loadingHolder: LinearLayout? = null
    private var progressBar: WP7ProgressBar? = null
    var contxt: Context? = null
    private var sharedPool = RecycledViewPool()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.all_apps_screen, container, false)
        contxt = context
        progressBar = view.findViewById(R.id.progressBar)
        progressBar!!.setIndicatorRadius(5)
        progressBar!!.showProgressBar()
        recyclerView = view.findViewById(R.id.app_list)
        setRecyclerPadding(96)
        searchBtn = view.findViewById(R.id.searchButton)
        searchBtnBack = view.findViewById(R.id.searchBackBtn)
        search = view.findViewById(R.id.search)
        loadingHolder = view.findViewById(R.id.loadingHolder)
        searchBtn!!.setOnClickListener { searchFunction() }
        progressBar!!.showProgressBar()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Thread {
            mApps = ArrayList()
            appAdapter = AppAdapter(mApps!!)
            appsList = getAppList(contxt!!)
            getHeaderListLatter(appsList)
            requireActivity().runOnUiThread {
                recyclerView!!.setLayoutManager(LinearLayoutManager(contxt))
                recyclerView!!.adapter = appAdapter
                hideLoadingHolder()
            }
        }.start()
        searchBtnBack!!.setOnClickListener {
            searchBtn!!.visibility = View.VISIBLE
            search!!.visibility = View.GONE
            searchBtnBack!!.visibility = View.GONE
            setRecyclerPadding(96)
            getHeaderListLatter(appsList)
            appAdapter!!.restoreAppList()
        }
    }
    private fun hideLoadingHolder() {
        progressBar!!.hideProgressBar()
        loadingHolder!!.visibility = View.GONE
        recyclerView!!.visibility = View.VISIBLE
    }
    private fun setRecyclerPadding(pad: Int) {
        recyclerView!!.setPadding(pad, 0, 0 ,0)
    }
    private fun searchFunction() {
        searchBtn!!.visibility = View.GONE
        search!!.visibility = View.VISIBLE
        search!!.isFocusable = true
        searchBtnBack!!.visibility = View.VISIBLE
        setRecyclerPadding(0)
        Thread {
            removeHeaders()
            (search!!.editText as? AutoCompleteTextView)?.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    filterText(s.toString())
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {
                }

                override fun afterTextChanged(s: Editable) {
                }
            })
        }.start()
    }
    private fun filterText(text: String) {
        val filteredlist: ArrayList<App> = ArrayList()
        for (item in appsList!!) {
            if (item.appLabel!!.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
        } else {
            appAdapter!!.setNewFilteredList(filteredlist)
        }
    }

    private fun getHeaderListLatter(newApps: List<App>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (newApps != null) {
                Collections.sort(newApps, Comparator.comparing { app: App -> app.appLabel!![0].toString().uppercase(Locale.getDefault()) })
            }
        } else {
            if (newApps != null) {
                Collections.sort(newApps) { app1: App, app2: App -> app1.appLabel!![0].toString().uppercase(Locale.getDefault()).compareTo(app2.appLabel!![0].toString().uppercase(Locale.getDefault())) }
            }
        }
        var lastHeader: String? = ""
        for (app in newApps!!) {
            val header = app.appLabel!![0].toString().uppercase(Locale.getDefault())
            if (!TextUtils.equals(lastHeader, header)) {
                lastHeader = header
                val head = App()
                head.appLabel = header
                head.isSection = true
                mApps!!.add(head)
            }
            mApps!!.add(app)
        }
    }
    private fun removeHeaders() {
        var temp = mApps!!.size
        while (temp != 0) {
            temp -= 1
            val item = mApps!![temp]
            if (item.isSection) {
                mApps!!.remove(item)
            }
        }
        appAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        appAdapter?.notifyDataSetChanged()
        super.onResume()
    }
    inner class AppAdapter internal constructor(private var appsList: List<App>) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
        fun setNewFilteredList(appListFiltered: List<App>) {
            appsList = appListFiltered
            notifyDataSetChanged()
        }

        fun restoreAppList() {
            appsList = Companion.appsList!!
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == Companion.SECTION_VIEW) {
                SectionHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.abc, parent, false))
            } else {
                recyclerView!!.setRecycledViewPool(sharedPool)
                AppHolder(LayoutInflater.from(parent.context).inflate(R.layout.app, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (Companion.SECTION_VIEW == getItemViewType(position)) {
                val sectionHeaderViewHolder = holder as SectionHeaderViewHolder
                val sectionItem: App = appsList[position]
                sectionHeaderViewHolder.headerTitleTextview.text = sectionItem.appLabel
                return
            }
            val holder1 = holder as AppHolder
            val app: App = appsList[position]
            try {
                val bmp = contxt!!.packageManager!!.getApplicationIcon(app.appPackage!!).toBitmap(72, 72)
                holder.icon.setImageBitmap(bmp)
            } catch (e: PackageManager.NameNotFoundException) {
                holder.icon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_os_android, null))
            }
            holder1.label.text = app.appLabel
            holder1.itemView.setOnClickListener {
                val intent: Intent = if(app.appPackage == activity?.packageName) {
                    Intent(activity, SettingsActivity::class.java)
                } else {
                    contxt!!.packageManager.getLaunchIntentForPackage(app.appPackage!!)!!
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            holder1.itemView.setOnLongClickListener {
                holder1.layout.visibility = View.VISIBLE
                true
            }
            holder1.pin.setOnClickListener {
                val text = app.appLabel!!
                val packag = app.appPackage!!
                Thread {
                    val pos = Start.dbCall!!.getJustApps().size
                    val id = Random.nextInt(1000, 20000)
                    val item = AppEntity(pos, id,-1,"small", text, packag)
                    Start.dbCall!!.insertItem(item)
                    tileList = Start.dbCall!!.getJustApps()
                }.start()
                holder1.layout.visibility = View.GONE
                activity!!.onBackPressed()
            }
            holder1.share.setOnClickListener {
                holder1.layout.visibility = View.GONE
            }
            holder1.uninstall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.setData(Uri.parse("package:" + app.appPackage))
                startActivity(intent)
                holder1.layout.visibility = View.GONE
            }
        }
        override fun getItemViewType(position: Int): Int {
            return if (appsList[position].isSection) {
                Companion.SECTION_VIEW
            } else {
                Companion.CONTENT_VIEW
            }
        }

        override fun getItemCount(): Int {
            return appsList.size
        }

        inner class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView
            val label: TextView
            val layout: LinearLayout
            val pin: MaterialCardView
            val share: MaterialCardView
            val uninstall: MaterialCardView

            init {
                icon = itemView.findViewById(R.id.app_icon)
                label = itemView.findViewById(R.id.app_label)
                layout = itemView.findViewById(R.id.appSettings)
                pin = itemView.findViewById(R.id.pinApp)
                share = itemView.findViewById(R.id.shareApp)
                uninstall = itemView.findViewById(R.id.uninstallApp)
            }
        }

        inner class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var headerTitleTextview: MaterialTextView

            init {
                headerTitleTextview = itemView.findViewById(R.id.abc_label)
            }
        }
    }
    companion object {
        const val SECTION_VIEW = 0
        const val CONTENT_VIEW = 1
        var appsList: ArrayList<App>? = null
        private fun getAppList(context: Context): ArrayList<App> {
            val list = ArrayList<App>()
            val i = Intent(Intent.ACTION_MAIN, null)
            i.addCategory(Intent.CATEGORY_LAUNCHER)
            val pManager = context.packageManager
            val allApps = pManager.queryIntentActivities(i, 0)
            for (ri in allApps) {
                val app = App()
                app.appLabel = ri.loadLabel(pManager) as String
                app.appPackage = ri.activityInfo.packageName
                app.isSection = false
                list.add(app)
            }
            return list
        }
    }
}
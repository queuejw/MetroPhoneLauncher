package ru.dimon6018.metrolauncher.content

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import ir.alirezabdn.wp7progress.WP7ProgressBar
import ru.dimon6018.metrolauncher.Main
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.App
import ru.dimon6018.metrolauncher.content.data.DataProvider.ConcreteData
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider
import java.lang.ref.WeakReference
import java.util.*


class AllApps : Fragment(R.layout.all_apps_screen) {
    private var mApps: ArrayList<App>? = null
    private var recyclerView: RecyclerView? = null
    private var searchView: SearchView? = null
    private var appAdapter: AppAdapter? = null
    private var searchBtn: MaterialCardView? = null
    private var searchCard: MaterialCardView? = null
    var loadingHolder: LinearLayout? = null
    var progressBar: WP7ProgressBar? = null
    var contxt: Context? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.all_apps_screen, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar!!.showProgressBar()
        recyclerView = view.findViewById(R.id.app_list)
        searchBtn = view.findViewById(R.id.search_btn)
        searchCard = view.findViewById(R.id.search_card)
        searchView = view.findViewById(R.id.searchView)
        loadingHolder = view.findViewById(R.id.loadingHolder)
        searchBtn!!.setOnClickListener { searchFunction() }
        progressBar!!.showProgressBar()
        contxt = context
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val thread = Thread {
            mApps = ArrayList()
            appAdapter = AppAdapter(contxt, mApps!!)
            setUpApps()
            getHeaderListLatter(appsList)
            recyclerView!!.post {
                recyclerView!!.setLayoutManager(LinearLayoutManager(contxt))
                recyclerView!!.adapter = appAdapter
                appAdapter!!.hideLoadingHolder()
            }
        }
        thread.priority = 1
        thread.start()
    }
    private fun closeSearch() {
        searchView!!.clearFocus()
        searchCard!!.visibility = View.GONE
        searchBtn!!.visibility = View.VISIBLE
        appAdapter!!.restoreAppList()
    }

    private fun searchFunction() {
        removeHeaders()
        searchBtn!!.visibility = View.GONE
        searchCard!!.visibility = View.VISIBLE
        searchView!!.setOnCloseListener {
            closeSearch()
            false
        }
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterText(newText)
                return false
            }
        })
    }

    private fun setUpApps() {
        val pManager = contxt!!.packageManager
        appsList = ArrayList()
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val allApps = pManager.queryIntentActivities(i, 0)
        for (ri in allApps) {
            val app = App()
            app.app_label = ri.loadLabel(pManager) as String
            app.app_package = ri.activityInfo.packageName
            app.isSection = false
            app.app_icon = ri.activityInfo.loadIcon(pManager)
            appsList!!.add(app)
        }
    }

    private fun filterText(text: String) {
        val filteredlist: ArrayList<App> = ArrayList()
        for (item in appsList!!) {
            if (item.label.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
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
                Collections.sort(newApps, Comparator.comparing { app: App -> app.app_label[0].toString().uppercase(Locale.getDefault()) })
            }
        } else {
            if (newApps != null) {
                Collections.sort(newApps) { app1: App, app2: App -> app1.app_label[0].toString().uppercase(Locale.getDefault()).compareTo(app2.app_label[0].toString().uppercase(Locale.getDefault())) }
            }
        }
        var lastHeader: String? = ""
            for (app in newApps!!) {
                val header = app.label[0].toString().uppercase(Locale.getDefault())
                if (!TextUtils.equals(lastHeader, header)) {
                    lastHeader = header
                    val head = App()
                    head.app_label = header
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
            appAdapter!!.notifyDataSetChanged()
        }
    inner class AppAdapter internal constructor(context: Context?, private var appsList: List<App>) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
        private val appsListReserved: List<App> = appsList
        private var mContextWeakReference: WeakReference<Context?>
        init {
            mContextWeakReference = WeakReference(context)
        }

        fun setNewFilteredList(appListFiltered: List<App>) {
            appsList = appListFiltered
            notifyDataSetChanged()
        }

        fun restoreAppList() {
            appsList = appsListReserved
            getHeaderListLatter(appsList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == Companion.SECTION_VIEW) {
                SectionHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.abc, parent, false))
            } else AppHolder(LayoutInflater.from(parent.context).inflate(R.layout.app, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (Companion.SECTION_VIEW == getItemViewType(position)) {
                val sectionHeaderViewHolder = holder as SectionHeaderViewHolder
                val sectionItem: App = appsList[position]
                sectionHeaderViewHolder.headerTitleTextview.text = sectionItem.app_label
                return
            }
            val holder1 = holder as AppHolder
            val apps: App = appsList[position]
            holder1.icon.setImageDrawable(apps.drawable)
            holder1.label.text = apps.label
            holder1.itemView.setOnClickListener {
                val intent = contxt!!.packageManager.getLaunchIntentForPackage(apps.packagel)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            holder1.itemView.setOnLongClickListener {
                holder1.layout.visibility = View.VISIBLE
                true
            }
            holder1.pin.setOnClickListener {
                    holder1.layout.visibility = View.GONE
                    val prefs = Prefs(context)
                    if (prefs.isItemPinedToStart(apps.app_package)) {
                        Snackbar.make(holder1.itemView, "Это приложение уже закрепрелено", Snackbar.LENGTH_SHORT).show()
                    } else {
                        val mPrevData: MutableList<App> = Prefs(context).getAppsPackage()
                        val iCount = mPrevData.size
                        prefs.addApp(apps.packagel, apps.label)
                        prefs.setPos(apps.packagel, iCount)
                        prefs.setTileSize(apps.packagel, 0)
                        prefs.setAppTileColorAvailability(apps.app_package, false)
                        mPrevData.add(apps)
                        val text = apps.app_label
                        val packag = apps.app_package
                        apps.tilesize = Prefs(context).getTileSize(apps.app_package)
                        val size = apps.tilesize
                        val icon2: Drawable
                        val pManager = context!!.packageManager
                        icon2 = try {
                            pManager.getApplicationIcon(apps.app_package)
                        } catch (e: PackageManager.NameNotFoundException) {
                            throw RuntimeException(e)
                        }
                        val item = ConcreteData(iCount.toLong(), 0, text, icon2, iCount, packag, size, false, 0)
                        dataProvider.addItem(iCount, item, apps)
                        activity!!.onBackPressed()
                    }
            }
            holder1.share.setOnClickListener {
                holder1.layout.visibility = View.GONE
            }
            holder1.uninstall.setOnClickListener {
                holder1.layout.visibility = View.GONE
            }
        }

        private val dataProvider: AbstractDataProvider
            get() = (requireActivity() as Main).dataProvider

        fun hideLoadingHolder() {
            progressBar!!.hideProgressBar()
            val anim = AlphaAnimation(1f,0f)
            anim.duration = 250
            loadingHolder!!.startAnimation(anim)
            loadingHolder!!.visibility = View.GONE
            recyclerView!!.visibility = View.VISIBLE
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
        private var appsList: MutableList<App>? = null
    }
}
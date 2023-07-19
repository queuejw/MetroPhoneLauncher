package ru.dimon6018.metrolauncher.content

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.arasthel.spannedgridlayoutmanager.SpanSize
import com.arasthel.spannedgridlayoutmanager.SpannedGridLayoutManager
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.helpers.PreferencesControl
import ru.dimon6018.metrolauncher.helpers.PreferencesControl.PrefsListener
import ru.dimon6018.metrolauncher.helpers.SpaceItemDecorator

class Start : Fragment(R.layout.main_screen_content), PrefsListener {

    private var appsList: MutableList<Apps?>? = null
    var spannedGridLayoutManager: SpannedGridLayoutManager? = null
    var mPrefs: PreferencesControl? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpApps()
        mPrefs = PreferencesControl(context)
        val recyclerView = view.findViewById<RecyclerView>(R.id.start_apps_tiles)
        val adapter: RecyclerView.Adapter<StartAppAdapter.AppHolder> = StartAppAdapter(context, appsList)
        spannedGridLayoutManager = SpannedGridLayoutManager(SpannedGridLayoutManager.Orientation.VERTICAL, 3)
        spannedGridLayoutManager!!.itemOrderIsStable = true
        recyclerView.addItemDecoration(SpaceItemDecorator(5, 5, 5, 5))
        spannedGridLayoutManager!!.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { position ->
            when {
                position == 0 -> {
                    /**
                     * 150f is now static
                     * should calculate programmatically in runtime
                     * for to manage header hight for different resolution devices
                     */
                    SpanSize(3, 1)
                }
                position % 7 == 1 ->
                    SpanSize(2, 2)
                else ->
                    SpanSize(1, 1)
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = spannedGridLayoutManager

        mPrefs!!.setListener(this)
        Log.e("Start", mPrefs!!.apps.toString())
    }

    fun setUpApps() {
        val pManager = context?.packageManager
        appsList = ArrayList<Apps?>()
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val allApps = pManager?.queryIntentActivities(i, 0)
            for (ri in allApps!!) {
                val app = Apps()
                app.app_label = ri.loadLabel(pManager)
                app.app_package = ri.activityInfo.packageName
                app.app_icon = ri.activityInfo.loadIcon(pManager)
                appsList?.add(app)
            }
    }
    override fun onPrefsChanged() {}
    inner class StartAppAdapter internal constructor(context: Context?, private var appsList: List<Apps?>?) : RecyclerView.Adapter<StartAppAdapter.AppHolder>() {

        private val inflater: LayoutInflater
        val clickedItems: MutableList<Boolean>

        init {
            clickedItems = MutableList(itemCount) { false }
            setHasStableIds(true)
            inflater = LayoutInflater.from(context)
        }
        override fun getItemId(position: Int): Long {
            return super.getItemId(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
            val view = inflater.inflate(R.layout.start_medium_tile, parent, false)
            return AppHolder(view)
        }

        override fun onBindViewHolder(holder: AppHolder, position: Int) {
            val apps: Apps? = appsList?.get(position)
            val icon = apps?.drawable
            icon!!.setBounds(0, 0, resources.getDimensionPixelSize(R.dimen.app_icon_size), resources.getDimensionPixelSize(R.dimen.app_icon_size))
            holder.icon.setImageDrawable(icon)
            holder.label.text = apps?.label
            holder.itemView.setOnClickListener { view: View? ->
            }
            holder.itemView.setOnLongClickListener { view: View? ->
                showMenu(holder.itemView, R.menu.tile_menu_start, position);
                true
            }
        }
        private fun showMenu(v: View, @MenuRes menuRes: Int, pos: Int) {
            val popup = PopupMenu(context!!, v)
            popup.menuInflater.inflate(menuRes, popup.menu)
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                if (menuItem.itemId == R.id.tile_small) {
                    spannedGridLayoutManager?.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { pos ->
                       SpanSize(1, 1)
                    }
                } else if (menuItem.itemId == R.id.tile_medium) {
                    spannedGridLayoutManager?.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { pos ->
                        SpanSize(2, 2)
                    }
                } else if (menuItem.itemId == R.id.tile_big) {
                    spannedGridLayoutManager?.spanSizeLookup = SpannedGridLayoutManager.SpanSizeLookup { pos ->
                        SpanSize(3, 2)
                    }
                }
                true
            }
            popup.show()
    }
        override fun getItemCount(): Int {
            return appsList?.size!!
        }

        inner class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView
            val label: TextView

            init {
                icon = itemView.findViewById(R.id.app_icon)
                label = itemView.findViewById(R.id.app_label)
            }
        }
    }
}
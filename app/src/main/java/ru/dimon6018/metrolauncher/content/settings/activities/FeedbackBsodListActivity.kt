package ru.dimon6018.metrolauncher.content.settings.activities

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.Application.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.data.bsod.BSODEntity

class FeedbackBsodListActivity: AppCompatActivity() {

    private var db: BSOD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.launcherAccentTheme())
        super.onCreate(savedInstanceState)
        db = BSOD.getData(this)
        setContentView(R.layout.launcher_settings_feedback_bsods)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        applyWindowInsets(coord)
        Thread {
            val adapter = BSODadapter(db!!.getDao().getBsodList(), this, db!!)
            runOnUiThread {
                val recyclerView: RecyclerView = findViewById(R.id.bsodlist_recycler)
                recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                recyclerView.adapter = adapter
            }
        }.start()
    }
}
class BSODadapter(private var data: List<BSODEntity>, private val activity: Activity, private val db: BSOD) :
        RecyclerView.Adapter<BSODadapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val log: TextView
        var date: TextView
        val delete: MaterialCardView
        val share: MaterialCardView

        init {
            log = view.findViewById(R.id.log)
            date = view.findViewById(R.id.date)
            share = view.findViewById(R.id.share)
            delete = view.findViewById(R.id.delete)
        }
    }
    private fun updateList() {
        Thread {
            val newList = db.getDao().getBsodList()
            activity.runOnUiThread {
                data = newList
                notifyDataSetChanged()
            }
        }.start()
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.bsod_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = data[position]
        viewHolder.date.text = item.date.toString()
        viewHolder.log.text = item.log
        viewHolder.delete.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                db.getDao().removeLog(item)
                viewHolder.itemView.post {
                    updateList()
                }
            }
        }
        viewHolder.itemView.setOnClickListener {
            showDialog(item.log)
        }
        viewHolder.share.setOnClickListener {
            sendCrash(item.log, activity)
        }
    }
    private fun showDialog(text: String) {
        MaterialAlertDialogBuilder(activity).setMessage(text).setPositiveButton(activity.getString(R.string.copy)) { _: DialogInterface?, _: Int -> copyError(text) }.setNegativeButton(activity.getString(android.R.string.cancel), null).show()
    }
    private fun copyError(error: String) {
        val clipbrd = activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("MPL_Error", error)
        clipbrd.setPrimaryClip(clip)
    }
    override fun getItemCount() = data.size
    companion object {
        fun sendCrash(text: String, activity: Activity) {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.setData(Uri.parse("mailto:dimon6018t@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "MPL Crash report")
            intent.putExtra(Intent.EXTRA_TEXT, text)
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
            }
        }
    }
}
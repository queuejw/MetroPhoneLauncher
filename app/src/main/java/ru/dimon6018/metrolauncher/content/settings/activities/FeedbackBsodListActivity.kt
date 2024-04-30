package ru.dimon6018.metrolauncher.content.settings.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.data.bsod.BSODEntity
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sendCrash

class FeedbackBsodListActivity: AppCompatActivity() {

    private var db: BSOD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_feedback_bsods)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val coord = findViewById<CoordinatorLayout>(R.id.coordinator)
        applyWindowInsets(coord)
        lifecycleScope.launch(Dispatchers.IO) {
            db = BSOD.getData(this@FeedbackBsodListActivity)
            val adapter =
                BSODadapter(db!!.getDao().getBsodList(), db!!)
            runOnUiThread {
                val recyclerView: RecyclerView = findViewById(R.id.bsodlist_recycler)
                recyclerView.layoutManager = LinearLayoutManager(
                    this@FeedbackBsodListActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                recyclerView.adapter = adapter
            }
        }
    }
    inner class BSODadapter(
        private var data: List<BSODEntity>,
        private val db: BSOD
    ) :
        RecyclerView.Adapter<BSODadapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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
            lifecycleScope.launch(Dispatchers.IO) {
                val newList = db.getDao().getBsodList()
                runOnUiThread {
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
                lifecycleScope.launch(Dispatchers.IO) {
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
                sendCrash(item.log, this@FeedbackBsodListActivity)
            }
        }

        private fun showDialog(text: String) {
            MaterialAlertDialogBuilder(this@FeedbackBsodListActivity).setMessage(text)
                .setPositiveButton(getString(R.string.copy)) { _: DialogInterface?, _: Int ->
                    copyError(text)
                }.setNegativeButton(getString(android.R.string.cancel), null).show()
        }

        private fun copyError(error: String) {
            val clipbrd = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("MPL_Error", error)
            clipbrd.setPrimaryClip(clip)
        }

        override fun getItemCount() = data.size
    }
}
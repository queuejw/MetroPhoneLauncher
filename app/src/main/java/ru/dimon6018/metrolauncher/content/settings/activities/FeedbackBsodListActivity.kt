package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
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
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.data.bsod.BSODEntity
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sendCrash

class FeedbackBsodListActivity: AppCompatActivity() {

    private var db: BSOD? = null
    private var main: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_feedback_bsods)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        main = findViewById(R.id.coordinator)
        main?.apply { applyWindowInsets(this) }
        lifecycleScope.launch(Dispatchers.IO) {
            db = BSOD.getData(this@FeedbackBsodListActivity)
            val mAdapter =
                BSODadapter(db!!.getDao().getBsodList(), db!!)
            runOnUiThread {
                val recyclerView: RecyclerView = findViewById(R.id.bsodlist_recycler)
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(
                        this@FeedbackBsodListActivity,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    adapter = mAdapter
                }
            }
        }
    }
    private fun enterAnimation(exit: Boolean) {
        if(main == null || !PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val animatorSet = AnimatorSet()
        if(exit) {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 0f, 300f),
                ObjectAnimator.ofFloat(main!!, "rotationY", 0f, 90f),
                ObjectAnimator.ofFloat(main!!, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(main!!, "scaleX", 1f, 0.5f),
                ObjectAnimator.ofFloat(main!!, "scaleY", 1f, 0.5f),
            )
        } else {
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(main!!, "translationX", 300f, 0f),
                ObjectAnimator.ofFloat(main!!, "rotationY", 90f, 0f),
                ObjectAnimator.ofFloat(main!!, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(main!!, "scaleX", 0.5f, 1f),
                ObjectAnimator.ofFloat(main!!, "scaleY", 0.5f, 1f)
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
    inner class BSODadapter(
        private var data: List<BSODEntity>,
        private val db: BSOD
    ) :
        RecyclerView.Adapter<BSODadapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val log: TextView = view.findViewById(R.id.log)
            var date: TextView = view.findViewById(R.id.date)
            val delete: MaterialCardView = view.findViewById(R.id.delete)
            val share: MaterialCardView = view.findViewById(R.id.share)
        }
        @SuppressLint("NotifyDataSetChanged")
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
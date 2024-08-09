package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.content.data.bsod.BSODEntity
import ru.dimon6018.metrolauncher.databinding.BsodItemBinding
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsFeedbackBsodsBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.sendCrash

class FeedbackBsodListActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsFeedbackBsodsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        binding = LauncherSettingsFeedbackBsodsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyWindowInsets(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        lifecycleScope.launch(Dispatchers.IO) {
            val db = BSOD.getData(this@FeedbackBsodListActivity)
            val mAdapter = BSODadapter(db.getDao().getBsodList(), db)
            withContext(Dispatchers.Main) {
                binding.settingsInclude.bsodlistRecycler.apply {
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
        if(!PREFS!!.isTransitionAnimEnabled) {
            return
        }
        val main = binding.root
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
    inner class BSODadapter(
        private var data: List<BSODEntity>,
        private val db: BSOD
    ) :
        RecyclerView.Adapter<BSODadapter.ViewHolder>() {

        inner class ViewHolder(val holderBinding: BsodItemBinding) : RecyclerView.ViewHolder(holderBinding.root)

        @SuppressLint("NotifyDataSetChanged")
        private fun updateList() {
            lifecycleScope.launch(Dispatchers.IO) {
                val newList = db.getDao().getBsodList()
                withContext(Dispatchers.Main) {
                    data = newList
                    notifyDataSetChanged()
                }
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(BsodItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false))
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val item = data[position]
            viewHolder.holderBinding.date.text = item.date.toString()
            viewHolder.holderBinding.log.text = item.log
            viewHolder.holderBinding.delete.setOnClickListener {
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
            viewHolder.holderBinding.share.setOnClickListener {
                sendCrash(item.log, this@FeedbackBsodListActivity)
            }
        }
        private fun showDialog(text: String) {
            MaterialAlertDialogBuilder(this@FeedbackBsodListActivity)
                .setMessage(text)
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
package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import ru.dimon6018.metrolauncher.content.data.bsod.BSOD
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsFeedbackBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.launcherAccentTheme

class FeedbackSettingsActivity: AppCompatActivity()  {

    private lateinit var binding: LauncherSettingsFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(launcherAccentTheme())
        binding = LauncherSettingsFeedbackBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        initViews()
        updateMaxLogsSize(binding.settingsInclude.save1bsod,0)
        updateMaxLogsSize(binding.settingsInclude.save5bsod,1)
        updateMaxLogsSize(binding.settingsInclude.save10bsod,2)
        updateMaxLogsSize(binding.settingsInclude.saveallbsods,3)
        applyWindowInsets(binding.root)
    }

    private fun initViews() {
        binding.settingsInclude.showBsodInfo.setOnClickListener {
            startActivity(Intent(this, FeedbackBsodListActivity::class.java))
        }
        binding.settingsInclude.deleteBsodInfo.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                BSOD.getData(this@FeedbackSettingsActivity).clearAllTables()
            }.start()
        }
        binding.settingsInclude.sendFeedbackSwitch.apply {
            isChecked = PREFS!!.isFeedbackEnabled
            text = if(PREFS!!.isFeedbackEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.isFeedbackEnabled = isChecked
                text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.setCrashLogLimitBtn.apply {
            setButtonText(PREFS!!, this)
            setOnClickListener {
                binding.settingsInclude.chooseBsodInfoLimit.visibility = View.VISIBLE
                visibility = View.GONE
            }
        }
        binding.settingsInclude.showErrorDetailsOnBsodSwitch.apply {
            PREFS!!.bsodOutputEnabled
            text = if(PREFS!!.bsodOutputEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS!!.bsodOutputEnabled = isChecked
                text = if(isChecked) getString(R.string.on) else getString(R.string.off)
            }
        }
    }

    private fun updateMaxLogsSize(view: View, value: Int) {
        view.setOnClickListener {
            PREFS!!.maxCrashLogs = value
            binding.settingsInclude.chooseBsodInfoLimit.visibility = View.GONE
            binding.settingsInclude.setCrashLogLimitBtn.visibility = View.VISIBLE
            setButtonText(PREFS!!, binding.settingsInclude.setCrashLogLimitBtn)
        }
    }
    private fun setButtonText(prefs: Prefs, button: MaterialButton) {
        button.text = when(prefs.maxCrashLogs) {
            0 -> getString(R.string.feedback_limit_0)
            1 -> getString(R.string.feedback_limit_1)
            2 -> getString(R.string.feedback_limit_2)
            3 -> getString(R.string.feedback_limit_3)
            else -> getString(R.string.feedback_limit_0)
        }
    }
    private fun enterAnimation(exit: Boolean) {
        if (!PREFS!!.isTransitionAnimEnabled) {
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
}
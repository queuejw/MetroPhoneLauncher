package ru.queuejw.mpl.content.settings.activities

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
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.Application.Companion.customBoldFont
import ru.queuejw.mpl.Application.Companion.customFont
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.data.bsod.BSOD
import ru.queuejw.mpl.databinding.LauncherSettingsFeedbackBinding
import ru.queuejw.mpl.helpers.utils.Utils.Companion.applyWindowInsets

class FeedbackSettingsActivity : AppCompatActivity() {

    private lateinit var binding: LauncherSettingsFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsFeedbackBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        initViews()
        updateMaxLogsSize(binding.settingsInclude.save1bsod, 0)
        updateMaxLogsSize(binding.settingsInclude.save5bsod, 1)
        updateMaxLogsSize(binding.settingsInclude.save10bsod, 2)
        updateMaxLogsSize(binding.settingsInclude.saveallbsods, 3)
        applyWindowInsets(binding.root)
        setupFont()
    }

    private fun setupFont() {
        customFont?.let {
            binding.settingsSectionLabel.typeface = it
            binding.settingsLabel.typeface = it
            binding.settingsInclude.save1bsod.typeface = it
            binding.settingsInclude.save5bsod.typeface = it
            binding.settingsInclude.save10bsod.typeface = it
            binding.settingsInclude.saveallbsods.typeface = it
            binding.settingsInclude.showBsodInfo.typeface = it
            binding.settingsInclude.deleteBsodInfo.typeface = it
            binding.settingsInclude.sendFeedbackSwitch.typeface = it
            binding.settingsInclude.showErrorDetailsOnBsodSwitch.typeface = it
            binding.settingsInclude.feedbackLabel.typeface = it
            binding.settingsInclude.sendFeedbackLabel.typeface = it
            binding.settingsInclude.bsodDetailsLabel.typeface = it
            binding.settingsInclude.text.typeface = it
            binding.settingsInclude.numOfIssuesLabel.typeface = it
            binding.settingsInclude.setCrashLogLimitBtn.typeface = it

        }
        customBoldFont?.let {
            binding.settingsLabel.typeface = it
        }
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
            isChecked = PREFS.isFeedbackEnabled
            text = if (PREFS.isFeedbackEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.isFeedbackEnabled = isChecked
                text = if (isChecked) getString(R.string.on) else getString(R.string.off)
            }
        }
        binding.settingsInclude.setCrashLogLimitBtn.apply {
            setButtonText(this)
            setOnClickListener {
                binding.settingsInclude.chooseBsodInfoLimit.visibility = View.VISIBLE
                visibility = View.GONE
            }
        }
        binding.settingsInclude.showErrorDetailsOnBsodSwitch.apply {
            isChecked = PREFS.bsodOutputEnabled
            text = if (PREFS.bsodOutputEnabled) getString(R.string.on) else getString(R.string.off)
            setOnCheckedChangeListener { _, isChecked ->
                PREFS.bsodOutputEnabled = isChecked
                text = if (isChecked) getString(R.string.on) else getString(R.string.off)
            }
        }
    }

    private fun updateMaxLogsSize(view: View, value: Int) {
        view.setOnClickListener {
            PREFS.maxCrashLogs = value
            binding.settingsInclude.chooseBsodInfoLimit.visibility = View.GONE
            binding.settingsInclude.setCrashLogLimitBtn.visibility = View.VISIBLE
            setButtonText(binding.settingsInclude.setCrashLogLimitBtn)
        }
    }

    private fun setButtonText(button: MaterialButton) {
        button.text = when (PREFS.maxCrashLogs) {
            0 -> getString(R.string.feedback_limit_0)
            1 -> getString(R.string.feedback_limit_1)
            2 -> getString(R.string.feedback_limit_2)
            3 -> getString(R.string.feedback_limit_3)
            else -> getString(R.string.feedback_limit_0)
        }
    }

    private fun enterAnimation(exit: Boolean) {
        if (!PREFS.isTransitionAnimEnabled) return

        val main = binding.root
        val animatorSet = AnimatorSet().apply {
            playTogether(
                createObjectAnimator(
                    main,
                    "translationX",
                    if (exit) 0f else -300f,
                    if (exit) -300f else 0f
                ),
                createObjectAnimator(
                    main,
                    "rotationY",
                    if (exit) 0f else 90f,
                    if (exit) 90f else 0f
                ),
                createObjectAnimator(main, "alpha", if (exit) 1f else 0f, if (exit) 0f else 1f),
                createObjectAnimator(
                    main,
                    "scaleX",
                    if (exit) 1f else 0.5f,
                    if (exit) 0.5f else 1f
                ),
                createObjectAnimator(main, "scaleY", if (exit) 1f else 0.5f, if (exit) 0.5f else 1f)
            )
            duration = 400
        }
        animatorSet.start()
    }

    private fun createObjectAnimator(
        target: Any,
        property: String,
        startValue: Float,
        endValue: Float
    ): ObjectAnimator {
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
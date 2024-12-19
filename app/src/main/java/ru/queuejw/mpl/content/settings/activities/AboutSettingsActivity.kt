package ru.queuejw.mpl.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import ru.queuejw.mpl.Application.Companion.PREFS
import ru.queuejw.mpl.Application.Companion.customBoldFont
import ru.queuejw.mpl.Application.Companion.customFont
import ru.queuejw.mpl.R
import ru.queuejw.mpl.content.settings.Reset
import ru.queuejw.mpl.databinding.LauncherSettingsAboutBinding
import ru.queuejw.mpl.helpers.ui.WPDialog
import ru.queuejw.mpl.helpers.utils.Utils
import kotlin.system.exitProcess

class AboutSettingsActivity : AppCompatActivity() {

    private lateinit var binding: LauncherSettingsAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsAboutBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        Utils.applyWindowInsets(binding.root)
        setupLayout()
        if (PREFS.customFontInstalled) {
            customFont?.let {
                binding.settingsSectionLabel.typeface = it
                binding.settingsInclude.phoneinfoLabel.typeface = it
                binding.settingsInclude.queuejw.typeface = it
                binding.settingsInclude.phoneinfo.typeface = it
                binding.settingsInclude.phoneinfoMore.typeface = it
                binding.settingsInclude.moreInfobtn.typeface = it
                binding.settingsInclude.resetLauncher.typeface = it
                binding.settingsInclude.restartLauncher.typeface = it
                binding.settingsInclude.helpContactsLabel.typeface = it
                binding.settingsInclude.getHelpText.typeface = it
                binding.settingsInclude.emailText.typeface = it
                binding.settingsInclude.onlineContactsText.typeface = it
                binding.settingsInclude.getHelpText.typeface = it
                binding.settingsInclude.githubText.typeface = it
                binding.settingsInclude.telegramText.typeface = it
                binding.settingsInclude.supportText.typeface = it
            }
            customBoldFont?.let {
                binding.settingsLabel.typeface = it
            }
        }
    }

    private fun setupLayout() {
        binding.settingsInclude.phoneinfo.text =
            getString(
                R.string.phone_info,
                "${Utils.MANUFACTURER} ${Utils.PRODUCT}",
                Utils.MODEL,
                Utils.VERSION_NAME
            )
        binding.settingsInclude.moreInfobtn.setOnClickListener {
            binding.settingsInclude.phoneinfoMore.text = getString(
                R.string.phone_moreinfo,
                Utils.VERSION_NAME,
                Utils.VERSION_CODE,
                Utils.DEVICE,
                Utils.BRAND,
                Utils.MODEL,
                Utils.PRODUCT,
                Utils.HARDWARE,
                Utils.BUILD,
                Utils.TIME
            )
            binding.settingsInclude.moreInfobtn.visibility = View.GONE
            binding.settingsInclude.moreinfoLayout.visibility = View.VISIBLE
        }
        binding.settingsInclude.resetLauncher.setOnClickListener {
            WPDialog(this).setTopDialog(true)
                .setTitle(getString(R.string.reset_warning_title))
                .setMessage(getString(R.string.reset_warning))
                .setNegativeButton(getString(R.string.yes)) {
                    resetPart1()
                    WPDialog(this).dismiss()
                }
                .setPositiveButton(getString(R.string.no), null).show()
        }
        binding.settingsInclude.restartLauncher.setOnClickListener {
            exitProcess(0)
        }
    }

    private fun resetPart1() {
        val intent = (Intent(this@AboutSettingsActivity, Reset::class.java))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        finishAffinity()
        startActivity(intent)
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

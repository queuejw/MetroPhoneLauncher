package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.customBoldFont
import ru.dimon6018.metrolauncher.Application.Companion.customFont
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.settings.Reset
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsAboutBinding
import ru.dimon6018.metrolauncher.helpers.ui.WPDialog
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.BRAND
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.BUILD
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.DEVICE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.HARDWARE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.MANUFACTURER
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.MODEL
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.PRODUCT
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.TIME
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_CODE
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.VERSION_NAME
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import kotlin.system.exitProcess

class AboutSettingsActivity : AppCompatActivity() {

    private lateinit var binding: LauncherSettingsAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsAboutBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        applyWindowInsets(binding.root)
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
            getString(R.string.phone_info, "$MANUFACTURER $PRODUCT", MODEL, VERSION_NAME)
        binding.settingsInclude.moreInfobtn.setOnClickListener {
            binding.settingsInclude.phoneinfoMore.text = getString(
                R.string.phone_moreinfo,
                VERSION_NAME,
                VERSION_CODE,
                DEVICE,
                BRAND,
                MODEL,
                PRODUCT,
                HARDWARE,
                BUILD,
                TIME
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

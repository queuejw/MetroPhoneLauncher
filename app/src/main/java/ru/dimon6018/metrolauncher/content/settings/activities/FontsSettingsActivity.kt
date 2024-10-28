package ru.dimon6018.metrolauncher.content.settings.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.customBoldFont
import ru.dimon6018.metrolauncher.Application.Companion.customFont
import ru.dimon6018.metrolauncher.Application.Companion.customLightFont
import ru.dimon6018.metrolauncher.Application.Companion.setupCustomBoldFont
import ru.dimon6018.metrolauncher.Application.Companion.setupCustomFont
import ru.dimon6018.metrolauncher.Application.Companion.setupCustomLightFont
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.databinding.LauncherSettingsFontsBinding
import ru.dimon6018.metrolauncher.helpers.utils.Utils.Companion.applyWindowInsets
import java.io.File

class FontsSettingsActivity: AppCompatActivity() {

    private lateinit var binding: LauncherSettingsFontsBinding

    private lateinit var regularFontPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var lightFontPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var boldFontPickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = LauncherSettingsFontsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyWindowInsets(binding.root)
        regularFontPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                PREFS.customFontInstalled = true
                result.data?.data?.let { uri ->
                    applyFontFromUri(uri, "regular")
                }
            }
        }
        lightFontPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    applyFontFromUri(uri, "light")
                }
            }
        }
        boldFontPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    applyFontFromUri(uri, "bold")
                }
            }
        }
        binding.settingsInclude.chooseFont.setOnClickListener {
            if(!PREFS.customFontInstalled) selectCustomFont("regular") else removeCustomFont("regular")
            PREFS.isPrefsChanged = true
        }
        binding.settingsInclude.chooseLightFont.setOnClickListener {
            if(PREFS.customLightFontPath == null) selectCustomFont("light") else removeCustomFont("light")
            PREFS.isPrefsChanged = true
        }
        binding.settingsInclude.chooseBoldFont.setOnClickListener {
            if(PREFS.customBoldFontPath == null) selectCustomFont("bold") else removeCustomFont("bold")
            PREFS.isPrefsChanged = true
        }
        setupUi()
    }
    private fun applyFontFromUri(uri: Uri, fontType: String) {
        runCatching {
            val file = File(uri.path!!)
            val fileName: String
            when(fontType) {
                "regular" -> {
                    PREFS.customFontName = file.name
                    fileName = "custom_regular." + file.extension
                }
                "light" -> {
                    PREFS.customLightFontName = file.name
                    fileName = "custom_light." + file.extension
                }
                "bold" -> {
                    PREFS.customBoldFontName = file.name
                    fileName = "custom_bold." + file.extension
                }
                else -> {
                    PREFS.customFontName = file.name
                    fileName = "custom_regular." + file.extension
                }
            }
            val fontFile = File(ContextCompat.getDataDir(this), fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                fontFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
            }
            saveFontPath(fontFile.absolutePath, fontType)
            activateNewFont()
        }.getOrElse {
            it.printStackTrace()
        }
    }
    private fun activateNewFont() {
        setupCustomFont()
        setupCustomLightFont()
        setupCustomBoldFont()
        setupUi()
    }
    private fun saveFontPath(path: String, fontType: String) {
        when(fontType) {
            "regular" -> PREFS.customFontPath = path
            "light" -> PREFS.customLightFontPath = path
            "bold" -> PREFS.customBoldFontPath = path
            else -> PREFS.customFontPath = path
        }
    }
    private fun setupUi() {
        binding.settingsInclude.chooseFont.text = getString(if(!PREFS.customFontInstalled) R.string.choose_font else R.string.remove_font)
        binding.settingsInclude.chooseLightFont.text = getString(if(PREFS.customLightFontPath == null) R.string.choose_light_font else R.string.remove_light_font)
        binding.settingsInclude.chooseBoldFont.text = getString(if(PREFS.customBoldFontPath == null) R.string.choose_bold_font else R.string.remove_bold_font)
        binding.settingsInclude.currentFont.visibility = if(!PREFS.customFontInstalled) View.GONE else View.VISIBLE
        binding.settingsInclude.currentLightFont.visibility = if(PREFS.customLightFontPath == null) View.GONE else View.VISIBLE
        binding.settingsInclude.currentBoldFont.visibility = if(PREFS.customBoldFontPath == null) View.GONE else View.VISIBLE
        if(PREFS.customFontInstalled) binding.settingsInclude.currentFont.text = getString(R.string.current_font, PREFS.customFontName)
        if(PREFS.customLightFontPath != null) binding.settingsInclude.currentLightFont.text = getString(R.string.current_light_font, PREFS.customLightFontName)
        if(PREFS.customBoldFontPath != null) binding.settingsInclude.currentBoldFont.text = getString(R.string.current_bold_font, PREFS.customBoldFontName)
        setFont()
    }
    private fun setFont() {
        customFont?.let {
            binding.settingsInclude.currentFont.typeface = it
            binding.settingsInclude.chooseFont.typeface = it
            binding.settingsInclude.testFontText.typeface = it
            binding.settingsInclude.fontsTip.typeface = it
            binding.settingsSectionLabel.typeface = it
            binding.settingsLabel.typeface = it
            binding.settingsInclude.additionalOptions.typeface = it
            binding.settingsInclude.additionalFontsTip.typeface = it
        }
        customLightFont?.let {
            binding.settingsInclude.currentLightFont.typeface = it
            binding.settingsInclude.chooseLightFont.typeface = it
            binding.settingsInclude.testLightFontText.typeface = it
        }
        customBoldFont?.let {
            binding.settingsLabel.typeface = it
            binding.settingsInclude.chooseBoldFont.typeface = it
            binding.settingsInclude.currentBoldFont.typeface = it
            binding.settingsInclude.testBoldFontText.typeface = it
        }
    }
    private fun selectCustomFont(fontType: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "font/*"
        }
        when(fontType) {
            "regular" -> regularFontPickerLauncher.launch(intent)
            "light" -> lightFontPickerLauncher.launch(intent)
            "bold" -> boldFontPickerLauncher.launch(intent)
            else -> regularFontPickerLauncher.launch(intent)
        }
    }
    private fun removeCustomFont(fontType: String) {
        PREFS.apply {
            when(fontType) {
                "regular" -> {
                    customFontPath = null
                    customFontName = null
                    customFontInstalled = false
                    customLightFontPath = null
                    customLightFontName = null
                    customBoldFontPath = null
                    customBoldFontName = null
                }
                "light" -> {
                    customLightFontPath = null
                    customLightFontName = null
                }
                "bold" -> {
                    customBoldFontPath = null
                    customBoldFontName = null
                }
            }
        }
        activateNewFont()
        recreate()
    }
    private fun enterAnimation(exit: Boolean) {
        if (!PREFS.isTransitionAnimEnabled) return
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
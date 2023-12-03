package ru.dimon6018.metrolauncher.content.settings

import android.app.UiModeManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import ru.dimon6018.metrolauncher.Application
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.content.data.Prefs
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class ThemeSettingsActivity : AppCompatActivity() {
    private var themeMenu: MaterialCardView? = null
    private var chooseThemeBtn: MaterialButton? = null
    private var chooseAccentBtn: MaterialButton? = null
    private var chooseBackground: MaterialButton? = null
    private var light: TextView? = null
    private var dark: TextView? = null
    private var accentTip: TextView? = null
    private var backgroundImg: ImageView? = null
    private var removeBackgrd: TextView? = null
    private var context: Context? = null
    private var prefs: Prefs? = null
    private var coord: CoordinatorLayout? = null

    private var pickMedia = registerForActivityResult<PickVisualMediaRequest, Uri>(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            Log.i("PhotoPicker", "Selected URI: $uri")
            prefs!!.setLauncherCustomBackgrdAvailability(true)
            prefs!!.setCustomBackgrdPath(getRealPathFromURI(uri, this))
            val bmp = BitmapFactory.decodeFile(prefs!!.backgroundPath)
            val newBmp = Bitmap.createScaledBitmap(bmp, 148, 148, false)
            bmp.recycle()
            backgroundImg!!.setVisibility(View.VISIBLE)
            backgroundImg!!.setImageBitmap(newBmp)
            removeBackgrd!!.visibility = View.VISIBLE
        } else {
            Log.i("PhotoPicker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Application.getLauncherAccentTheme())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_settings_theme)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        coord = findViewById(R.id.coordinator)
        context = this
        prefs = Prefs(context)
        backgroundImg = findViewById(R.id.Startbackground)
        removeBackgrd = findViewById(R.id.removeBackground)
        chooseThemeBtn = findViewById(R.id.chooseTheme)
        accentTip = findViewById(R.id.accentTip)
        chooseBackground = findViewById(R.id.chooseBackground)
        chooseAccentBtn = findViewById(R.id.chooseAccent)
        themeMenu = findViewById(R.id.chooseThemeMenu)
        light = findViewById(R.id.chooseLight)
        dark = findViewById(R.id.chooseDark)
        chooseAccentBtn!!.text = Application.getAccentName()
        val themeButtonLabel: String = if (prefs!!.isLightThemeUsed) {
            "light"
        } else {
            "dark"
        }
        chooseThemeBtn!!.text = themeButtonLabel
        chooseThemeBtn!!.setOnClickListener {
            chooseThemeBtn!!.visibility = View.GONE
            themeMenu!!.visibility = View.VISIBLE
        }
        light!!.setOnClickListener {
            prefs!!.useLightTheme(true)
            restoreThemeButtonsAndApplyChanges()
        }
        dark!!.setOnClickListener {
            prefs!!.useLightTheme(false)
            restoreThemeButtonsAndApplyChanges()
        }
        chooseAccentBtn!!.setOnClickListener { AccentDialog.display(supportFragmentManager) }
        chooseBackground!!.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest.Builder()
                    .setMediaType(ImageOnly)
                    .build())
        }
        removeBackgrd!!.setOnClickListener {
            prefs!!.setLauncherCustomBackgrdAvailability(false)
            prefs!!.setCustomBackgrdPath("")
            backgroundImg!!.setBackgroundColor(getColor(android.R.color.darker_gray))
            backgroundImg!!.setVisibility(View.GONE)
            removeBackgrd!!.visibility = View.GONE
        }
        try {
            if (prefs!!.isCustomBackgroundUsed) {
                val bmp = BitmapFactory.decodeFile(prefs!!.backgroundPath)
                val newBmp = Bitmap.createScaledBitmap(bmp, 148, 148, false)
                bmp.recycle()
                backgroundImg!!.setVisibility(View.VISIBLE)
                backgroundImg!!.setImageBitmap(newBmp)
            } else {
                removeBackgrd!!.visibility = View.GONE
                backgroundImg!!.setVisibility(View.GONE)
            }
        } catch (ex: Exception) {
            Log.e("ThemeSettings", "Exception. See: $ex")
        }
        ViewCompat.setOnApplyWindowInsetsListener(coord!!) { v: View, insets: WindowInsetsCompat ->
            val pB = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val tB = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, tB, 0, pB)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun restoreThemeButtonsAndApplyChanges() {
        chooseThemeBtn!!.visibility = View.VISIBLE
        themeMenu!!.visibility = View.GONE
        setAppTheme()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        window.decorView.findViewById<View>(android.R.id.content).startAnimation(AnimationUtils.loadAnimation(this, R.anim.back_flip_anim))
        finish()
    }

    private fun setAppTheme() {
        val uimanager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if (Prefs(this).isLightThemeUsed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                uimanager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    class AccentDialog : DialogFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
        }

        override fun onStart() {
            super.onStart()
            val dialog = dialog
            if (dialog != null) {
                val width = ViewGroup.LayoutParams.MATCH_PARENT
                val height = ViewGroup.LayoutParams.MATCH_PARENT
                dialog.setTitle("ACCENT")
                dialog.window!!.setLayout(width, height)
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            return inflater.inflate(R.layout.accent_dialog, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val prefs = Prefs(context)
            val lime = view.findViewById<ImageView>(R.id.choose_color_lime)
            val back = view.findViewById<FrameLayout>(R.id.back_accent_menu)
            back.setOnClickListener { dismiss() }
            lime.setOnClickListener {
                prefs.setAccentColor(0)
                dismiss()
                requireActivity().recreate()
            }
            val green = view.findViewById<ImageView>(R.id.choose_color_green)
            green.setOnClickListener {
                prefs.setAccentColor(1)
                dismiss()
                requireActivity().recreate()
            }
            val emerald = view.findViewById<ImageView>(R.id.choose_color_emerald)
            emerald.setOnClickListener {
                prefs.setAccentColor(2)
                dismiss()
                requireActivity().recreate()
            }
            val cyan = view.findViewById<ImageView>(R.id.choose_color_cyan)
            cyan.setOnClickListener {
                prefs.setAccentColor(3)
                dismiss()
                requireActivity().recreate()
            }
            val teal = view.findViewById<ImageView>(R.id.choose_color_teal)
            teal.setOnClickListener {
                prefs.setAccentColor(4)
                dismiss()
                requireActivity().recreate()
            }
            val cobalt = view.findViewById<ImageView>(R.id.choose_color_cobalt)
            cobalt.setOnClickListener {
                prefs.setAccentColor(5)
                dismiss()
                requireActivity().recreate()
            }
            val indigo = view.findViewById<ImageView>(R.id.choose_color_indigo)
            indigo.setOnClickListener {
                prefs.setAccentColor(6)
                dismiss()
                requireActivity().recreate()
            }
            val violet = view.findViewById<ImageView>(R.id.choose_color_violet)
            violet.setOnClickListener {
                prefs.setAccentColor(7)
                dismiss()
                requireActivity().recreate()
            }
            val pink = view.findViewById<ImageView>(R.id.choose_color_pink)
            pink.setOnClickListener {
                prefs.setAccentColor(8)
                dismiss()
                requireActivity().recreate()
            }
            val magenta = view.findViewById<ImageView>(R.id.choose_color_magenta)
            magenta.setOnClickListener {
                prefs.setAccentColor(9)
                dismiss()
                requireActivity().recreate()
            }
            val crimson = view.findViewById<ImageView>(R.id.choose_color_crimson)
            crimson.setOnClickListener {
                prefs.setAccentColor(10)
                dismiss()
                requireActivity().recreate()
            }
            val red = view.findViewById<ImageView>(R.id.choose_color_red)
            red.setOnClickListener {
                prefs.setAccentColor(11)
                dismiss()
                requireActivity().recreate()
            }
            val orange = view.findViewById<ImageView>(R.id.choose_color_orange)
            orange.setOnClickListener {
                prefs.setAccentColor(12)
                dismiss()
                requireActivity().recreate()
            }
            val amber = view.findViewById<ImageView>(R.id.choose_color_amber)
            amber.setOnClickListener {
                prefs.setAccentColor(13)
                dismiss()
                requireActivity().recreate()
            }
            val yellow = view.findViewById<ImageView>(R.id.choose_color_yellow)
            yellow.setOnClickListener {
                prefs.setAccentColor(14)
                dismiss()
                requireActivity().recreate()
            }
            val brown = view.findViewById<ImageView>(R.id.choose_color_brown)
            brown.setOnClickListener {
                prefs.setAccentColor(15)
                dismiss()
                requireActivity().recreate()
            }
            val olive = view.findViewById<ImageView>(R.id.choose_color_olive)
            olive.setOnClickListener {
                prefs.setAccentColor(16)
                dismiss()
                requireActivity().recreate()
            }
            val steel = view.findViewById<ImageView>(R.id.choose_color_steel)
            steel.setOnClickListener {
                prefs.setAccentColor(17)
                dismiss()
                requireActivity().recreate()
            }
            val mauve = view.findViewById<ImageView>(R.id.choose_color_mauve)
            mauve.setOnClickListener {
                prefs.setAccentColor(18)
                dismiss()
                requireActivity().recreate()
            }
            val taupe = view.findViewById<ImageView>(R.id.choose_color_taupe)
            taupe.setOnClickListener {
                prefs.setAccentColor(19)
                dismiss()
                requireActivity().recreate()
            }
        }

        companion object {
            private const val TAG = "accentD"
            fun display(fragmentManager: FragmentManager?): AccentDialog {
                val accentDialog = AccentDialog()
                accentDialog.show(fragmentManager!!, TAG)
                return accentDialog
            }
        }
    }

    companion object {
        //Thanks https://stackoverflow.com/a/72444629
        private fun getRealPathFromURI(uri: Uri, context: Context): String {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            val file = File(context.filesDir, name)
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read: Int
                val maxBufferSize = 1024 * 1024
                val bytesAvailable = inputStream!!.available()

                val bufferSize = min(bytesAvailable.toDouble(), maxBufferSize.toDouble()).toInt()
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream.close()
                outputStream.close()
            } catch (e: Exception) {
                Log.e("ParseURI", "Exception. See: $e")
            }
            returnCursor.close()
            Log.e("ParseURI", "Result: " + file.path)
            return file.path
        }
    }
}

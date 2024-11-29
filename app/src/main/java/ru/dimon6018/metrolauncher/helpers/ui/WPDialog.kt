package ru.dimon6018.metrolauncher.helpers.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.Application.Companion.customFont
import ru.dimon6018.metrolauncher.R
import ru.dimon6018.metrolauncher.databinding.WpdialogLayoutBinding

/*
*	Peter开发
*	Developed by Peter.
*/
class WPDialog(private val mContext: Context) {
    private var wp: Dialog? = null
    private val isDarkMode = mContext.resources.getBoolean(R.bool.isDark) && PREFS.appTheme != 2
    private var defStyle: Int
    private var title = ""
    private var messageText = ""
    private var okText = ""
    private var neutral = ""
    private var cancleText = ""
    private var cancelable = true
    private var onTop = false
    private var light = false
    private var mView: View? = null

    //监听器
    private var okButtonListener: View.OnClickListener? = null
    private var cancelButtonListener: View.OnClickListener? = null
    private var neutralListener: View.OnClickListener? = null
    private var dismissListener: DialogInterface.OnDismissListener? = null

    //Show方法
    fun show(): WPDialog {
        if (!isDarkMode) {
            setLightTheme()
        }
        Builder()
        wp?.apply {
            dismiss()
            show()
        }
        return this
    }

    //SnackBar消失方法
    fun dismiss(): WPDialog {
        wp?.dismiss()
        return this
    }

    init {
        defStyle = R.style.CustomDialog
    }

    //创建
    private inner class Builder {

        private lateinit var binding: WpdialogLayoutBinding

        init {
            if (onTop) {
                defStyle = R.style.CustomDialogTop
            }
            wp = Dialog(mContext, defStyle)
            wp?.apply {
                binding = WpdialogLayoutBinding.inflate(this.layoutInflater)
                if (this.window != null) {
                    this.window!!.setFlags(
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                    )
                }
                this.setContentView(binding.root)
                val dialogWindow = this.window
                val d = mContext.resources.displayMetrics
                val p = dialogWindow!!.attributes
                val gravity: Int = if (!onTop) {
                    80
                } else {
                    48
                }
                dialogWindow.setGravity(Gravity.CENTER or gravity)
                p.width = (d.widthPixels * 1.0).toInt()
                dialogWindow.attributes = p
                setCustomView(this)
                this.setCanceledOnTouchOutside(cancelable)
                this.setCancelable(cancelable)
            }
        }

        //设置布局
        private fun setCustomView(dialog: Dialog) {
            //控件实例化
            dialog.apply {
                if (light) {
                    val wpBg = ContextCompat.getColor(mContext, R.color.wp_bg)
                    binding.wpBg.setBackgroundResource(R.color.wp_light_bg)
                    binding.wpOkBg.setBackgroundResource(R.color.wp_bg)
                    binding.wpNeutralBg.setBackgroundResource(R.color.wp_bg)
                    binding.wpCancleBg.setBackgroundResource(R.color.wp_bg)
                    binding.wpOk.setBackgroundResource(R.drawable.dialog_background_light)
                    binding.wpNeutral.setBackgroundResource(R.drawable.dialog_background_light)
                    binding.wpCancel.setBackgroundResource(R.drawable.dialog_background_light)
                    binding.wpTitle.setTextColor(wpBg)
                    binding.wpMessage.setTextColor(wpBg)
                    binding.wpOk.setTextColor(wpBg)
                    binding.wpNeutral.setTextColor(wpBg)
                    binding.wpCancel.setTextColor(wpBg)
                }
                mView?.let { view ->
                    view.setPadding(10, 0, 10, 20)
                    binding.wpView.addView(view)
                }
                if (title.isEmpty()) binding.wpTitle.visibility =
                    View.GONE else binding.wpTitle.text = title
                if (messageText.isEmpty()) binding.wpMessage.visibility =
                    View.GONE else binding.wpMessage.text = messageText
                if (okText.isEmpty()) binding.wpOkBg.visibility = View.GONE else binding.wpOk.text =
                    okText
                if (cancleText.isEmpty()) binding.wpCancleBg.visibility =
                    View.GONE else binding.wpCancel.text = cancleText
                if (neutral.isEmpty()) binding.wpNeutralBg.visibility =
                    View.GONE else binding.wpNeutral.text = neutral
                binding.wpOk.setOnClickListener(if (okButtonListener != null) okButtonListener else OnDialogButtonClickListener())
                binding.wpCancel.setOnClickListener(if (cancelButtonListener != null) cancelButtonListener else OnDialogButtonClickListener())
                binding.wpNeutral.setOnClickListener(if (neutralListener != null) neutralListener else OnDialogButtonClickListener())
                if (dismissListener != null) setOnDismissListener(dismissListener)
                if (PREFS.customFontInstalled) {
                    customFont?.let { font ->
                        binding.wpTitle.typeface = font
                        binding.wpMessage.typeface = font
                        binding.wpOk.typeface = font
                        binding.wpNeutral.typeface = font
                        binding.wpCancel.typeface = font
                    }
                }

            }
        }
    }

    fun setTitle(text: String): WPDialog {
        title = text
        return this
    }

    fun setMessage(text: String): WPDialog {
        messageText = text
        return this
    }

    /**fun setView(view: View?): WPDialog {
    mView = view
    return this
    } **/
    fun setDismissListener(listener: DialogInterface.OnDismissListener?): WPDialog {
        dismissListener = listener
        return this
    }

    fun setPositiveButton(text: String, listener: View.OnClickListener?): WPDialog {
        okText = text
        okButtonListener = listener
        return this
    }

    fun setNegativeButton(text: String, listener: View.OnClickListener?): WPDialog {
        cancleText = text
        cancelButtonListener = listener
        return this
    }

    fun setNeutralButton(text: String, listener: View.OnClickListener?): WPDialog {
        neutral = text
        neutralListener = listener
        return this
    }

    fun setCancelable(cancelable: Boolean): WPDialog {
        this.cancelable = cancelable
        wp?.setCanceledOnTouchOutside(this.cancelable)
        return this
    }

    fun setTopDialog(top: Boolean): WPDialog {
        onTop = top
        return this
    }

    private fun setLightTheme(): WPDialog {
        light = true
        return this
    }

    private inner class OnDialogButtonClickListener : View.OnClickListener {
        override fun onClick(p1: View) {
            wp?.dismiss()
        }
    }
}
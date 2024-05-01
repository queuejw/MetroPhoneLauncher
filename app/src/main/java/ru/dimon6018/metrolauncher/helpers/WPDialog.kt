package ru.dimon6018.metrolauncher.helpers

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import ru.dimon6018.metrolauncher.Application.Companion.PREFS
import ru.dimon6018.metrolauncher.R

/*
*	Peter开发
*	Developed by Peter.
*/
class WPDialog(private val mContext: Context) {
    private var wp: Dialog? = null
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
    //Show方法
    fun show(): WPDialog {
        if(PREFS!!.isLightThemeUsed) {
            setLightTheme()
        }
        Builder()
        if (wp?.isShowing == false) {
            wp?.show()
        } else {
            wp?.dismiss()
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
        init {
            if (onTop) {
                defStyle = R.style.CustomDialogTop
            }
            wp = Dialog(mContext, defStyle)
            if (wp!!.window != null) {
                wp!!.window!!.setFlags(
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            }
            wp!!.setContentView(R.layout.wpdialog_layout)
            val dialogWindow = wp!!.window
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
            setCustomView(wp!!)
            if (wp != null) {
                wp!!.setCanceledOnTouchOutside(cancelable)
                wp!!.setCancelable(cancelable)
            }
        }
        //设置布局
        private fun setCustomView(dialog: Dialog) {
            //控件实例化
            val wpTitle = dialog.findViewById<TextView>(R.id.wp_title)
            val wpMessage = dialog.findViewById<TextView>(R.id.wp_message)
            val wpOk = dialog.findViewById<TextView>(R.id.wp_ok)
            val wpNeutral = dialog.findViewById<TextView>(R.id.wp_neutral)
            val wpCancel = dialog.findViewById<TextView>(R.id.wp_cancel)

            //布局
            val layout = dialog.findViewById<LinearLayout>(R.id.wp_bg)
            val okLayout = dialog.findViewById<LinearLayout>(R.id.wp_ok_bg)
            val neutralLayout = dialog.findViewById<LinearLayout>(R.id.wp_neutral_bg)
            val cancleLayout = dialog.findViewById<LinearLayout>(R.id.wp_cancle_bg)
            val frame = dialog.findViewById<FrameLayout>(R.id.wp_view)
            if (light) {
                val wpBg = ContextCompat.getColor(mContext, R.color.wp_bg)
                layout.setBackgroundResource(R.color.wp_light_bg)
                okLayout.setBackgroundResource(R.color.wp_bg)
                neutralLayout.setBackgroundResource(R.color.wp_bg)
                cancleLayout.setBackgroundResource(R.color.wp_bg)
                wpOk.setBackgroundResource(R.drawable.dialog_background_light)
                wpNeutral.setBackgroundResource(R.drawable.dialog_background_light)
                wpCancel.setBackgroundResource(R.drawable.dialog_background_light)
                wpTitle.setTextColor(wpBg)
                wpMessage.setTextColor(wpBg)
                wpOk.setTextColor(wpBg)
                wpNeutral.setTextColor(wpBg)
                wpCancel.setTextColor(wpBg)
            }
            if (mView != null) {
                mView!!.setPadding(10, 0, 10, 20)
                frame.addView(mView)
            }
            if (title.isEmpty()) {
                wpTitle.visibility = View.GONE
            } else {
                wpTitle.text = title
            }
            if (messageText.isEmpty()) {
                wpMessage.visibility = View.GONE
            } else {
                wpMessage.text = messageText
            }
            if (okText.isEmpty()) {
                okLayout.visibility = View.GONE
            } else {
                wpOk.text = okText
            }
            if (cancleText.isEmpty()) {
                cancleLayout.visibility = View.GONE
            } else {
                wpCancel.text = cancleText
            }
            if (neutral.isEmpty()) {
                neutralLayout.visibility = View.GONE
            } else {
                wpNeutral.text = neutral
            }
            if (okButtonListener != null) {
                wpOk.setOnClickListener(okButtonListener)
            } else {
                wpOk.setOnClickListener(OnDialogButtonClickListener())
            }
            if (cancelButtonListener != null) {
                wpCancel.setOnClickListener(cancelButtonListener)
            } else {
                wpCancel.setOnClickListener(OnDialogButtonClickListener())
            }
            //
            if (neutralListener != null) {
                wpNeutral.setOnClickListener(neutralListener)
            } else {
                wpNeutral.setOnClickListener(OnDialogButtonClickListener())
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
    fun setView(view: View?): WPDialog {
        mView = view
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
        if (wp != null) {
            wp!!.setCanceledOnTouchOutside(this.cancelable)
        }
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
            wp!!.dismiss()
        }
    }
}
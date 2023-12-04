package ru.dimon6018.metrolauncher.helpers;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.dimon6018.metrolauncher.R;

/*
 *	Peter开发
 *	Developed by Peter.
 */
public class WPDialog
{
    private Dialog wp;
    private final Context mContext;
    private int defStyle;

    private String title="";
    private String messageText="";
    private String okText="";
    private String neutral="";
    private String cancleText="";

    private boolean Cancelable=true,onTop=false,Light=false;

    private WPDialog.Builder mBuilder;

    private View mView=null;

    //监听器
    private View.OnClickListener okButtonListener;
    private View.OnClickListener cancelButtonListener;
    private View.OnClickListener NeutralListener;

    private DialogInterface.OnDismissListener dismissListener;

    //Show方法
    public WPDialog show(){
        mBuilder=new Builder();
        if(!wp.isShowing()){
            wp.show();
        }
        else{
            wp.dismiss();
        }
        return this;
    }
    //SnackBar消失方法
    public WPDialog dismiss(){
        wp.dismiss();
        return this;
    }

    public WPDialog(Context context){
        this.mContext=context;
        this.defStyle= R.style.CustomDialog;
    }
    //创建
    private class Builder {
        //布局
        private LinearLayout layout,okLayout,neutralLayout,cancleLayout;
        private TextView wp_title,wp_ok,wp_neutral,wp_cancel;
        private TextView wp_message;
        private FrameLayout frame;
        private Builder() {
            if(onTop){
                defStyle=R.style.CustomDialogTop;
            }
            wp=new Dialog(mContext,defStyle);
            wp.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            wp.setContentView(R.layout.wpdialog_layout);
            Window dialogWindow = wp.getWindow();
            DisplayMetrics d = mContext.getResources().getDisplayMetrics();
            WindowManager.LayoutParams p = dialogWindow.getAttributes();
            int gravity;
            if(!onTop){
                gravity = 80;
            }
            else{
                gravity = 48;
            }
            dialogWindow.setGravity(Gravity.CENTER| gravity);
            p.width =(int)(d.widthPixels*1.0);
            dialogWindow.setAttributes(p);
            setCustomView(wp);

            if (wp != null) {
                wp.setCanceledOnTouchOutside(Cancelable);
                wp.setCancelable(Cancelable);
            }

        }
        //设置布局
        private void setCustomView(Dialog dialog){
            //控件实例化
            wp_title= dialog.findViewById(R.id.wp_title);
            wp_message= dialog.findViewById(R.id.wp_message);
            wp_ok= dialog.findViewById(R.id.wp_ok);
            wp_neutral= dialog.findViewById(R.id.wp_neutral);
            wp_cancel=dialog.findViewById(R.id.wp_cancel);

            layout=dialog.findViewById(R.id.wp_bg);
            okLayout=dialog.findViewById(R.id.wp_ok_bg);
            neutralLayout=dialog.findViewById(R.id.wp_neutral_bg);
            cancleLayout=dialog.findViewById(R.id.wp_cancle_bg);

            frame=dialog.findViewById(R.id.wp_view);

            if(Light){
                layout.setBackgroundResource(R.color.wp_light_bg);
                okLayout.setBackgroundResource(R.color.wp_bg);
                neutralLayout.setBackgroundResource(R.color.wp_bg);
                cancleLayout.setBackgroundResource(R.color.wp_bg);

                wp_ok.setBackgroundResource(R.drawable.background_light);
                wp_neutral.setBackgroundResource(R.drawable.background_light);
                wp_cancel.setBackgroundResource(R.drawable.background_light);

                wp_title.setTextColor(mContext.getResources().getColor(R.color.wp_bg));
                wp_message.setTextColor(mContext.getResources().getColor(R.color.wp_bg));
                wp_ok.setTextColor(mContext.getResources().getColor(R.color.wp_bg));
                wp_neutral.setTextColor(mContext.getResources().getColor(R.color.wp_bg));
                wp_cancel.setTextColor(mContext.getResources().getColor(R.color.wp_bg));
            }

            if(mView!=null){
                mView.setPadding(10,0,10,20);
                frame.addView(mView);
            }
            if(title.isEmpty()){
                wp_title.setVisibility(View.GONE);
            }
            else{
                wp_title.setText(title);
            }
            if(messageText.isEmpty()){
                wp_message.setVisibility(View.GONE);
            }
            else{
                wp_message.setText(messageText);
            }

            if(okText.isEmpty()){
                okLayout.setVisibility(View.GONE);
            }
            else {
                wp_ok.setText(okText);
            }

            if(cancleText.isEmpty()){
                cancleLayout.setVisibility(View.GONE);
            }
            else{
                wp_cancel.setText(cancleText);
            }

            if(neutral.isEmpty()){
                neutralLayout.setVisibility(View.GONE);
            }
            else{
                wp_neutral.setText(neutral);
            }
            //
            if(okButtonListener!=null){
                wp_ok.setOnClickListener(okButtonListener);
            }
            else{
                wp_ok.setOnClickListener(new OnDialogButtonClickListener());
            }
            //
            if(cancelButtonListener!=null){
                wp_cancel.setOnClickListener(cancelButtonListener);
            }
            else{
                wp_cancel.setOnClickListener(new OnDialogButtonClickListener());
            }
            //
            if(NeutralListener!=null){
                wp_neutral.setOnClickListener(NeutralListener);
            }
            else{
                wp_neutral.setOnClickListener(new OnDialogButtonClickListener());
            }

        }
    }
    public WPDialog setTitle(String text){
        this.title=text;
        return this;
    }
    public WPDialog setMessage(String text){
        this.messageText=text;
        return this;
    }
    public WPDialog setView(View view){
        this.mView=view;
        return this;
    }
    public WPDialog setPositiveButton(String text, final View.OnClickListener listener) {
        this.okText=text;
        this.okButtonListener=listener;
        return this;
    }
    public WPDialog setNegativeButton(String text, final View.OnClickListener listener) {
        this.cancleText = text;
        this.cancelButtonListener = listener;
        return this;
    }
    public WPDialog setNeutralButton(String text,final View.OnClickListener listener){
        this.neutral=text;
        this.NeutralListener=listener;
        return this;
    }
    public WPDialog setOnDismissListener(DialogInterface.OnDismissListener listener){
        this.dismissListener=listener;
        return this;
    }
    public WPDialog setCancelable(boolean cancelable){
        this.Cancelable = cancelable;
        if (wp != null) {
            wp.setCanceledOnTouchOutside(Cancelable);
        }
        return this;
    }
    public WPDialog setTopDialog(boolean top){
        this.onTop=top;
        return this;
    }
    public WPDialog setLightTheme(){
        this.Light=true;
        return this;
    }
    private class OnDialogButtonClickListener implements OnClickListener
    {
        @Override
        public void onClick(View p1)
        {
            wp.dismiss();
        }
    }

}
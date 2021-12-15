package com.jeme.jedis.dialog

import android.content.Context
import android.view.View
import com.jeme.jedis.R
import com.lxj.xpopup.impl.ConfirmPopupView

/**
 * 通用对话框
 * @author jeme
 * @date 2020/4/14
 */
class MsgDialog(context: Context, hideCancel : Boolean, bindingLayoutId : Int) : ConfirmPopupView(context,bindingLayoutId) {

    init {
        isHideCancel = hideCancel
    }
    override fun onCreate() {
        super.onCreate()
        if(isHideCancel){
            findViewById<View>(R.id.v_line)?.visibility = View.GONE
        }
    }
}
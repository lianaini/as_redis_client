package com.jeme.jedis.utils

import android.content.Context
import com.jeme.jedis.R
import com.jeme.jedis.dialog.MsgDialog
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.interfaces.OnCancelListener
import com.lxj.xpopup.interfaces.OnConfirmListener

/**
 * 通用对话框
 * @author jeme
 * @date 2020/2/24
 */
class CommonDialogUtils {
    companion object {

        @JvmStatic
        fun showLoading(context: Context, loadingTip: String?): BasePopupView {
            return XPopup.Builder(context)
//                    .customAnimator(EmptyAnimator())

                    .asLoading(loadingTip, R.layout.dialog_loading_layout)
                    .show()
        }

        @JvmStatic
        fun showMessage(context: Context,
                        title: String? = "",
                        content: String,
                        confirm: String = "确认",
                        cancel: String = "取消",
                        confirmListener: OnConfirmListener,
                        cancelListener: OnCancelListener? = null,
                        isCanCancel: Boolean = true,
                        autoDismiss : Boolean = true): BasePopupView {
            return XPopup.Builder(context)
                    .autoDismiss(autoDismiss)
                    .dismissOnBackPressed(isCanCancel)
                    .dismissOnTouchOutside(isCanCancel)
                    .enableDrag(false)
                    .asConfirm(title, content, cancel, confirm, confirmListener, cancelListener, false,R.layout.dialog_msg_tip_layout)
                    .show()
        }

        @JvmStatic
        fun showTip(context: Context,
                    title: String = "",
                    content: String,
                    confirm: String = "确认",
                    confirmListener: OnConfirmListener,
                    isCanCancel: Boolean = true): BasePopupView {
            return XPopup.Builder(context)
                    .dismissOnBackPressed(isCanCancel)
                    .dismissOnTouchOutside(isCanCancel)
                    .enableDrag(false)
                    .asCustom(asConfirm(context,title, content, null, confirm, confirmListener, null, true))
                    .show()
        }



        private fun asConfirm(context: Context,title: String?, content: String?,
                      cancelBtnText: String?, confirmBtnText: String?,
                      confirmListener: OnConfirmListener?, cancelListener: OnCancelListener?, isHideCancel: Boolean): MsgDialog? {
            val popupView = MsgDialog(context,isHideCancel,R.layout.dialog_msg_tip_layout)
            popupView.setTitleContent(title, content, null as String?)
            popupView.setCancelText(cancelBtnText)
            popupView.setConfirmText(confirmBtnText)
            popupView.setListener(confirmListener, cancelListener)
//            popupView.bindLayout(R.layout.dialog_msg_tip_layout)
//            if (isHideCancel) {
//                popupView.hideCancelBtn()
//            }
            return popupView
        }
    }
}
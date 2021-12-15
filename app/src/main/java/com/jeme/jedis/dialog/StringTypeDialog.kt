package com.jeme.jedis.dialog

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.jeme.jedis.JApplication
import com.jeme.jedis.R
import com.jeme.jedis.databinding.StringTypeBottomDialogBinding
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView

/***
 * @date 2021/12/14
 * @author jeme
 * @description 字符串类型的数据展示
 */
class StringTypeDialog(context: Context,val content : String) : BottomPopupView(context) {
    companion object {
        fun show(context: Context,content : String) : StringTypeDialog{
            return XPopup.Builder(context)
                .asCustom(StringTypeDialog(context,content))
                .show() as StringTypeDialog
        }
    }
    private lateinit var binding : StringTypeBottomDialogBinding

    override fun getImplLayoutId(): Int {
        return R.layout.string_type_bottom_dialog
    }

    override fun onCreate() {
        super.onCreate()
        binding = DataBindingUtil.bind(popupImplView)!!
        binding.content.text = content
    }

    override fun getMaxHeight(): Int {
        return (getScreenHeight() * 0.55f).toInt()
    }

    override fun getPopupHeight(): Int {
        return getScreenHeight()
    }
    private fun getScreenHeight(): Int {
        val wm = JApplication.app
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
            ?: return -1
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.defaultDisplay.getRealSize(point)
        } else {
            wm.defaultDisplay.getSize(point)
        }
        return point.y
    }
}
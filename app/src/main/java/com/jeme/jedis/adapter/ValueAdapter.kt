package com.jeme.jedis.adapter

import android.content.Context
import android.view.View
import com.jeme.jedis.R
import com.jeme.jedis.ValueTypeEnum
import com.jeme.jedis.bean.ValueBean
import com.jeme.jedis.databinding.AdapterItemValueLayoutBinding
import com.jeme.jedis.dialog.StringTypeDialog

/***
 * @date 2021/12/14
 * @author jeme
 * @description
 */
class ValueAdapter(val context: Context, val viewType: ValueTypeEnum) :
    AbstractBindingAdapter<AdapterItemValueLayoutBinding, ValueBean>(context) {
    override fun getLayoutId(viewType: Int): Int {
        return R.layout.adapter_item_value_layout
    }

    override fun getItemViewType(position: Int): Int {
        return when (viewType) {
            ValueTypeEnum.hash -> 0
            ValueTypeEnum.set -> 1
            else -> 0
        }
    }

    override fun onBindingView(
        binding: AdapterItemValueLayoutBinding,
        item: ValueBean?,
        position: Int
    ) {
        if (getItemViewType(position) == 0) {
            //key-value
            binding.tvKey.visibility = View.VISIBLE
            binding.tvKey.text = item?.key
            binding.tvKey.tag = item?.key

            if (!binding.tvKey.hasOnClickListeners()) {
                binding.tvKey.setOnClickListener {
                    StringTypeDialog.show(context, it.tag as String)
                }
            }
        } else {
            //value
            binding.tvKey.visibility = View.GONE
            binding.tvKey.tag = null
        }
        binding.tvValue.text = item?.value

        binding.tvValue.tag = item?.value
        if (!binding.tvValue.hasOnClickListeners()) {
            binding.tvValue.setOnClickListener {
                StringTypeDialog.show(context, it.tag as String)
            }
        }
    }
}
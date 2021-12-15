package com.jeme.jedis.adapter

import android.content.Context
import com.jeme.jedis.R
import com.jeme.jedis.databinding.AdapterItemCatHistoryLayoutBinding

/***
 * @date 2021/12/14
 * @author jeme
 * @description 查询的历史记录
 */
class CatHistoryAdapter(val context: Context):AbstractBindingAdapter<AdapterItemCatHistoryLayoutBinding,String>(context) {
    override fun getLayoutId(viewType: Int): Int {
        return R.layout.adapter_item_cat_history_layout
    }

    override fun onBindingView(
        binding: AdapterItemCatHistoryLayoutBinding,
        item: String?,
        position: Int
    ) {
        binding.tvKey.text = item
    }
}
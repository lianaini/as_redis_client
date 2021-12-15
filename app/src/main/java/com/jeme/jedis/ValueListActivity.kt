package com.jeme.jedis

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeme.jedis.adapter.ValueAdapter
import com.jeme.jedis.bean.ValueBean
import com.jeme.jedis.databinding.ActivityValueListBinding
import com.jeme.jedis.utils.JedisHelper

/***
 * @date 2021/12/14
 * @author jeme
 * @description 查询结果列表
 */
class ValueListActivity : AppCompatActivity() {
    private lateinit var binding : ActivityValueListBinding
    private var valueType : ValueTypeEnum?=null
    private var key : String?=null
    private var valueAdapter : ValueAdapter?=null

    companion object {
        fun launch(activity: Activity,key : String,valueType : ValueTypeEnum) {
            activity.startActivity(Intent(activity,ValueListActivity::class.java).also {
                it.putExtra("key",key)
                it.putExtra("valueType",valueType.name)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        key = intent.getStringExtra("key")
        if(TextUtils.isEmpty(key)) {
            Toast.makeText(this,"请先输入要查询的key",Toast.LENGTH_LONG).show()
            return
        }
        binding = DataBindingUtil.setContentView(this,R.layout.activity_value_list)
        valueType = ValueTypeEnum.valueOf(intent.getStringExtra("valueType")?:ValueTypeEnum.set.name)
        initView()
    }
    private fun initView() {
        val layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        binding.rv.layoutManager = layoutManager

        valueAdapter = ValueAdapter(this,valueType?:ValueTypeEnum.set)
        binding.rv.adapter = valueAdapter

        binding.srl.setOnRefreshListener {
            refreshData()
        }
        refreshData()
    }

    private fun refreshData() {
        val commandType = when(valueType) {
            ValueTypeEnum.hash-> "hgetall"
            ValueTypeEnum.set-> "smembers"
            else ->"get"
        }
        JedisHelper.get().send("$commandType $key",object : JedisHelper.Callback{
            override fun run(valueType: ValueTypeEnum, value: Any?) {
                binding.srl.isRefreshing = false
                if(valueType != ValueTypeEnum.string) {
                    valueAdapter?.data = value as ArrayList<ValueBean>
                }
            }

        })

    }

}
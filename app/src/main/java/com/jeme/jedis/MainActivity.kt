package com.jeme.jedis

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jeme.jedis.adapter.CatHistoryAdapter
import com.jeme.jedis.bean.RedisConnectConfig
import com.jeme.jedis.databinding.ActivityMainBinding
import com.jeme.jedis.db.CatKeyRecordEntity
import com.jeme.jedis.db.JRedisDB
import com.jeme.jedis.dialog.StringTypeDialog
import com.jeme.jedis.utils.CommonDialogUtils
import com.jeme.jedis.utils.JedisHelper
import com.jeme.jedis.utils.SoftKeyUtils
import com.lxj.xpopup.core.BasePopupView

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private var valueAdapter: CatHistoryAdapter? = null

    private val sharedReadable = JApplication.app
        .getSharedPreferences("config", Context.MODE_PRIVATE)

    private var loadingDialog: BasePopupView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initView()
        timeCheckRedis()
        //从数据库获取所有搜索记录
        val all = JRedisDB.getInstance().catKeyRecordDao.queryAll()
        if (all?.size ?: 0 > 0) {
            valueAdapter?.data = MutableList(all.size) { all[it].key }
        }
    }

    private fun initView() {
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rv.layoutManager = layoutManager

        valueAdapter = CatHistoryAdapter(this)
        binding.rv.adapter = valueAdapter
        valueAdapter?.setItemClickListener { _, _, item ->
            binding.etCommand.text = Editable.Factory.getInstance().newEditable(item as String)
            binding.etCommand.setSelection(item.length)
        }

        binding.btn.setOnClickListener {
            processCat()
        }
        binding.tvReconnect.setOnClickListener {
            reConnect()
        }
        binding.tvClear.setOnClickListener {
            binding.etCommand.text = Editable.Factory.getInstance().newEditable("")
        }
    }

    /***
     * 处理查询逻辑
     */
    private fun processCat() {
        SoftKeyUtils.hidKeyboard(this)
        //如果连接断开，提示用户重新创建连接
        if (!JedisHelper.get().isConnected()) {
            CommonDialogUtils.showMessage(
                this,
                "重新连接",
                "Redis已断开，是否需要重新连接？",
                confirmListener = { reConnect() })
            return
        }
        val catKey = binding.etCommand.text?.trim()?.toString()
        if (TextUtils.isEmpty(catKey)) {
            Toast.makeText(this, "请填入相关命令", Toast.LENGTH_LONG).show()
            return
        }
        JedisHelper.get().type(catKey!!) { it ->
            Log.e("jeme====>", "type=${it}")
            if (catKey != valueAdapter?.data?.get(0) || valueAdapter?.data?.size ?: 0 == 0) {
                valueAdapter?.addData(0, catKey)
                JRedisDB.getInstance().catKeyRecordDao.addRecord(CatKeyRecordEntity().also { entity ->
                    entity.key = catKey
                    entity.updateTime = System.currentTimeMillis()
                })
            }
            when (it?.lowercase()) {
                "string" -> {
                    //对话框
                    JedisHelper.get().get(catKey) {
                        StringTypeDialog.show(this, it ?: "")
                    }
                }
                "hash" -> {
                    ValueListActivity.launch(this, catKey, ValueTypeEnum.hash)
                }
                "set" -> {
                    ValueListActivity.launch(this, catKey, ValueTypeEnum.set)
                }
                "disconnect" -> {
                    Toast.makeText(this, "连接已断开", Toast.LENGTH_LONG).show()
                }
                "none" -> {
                    Toast.makeText(this, "该键不存在", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this, "目前只支持string，hash，set这三种类型", Toast.LENGTH_LONG).show()
                }
            }

        }

    }


    /**
     * 循环查询是否连接
     */
    private fun timeCheckRedis() {

        JApplication.mainHandler.postDelayed(object : Runnable {
            override fun run() {
                if (JedisHelper.get().isConnected()) {

                    JApplication.mainHandler.postDelayed(this, 5000)
                    if (binding.tvReconnect.visibility != View.GONE) {
                        binding.tvReconnect.visibility = View.GONE
                    }
                } else {
                    if (binding.tvReconnect.visibility != View.VISIBLE) {
                        binding.tvReconnect.visibility = View.VISIBLE
                    }
                }

            }

        }, 5000)
    }

    private fun reConnect() {
        val redisConfigStr = sharedReadable.getString("redisConfig", null)
        if (redisConfigStr != null) {
            val redisConfig = Gson().fromJson(redisConfigStr, RedisConnectConfig::class.java)
            if (redisConfig == null) {
                Toast.makeText(this, "没有找到相关连接配置，可返回登录页重新配置", Toast.LENGTH_LONG).show()
                return
            }
            binding.tvReconnect.isEnabled = false
            loadingDialog = CommonDialogUtils.showLoading(this, "连接中")
            JedisHelper.get().connect(redisConfig) {
                binding.tvReconnect.isEnabled = true
                loadingDialog?.dismiss()
                if (it) {
                    Log.e("jeme", "连接成功")
                    Toast.makeText(this, "连接成功", Toast.LENGTH_LONG).show()
                    timeCheckRedis()
                    binding.tvReconnect.visibility = View.GONE
                } else {
                    Toast.makeText(this, "连接失败", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    override fun onBackPressed() {
        if (JedisHelper.get().isConnected()) {
            CommonDialogUtils.showMessage(
                this,
                "是否断开连接",
                "离开页面会断开redis连接，是否确定？",
                confirmListener = {
                    JedisHelper.get().disconnect()
                    finish()
                })
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        JedisHelper.get().disconnect()
    }
}
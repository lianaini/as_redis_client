package com.jeme.jedis

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.jeme.jedis.bean.RedisConnectConfig
import com.jeme.jedis.databinding.ActivityLoginBinding
import com.jeme.jedis.thread.WorkTaskManager
import com.jeme.jedis.utils.CommonDialogUtils
import com.jeme.jedis.utils.JedisHelper
import com.jeme.jedis.utils.SoftKeyUtils
import com.jeme.jedis.utils.tryDef
import com.lxj.xpopup.core.BasePopupView

/***
 * @date 2021/11/2
 * @author jeme
 * @description 登录功能
 */
class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

    val sharedReadable = JApplication.app
    .getSharedPreferences("config", Context.MODE_PRIVATE)
    val sharedWritable = sharedReadable.edit()
    private var loadingDialog : BasePopupView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)
        val redisConfigStr = sharedReadable.getString("redisConfig",null)
        if(redisConfigStr != null) {
            val redisConfig = Gson().fromJson(redisConfigStr, RedisConnectConfig::class.java)
            if(redisConfig != null) {
                binding.tvAddressValue.text = Editable.Factory.getInstance().newEditable(redisConfig.redisHost)
                binding.tvPortValue.text = Editable.Factory.getInstance().newEditable(redisConfig.redisPort.toString())
                binding.tvPwdValue.text = Editable.Factory.getInstance().newEditable(redisConfig.redisPassword)
                binding.tvSshHostValue.text = Editable.Factory.getInstance().newEditable(redisConfig.sshHost)
                binding.tvSshPortValue.text = Editable.Factory.getInstance().newEditable(redisConfig.sshPort.toString())
                binding.tvSshUserNameValue.text = Editable.Factory.getInstance().newEditable(redisConfig.sshUserName)
                binding.tvSshPwdValue.text = Editable.Factory.getInstance().newEditable(redisConfig.sshPassword)
                binding.tvSshLabel.isChecked = redisConfig.useSsh
            }
        }
        binding.btnConnect.setOnClickListener {
            SoftKeyUtils.hidKeyboard(this)
            val redisConnectConfig = RedisConnectConfig().also {
                it.redisHost = binding.tvAddressValue.text?.toString()
                it.redisPort = tryDef(6379){binding.tvPortValue.text?.toString()?.toInt()?:6379}
                it.redisPassword = binding.tvPwdValue.text?.toString()
                it.sshHost = binding.tvSshHostValue.text?.toString()
                it.sshPort = tryDef(0){binding.tvSshPortValue.text?.toString()?.toInt()?:0}
                it.sshUserName = binding.tvSshUserNameValue.text?.toString()
                it.sshPassword = binding.tvSshPwdValue.text?.toString()
                it.useSsh = binding.tvSshLabel.isChecked
            }
            saveRedisConfig(redisConnectConfig)
            binding.btnConnect.isEnabled = false
            loadingDialog = CommonDialogUtils.showLoading(this,"连接中")
            JedisHelper.get().connect(redisConnectConfig){
                binding.btnConnect.isEnabled = true
                loadingDialog?.dismiss()
                if(it) {
                    Log.e("jeme","连接成功")
                    startActivity(Intent(this,MainActivity::class.java))
                }else {
                    Toast.makeText(this,"连接失败",Toast.LENGTH_LONG).show()
                }
            }
        }

    }
    private fun saveRedisConfig(config : RedisConnectConfig) {
        WorkTaskManager.getInstance().addWorkEventTask {
            sharedWritable.putString("redisConfig",Gson().toJson(config))
            sharedWritable.apply()
        }
    }

    var number : Int = 0
    fun setBadgeNumber(context : Context,number : Int) {
        try {
            val num = if (number < 0) 0 else number
            val bundle = Bundle()
            bundle.putString("package", context.getPackageName());
            val launchClassName = context.packageManager.getLaunchIntentForPackage(context.packageName)?.component?.className;
            bundle.putString("class", launchClassName);
            bundle.putInt("badgenumber", num);
            context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, bundle);
        } catch ( e : Exception) {
            e.printStackTrace();
        }
    }
}
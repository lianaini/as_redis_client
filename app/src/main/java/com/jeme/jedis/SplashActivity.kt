package com.jeme.jedis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/***
 * @date 2021/11/2
 * @author jeme
 * @description
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }
}
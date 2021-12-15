package com.jeme.jedis

import android.app.Application
import android.os.Handler
import android.os.Looper

/***
 * @date 2021/12/13
 * @author jeme
 * @description
 */
class JApplication : Application() {
    companion object {
        lateinit var app : JApplication
        var mainHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}
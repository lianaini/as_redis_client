package com.jeme.jedis.utils

import android.util.Log
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jeme.jedis.JApplication
import com.jeme.jedis.ValueTypeEnum
import com.jeme.jedis.bean.RedisConnectConfig
import com.jeme.jedis.bean.ValueBean
import com.jeme.jedis.thread.WorkTaskManager
import redis.clients.jedis.*
import redis.clients.jedis.exceptions.JedisConnectionException
import redis.clients.jedis.exceptions.JedisDataException
import java.util.concurrent.ArrayBlockingQueue

/***
 * @date 2021/11/2
 * @author jeme
 * @description redis相关操作功能类
 */
class JedisHelper private constructor() {
    companion object {
        private const val TAG = "JedisHelper"
        private var instance: JedisHelper? = null
            get() {
                if (field == null) {
                    field = JedisHelper()
                }
                return field
            }

        fun get(): JedisHelper {
            return instance!!
        }
    }

    /***
     * 阻塞队列
     */
    var blockCmdQueue: ArrayBlockingQueue<MsgObj>? = null
    var jedis: Jedis? = null
    var sshSession: Session? = null

    /***
     * 判断redis连接是否存在
     */
    fun isConnected(): Boolean {
        return jedis?.isConnected == true
    }

    /***
     * 建立redis连接
     */
    fun connect(config: RedisConnectConfig, callback: (connect: Boolean) -> Unit) {
        if (jedis?.isConnected == true) {
            callback(true)
            return
        }
        WorkTaskManager.getInstance().addWorkEventTask {
            if (config.useSsh) {
                //如果需要ssh，则需要首先创建ssh通道
                sshSession = createSshSession(config)
                if (sshSession?.isConnected != true) {
                    JApplication.mainHandler.post {
                        callback(false)
                        Log.e(TAG, "创建ssh通道失败")
                    }
                    return@addWorkEventTask
                }
            }
            val jedisConfig = JedisPoolConfig()
            //坑，android不支持jmx的功能，必须关闭，否则提示Jmx的一个库找不到
            jedisConfig.jmxEnabled = false
            try {
                val jedisPool = JedisPool(
                    jedisConfig,
                    if (sshSession == null) config.redisHost else config.localHost,
                    if (sshSession == null) config.redisPort else config.localPort,
                    10000,
                    config.redisPassword
                )
                jedis = jedisPool.resource
                jedis?.connect()
                jedis?.select(0)
                JApplication.mainHandler.post {
                    callback(jedis?.isConnected == true)
                }
                if (jedis?.isConnected == true) {
                    createCustomer()
                }
            } catch (e: JedisConnectionException) {
                if (config.useSsh) {
                    sshSession?.delPortForwardingL(config.localPort)
                    sshSession?.disconnect()
                }
                JApplication.mainHandler.post {
                    callback(false)
                    Log.e(TAG, "创建redis失败")
                }
            }
        }
    }

    /***
     * 判断键值类型
     */
    fun type(key: String, callback: (String?) -> Unit) {
        blockCmdQueue?.add(MsgObj("type", key, object : Callback {
            override fun run(valueType: ValueTypeEnum, value: Any?) {
                callback(value as String)
            }

        }))
    }

    /***
     * 获取string类型
     */
    fun get(key: String, callback: (String?) -> Unit) {

        blockCmdQueue?.add(MsgObj("get", key, object : Callback {
            override fun run(valueType: ValueTypeEnum, value: Any?) {
                callback(value as String)
            }
        }))
    }


    /***
     * 可以根据命令行直接执行各项命令
     */
    fun send(commandLine: String, callback: Callback) {

        val cmd = commandLine.split(" ")
        if (cmd.size < 2) {
            return
        }
        blockCmdQueue?.add(MsgObj(cmd[0], cmd[1], callback))


    }

    /***
     * 断开连接，释放资源
     * 在不需要时主动调用
     */
    fun disconnect() {
        blockCmdQueue?.add(MsgObj("close", "", null))
        jedis?.close()
        sshSession?.disconnect()
        jedis = null
        sshSession = null

    }

    /***
     * 创建ssh通道
     */
    private fun createSshSession(config: RedisConnectConfig): Session? {
        val jsch = JSch()
        //设置ssh目标跳板机连接信息
        var session =
            jsch.getSession(config.sshUserName, config.sshHost, config.sshPort)
        session.setPassword(config.sshPassword)
        session.setConfig("StrictHostKeyChecking", "no")
        session.setConfig("userauth.gssapi-with-mic", "no")
        //将session绑定本地机器ip和端口
        session.setPortForwardingL(
            config.localPort,
            config.redisHost,
            config.redisPort
        )
        //开启ssh跳板机连接
        try {
            session.connect(10000)
        } catch (e: JSchException) {
            e.printStackTrace()
            session.delPortForwardingL(config.localPort)
            session = null
        }
        return session
    }

    /***
     * 创建消费者线程
     */
    private fun createCustomer() {
        blockCmdQueue = ArrayBlockingQueue<MsgObj>(5)
        Thread {
            while (jedis?.isConnected == true) {
                val msgObj: MsgObj?
                try {
                    msgObj = blockCmdQueue?.take()
                    if (msgObj?.command == "close") {
                        return@Thread
                    }
                } catch (e: InterruptedException) {
                    return@Thread
                }
                msgObj?.apply {
                    if (!processSendTask(this)) return@Thread
                }

            }
        }.start()
    }

    /***
     * 处理发送命令逻辑
     */
    private fun processSendTask(msgObj: MsgObj): Boolean {
        try {
            val value: Any?
            try {
                value =
                    jedis?.sendCommand({ msgObj.command.encodeToByteArray() }, msgObj.value)
            } catch (e: JedisConnectionException) {
                e.printStackTrace()
                JApplication.mainHandler.post {
                    msgObj.callback?.run(ValueTypeEnum.disconnect, ValueTypeEnum.disconnect.name)
                }
                disconnect()
                return false
            }
            Log.e(TAG, "cmd=${msgObj.command} valueType=${value!!::class.simpleName}")
            if (value is ByteArray) {
                JApplication.mainHandler.post {
                    msgObj.callback?.run(ValueTypeEnum.string, String(value))
                }
            } else if (value is ArrayList<*>) {
                if (msgObj.command == "hgetall") {
                    processHgetall(value, msgObj)
                } else {
                    val result = ArrayList<ValueBean>(value.size)
                    value.forEach {
                        if (it is ByteArray) {
                            result.add(ValueBean("", String(it)))
                        }
                    }
                    JApplication.mainHandler.post {
                        msgObj.callback?.run(ValueTypeEnum.set, result)
                    }
                }

            }

        } catch (e: JedisDataException) {
            e.printStackTrace()
        } catch (e: JedisConnectionException) {
            e.printStackTrace()
            JApplication.mainHandler.post {
                msgObj.callback?.run(ValueTypeEnum.disconnect, "连接异常")
            }
            disconnect()
            return false
        }
        return true
    }

    /***
     * 处理hgetall的返回结果
     */
    private fun processHgetall(value: ArrayList<*>, msgObj: MsgObj) {

        //hgetall 获取的结果集是一维数组，key,value,key,value形式存在
        val result = ArrayList<ValueBean>(value.size / 2)
        var hashKey: String
        var hashValue: String
        for (index in 0 until value.size step 2) {
            if (value[index] is ByteArray) {
                hashKey = String(value[index] as ByteArray)

                hashValue = if (index + 1 < value.size && value[index + 1] is ByteArray) {
                    String(value[index + 1] as ByteArray)
                } else {
                    ""
                }
                result.add(
                    ValueBean(
                        hashKey,
                        hashValue
                    )
                )
            }
        }
        JApplication.mainHandler.post {
            msgObj.callback?.run(ValueTypeEnum.hash, result)
        }
    }


    data class MsgObj(
        var command: String,
        var value: String,
        var callback: Callback?
    )

    interface Callback {
        fun run(valueType: ValueTypeEnum, value: Any?)
    }
}
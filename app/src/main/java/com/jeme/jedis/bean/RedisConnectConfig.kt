package com.jeme.jedis.bean

import java.io.Serializable

/***
 * @date 2021/12/13
 * @author jeme
 * @description 连接redis和ssh的配置参数
 */

data class RedisConnectConfig(

    /***
     * redis主机名
     */
    var redisHost: String? = null,
    /***
     * redis端口
     */
    var redisPort: Int = 6379,
    /***
     * redis密码
     */
    var redisPassword: String? = null,
    /***
     * 固定本机，外部无需修改，用于ssh端口转发
     */
    var localHost: String = "127.0.0.1",
    /***
     * 随意定义，只要该端口未被占用
     * 主要是使用ssh后，端口转发所用
     */
    var localPort : Int = 33333,
    /***
     * 是否使用ssh
     */
    var useSsh : Boolean = true,
    /***
     * 远端登录主机
     */
    var sshHost: String? = null,
    /***
     * 远端ssh端口
     */
    var sshPort: Int = 0,
    /***
     * 登入ssh的用户名
     */
    var sshUserName: String? = null,
    /***
     * 登入ssh的密码
     */
    var sshPassword: String? = null
): Serializable
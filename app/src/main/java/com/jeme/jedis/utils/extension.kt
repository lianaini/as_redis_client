package com.jeme.jedis.utils

fun <T> tryDef(def: T, block: () -> T): T {
    return try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
        def
    }
}
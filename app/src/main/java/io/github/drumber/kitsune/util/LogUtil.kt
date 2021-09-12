package io.github.drumber.kitsune.util

import android.util.Log

inline fun Any.logV(msg: String, t: Throwable? = null) {
    if (t == null)
        Log.v(this::class.java.simpleName, msg)
    else
        Log.v(this::class.java.simpleName, msg, t)
}

inline fun Any.logD(msg: String, t: Throwable? = null) {
    if (t == null)
        Log.d(this::class.java.simpleName, msg)
    else
        Log.d(this::class.java.simpleName, msg, t)
}

inline fun Any.logI(msg: String, t: Throwable? = null) {
    if (t == null)
        Log.i(this::class.java.simpleName, msg)
    else
        Log.i(this::class.java.simpleName, msg, t)
}

inline fun Any.logW(msg: String, t: Throwable? = null) {
    if (t == null)
        Log.w(this::class.java.simpleName, msg)
    else
        Log.w(this::class.java.simpleName, msg, t)
}

inline fun Any.logE(msg: String, t: Throwable? = null) {
    if (t == null)
        Log.e(this::class.java.simpleName, msg)
    else
        Log.e(this::class.java.simpleName, msg, t)
}

inline fun Any.logWTF(msg: String, t: Throwable? = null) {
    if (t == null)
        Log.wtf(this::class.java.simpleName, msg)
    else
        Log.wtf(this::class.java.simpleName, msg, t)
}

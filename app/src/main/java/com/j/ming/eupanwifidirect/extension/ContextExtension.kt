package com.j.ming.eupanwifidirect.extension

import android.app.Activity
import android.content.Context
import android.support.annotation.StringRes
import android.widget.Toast
import com.j.ming.eupanwifidirect.util.NetworkUtils

/**
 * Created by sunny on 18-3-26.
 */

fun Context.toast(info: String, duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, info, duration)
            .show()
}

fun Context.toast(@StringRes stringRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, stringRes, duration)
            .show()
}

fun Context.setWifiEnable(enable: Boolean){
    NetworkUtils.setWifiEnable(this, enable)
}
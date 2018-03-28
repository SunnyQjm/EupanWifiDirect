package com.j.ming.eupanwifidirect.extension

import android.app.Activity
import android.content.Intent
import android.support.annotation.*
import android.widget.Toast
import com.j.ming.eupanwifidirect.util.NetworkUtils

/**
 * Created by sunny on 17-12-29.
 */


fun Activity.jumpTo(cls: Class<*>, intentParam: IntentParam? = null, vararg flags: Int) {
    val intent = Intent(this, cls)
    flags.forEach {
        intent.addFlags(it)
        println("add flags: $it")
    }
    intentParam?.applyParam(intent)
    startActivity(intent)
}

fun Activity.jumpForResult(cls: Class<*>, requestCode: Int, intentParam: IntentParam? = null) {
    val intent = Intent(this, cls)
    intentParam?.applyParam(intent)
    startActivityForResult(intent, requestCode)
}


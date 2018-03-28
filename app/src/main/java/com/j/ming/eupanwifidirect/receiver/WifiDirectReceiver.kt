package com.j.ming.eupanwifidirect.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.*
import android.os.Build
import com.j.ming.eupanwifidirect.manager.WifiDirectManager

/**
 * Created by sunny on 18-3-26.
 */
class WifiDirectReceiver : BroadcastReceiver() {

    /**
     * 写一个便捷的注册方法，动态注册的时候就不用写intentFilter了
     */
    fun registerReceiver(context: Context) {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        context.registerReceiver(this, intentFilter)
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
        /**
         *当Wifi功能打开或关闭的时候系统会发送 WIFI_P2P_STATE_CHANGED_ACTION 广播
         * Tip: 并不是指是否已经成功连上WI-FI
         */
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                //get the state of current device
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
                        WifiP2pManager.WIFI_P2P_STATE_DISABLED)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    //Wifi p2p enable
                    println("wifi enable")
                    WifiDirectManager.updateWifiP2pEnable(true)
                } else {
                    //wifi p2p disEnable
                    println("wifi disEnable")
                    WifiDirectManager.updateWifiP2pEnable(false)
                }
            }
        /**
         * 当前设备的详细信息发生变化的时候，系统会发送 WIFI_P2P_THIS_DEVICE_CHANGED_ACTION 广播
         */
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                val device: WifiP2pDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                WifiDirectManager.updateSelfDeviceInfo(device)
            }

        /**
         * 当可连接的对等节点列表发生改变的时候， 系统会发送 WIFI_P2P_PEERS_CHANGED_ACTION 广播
         * invoke when the list of peers find, register, lost
         */
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                println("list change")
                //api > 18 have this extra info,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    val wifiP2pList: WifiP2pDeviceList = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST)
                    WifiDirectManager.updateWifiP2pDeviceList(wifiP2pList)
                } else { //if the sdk version lower than 18
                    //get WifiP2pDeviceList by call WifiP2pManager.requestPeers to get
                    WifiDirectManager.requestPeers()
                }
            }
        /**
         * 当一个连接建立或断开的时候，系统会发送该广播
         * This action received when the connection setup or dis-setup
         */
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                val wifiP2pInfo = intent.getParcelableExtra<WifiP2pInfo>(WifiP2pManager.EXTRA_WIFI_P2P_INFO)

                WifiDirectManager.updateNetworkInfo(networkInfo)
                        .updateWifiP2pInfo(wifiP2pInfo)
                println("network: $networkInfo")
                println("wifiP2pInfo: $wifiP2pInfo")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    val wifiP2pGroupInfo = intent.getParcelableExtra<WifiP2pGroup>(WifiP2pManager.EXTRA_WIFI_P2P_GROUP)
                    println("group: $wifiP2pGroupInfo")
                    WifiDirectManager.updateWifiP2pGroupInfo(wifiP2pGroupInfo)
                } else {
                    WifiDirectManager.requestGroup()
                }
            }
//        /**
//         * discover state change
//         */
//            WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
//                val discoverState = intent.getParcelableArrayExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE)
//            }
        }
    }
}
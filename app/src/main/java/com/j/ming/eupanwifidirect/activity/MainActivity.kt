package com.j.ming.eupanwifidirect.activity

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.CallSuper
import android.util.Log
import com.j.ming.eupanwifidirect.R
import com.j.ming.eupanwifidirect.extension.toast
import com.j.ming.eupanwifidirect.manager.WifiDirectManager
import com.j.ming.eupanwifidirect.receiver.WifiDirectReceiver
import com.j.ming.socket.client.Client
import com.j.ming.socket.model.DeviceInfo
import com.j.ming.socket.model.TransLocalFile
import com.j.ming.socket.server.Server
import com.j.ming.socket.util.SocketUtil
import com.j.ming.socket.util.doAsync
import kotlinx.android.synthetic.main.activity_main.*
import java.net.InetAddress

class MainActivity : AppCompatActivity() {
    private val wifiDirectReceiver = WifiDirectReceiver()
    private var targetIp: String = ""

    private val TAG = "MainActivity"
    @CallSuper
    override fun onStart() {
        super.onStart()
        wifiDirectReceiver.registerReceiver(this)
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        unregisterReceiver(wifiDirectReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        WifiDirectManager.init(this)
        doAsync {
            Server.startListen(callback = object : SocketUtil.SocketCallback {

                override fun onNewDeviceRegister(file: DeviceInfo) {
                    Log.e(TAG, "targetIP: ${file.ipAddress}")
                    listView.post {
                        toast("新设备注册： $file")
                    }
                    targetIp = file.ipAddress
                    //收到一个设备的注册之后，发现状态被打断，无法被其它设备检索到，所以在此处再次调用discoverPeers
                    //让群主仍然可以被检索到
                    WifiDirectManager.discoverPeers()
                }

                override fun onReceiveSimpleText(inetAddress: InetAddress, message: String) {
                    Log.e(TAG, "receiveMessage: $message")
                    etContent.post {
                        displayContent.text = message
                    }
                }

                override fun onBegin(file: TransLocalFile) {
                }

                override fun onEnd(file: TransLocalFile) {
                }

                override fun onError(file: TransLocalFile?, e: Throwable) {
                    e.printStackTrace()
                }

                override fun onProgress(file: TransLocalFile, progressState: SocketUtil.ProgressState) {
                }

            })
        }
        initView()
    }

    private fun initView() {
        btnSearch.setOnClickListener {
            WifiDirectManager.discoverPeers()
        }

        btnCreateGroup.setOnClickListener {
            WifiDirectManager.createGroup()
        }

        btnRemoveGroup.setOnClickListener {
            WifiDirectManager.removeGroup()
        }

        btnCancelConnect.setOnClickListener {
            WifiDirectManager.cancelConnect()
        }

        //init List
        val mList = mutableListOf<WifiP2pDevice>()
        val mAdapter = MyAdapter(mList)
        listView.adapter = mAdapter
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = mList[position]
            WifiDirectManager.connect(item.deviceAddress)
        }
        WifiDirectManager.setOnWifiDirectStateChangeListener(object : WifiDirectManager.OnWifiDirectStateChangeListener {
            override fun onDeviceListChange(wifiP2pDeviceList: WifiP2pDeviceList) {
                mList.clear()
                mList.addAll(wifiP2pDeviceList.deviceList)
                mAdapter.notifyDataSetChanged()
            }

            override fun onWifiP2pInfoUpdate(wifiP2pInfo: WifiP2pInfo) {
                Log.e("wifiP2pInfo:", wifiP2pInfo.toString())
                if (!wifiP2pInfo.isGroupOwner && wifiP2pInfo.groupFormed) {
                    wifiP2pInfo.groupOwnerAddress?.let {
                        btnCancelConnect.postDelayed({
                            doAsync {
                                Log.e(TAG, "Register to: ${it.hostAddress}")
                                Client.register(it.hostAddress, DeviceInfo("macAddr", "name", "ip"), {
                                    btnCancelConnect.post {
                                        toast("注册：$it")
                                    }
                                })
                            }
                        }, 200)
                    }
                }
            }

            override fun onWifiP2pGroupInfoUpdate(wifiP2pGroup: WifiP2pGroup) {

            }
        })

        btnSend.setOnClickListener {
            //发送消息
            WifiDirectManager.wifiP2pInfo?.let {
                when {
                    it.isGroupOwner -> {
                        if (!targetIp.isEmpty())
                            doAsync {
                                Client.sendText(targetIp, etContent.text.toString())
                            }
                        etContent.setText("")
                    }
                    it.groupOwnerAddress != null -> {
                        doAsync {
                            println("sendTextTo: ${it.groupOwnerAddress.hostAddress}")
                            Client.sendText(it.groupOwnerAddress.hostAddress, etContent.text.toString())
                        }
                        etContent.setText("")
                    }
                    else -> toast("臣妾发不出去")
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Server.stop()
    }
}

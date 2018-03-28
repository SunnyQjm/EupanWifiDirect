package com.j.ming.eupanwifidirect.manager

import android.content.Context
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.*
import android.os.Looper
import kotlin.properties.Delegates

/**
 * Created by sunny on 18-3-26.
 */
object WifiDirectManager {
    private var manager: WifiP2pManager by Delegates.notNull()
    private var channel: WifiP2pManager.Channel by Delegates.notNull()
    /**
     * 本设备的信息
     */
    var selfDeviceInfo: WifiP2pDevice? = null
    /**
     * Wifi p2p是否可用
     */
    var wifiP2pEnable = false

    /**
     * 可用对等节点列表
     */
    var wifiP2pDeviceList: WifiP2pDeviceList? = null

    /**
     * 网络状态信息
     */
    var networkInfo: NetworkInfo? = null

    /**
     * 当前群组的信息
     */
    var wifiP2pGroupInfo: WifiP2pGroup? = null

    var wifiP2pInfo: WifiP2pInfo? = null

    fun init(context: Context): WifiDirectManager {
        //通过获取系统服务的方式获得Manager对象
        manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(context, Looper.getMainLooper()) {
            //初始化操作成功的回调
        }
        return this
    }

    fun updateWifiP2pInfo(wifiP2pInfo: WifiP2pInfo): WifiDirectManager {
        this.wifiP2pInfo = wifiP2pInfo
        onWifiDirectStateChangeListener?.onWifiP2pInfoUpdate(wifiP2pInfo)
        return this
    }
    fun updateWifiP2pGroupInfo(wifiP2pGroup: WifiP2pGroup): WifiDirectManager {
        this.wifiP2pGroupInfo = wifiP2pGroup
        onWifiDirectStateChangeListener?.onWifiP2pGroupInfoUpdate(wifiP2pGroup)
        return this
    }

    fun updateNetworkInfo(networkInfo: NetworkInfo): WifiDirectManager {
        this.networkInfo = networkInfo
        return this
    }

    fun updateWifiP2pDeviceList(wifiP2pDeviceList: WifiP2pDeviceList) {
        this.wifiP2pDeviceList = wifiP2pDeviceList
        onWifiDirectStateChangeListener?.onDeviceListChange(wifiP2pDeviceList)
    }

    fun updateSelfDeviceInfo(deviceInfo: WifiP2pDevice) {
        selfDeviceInfo = deviceInfo
    }

    fun updateWifiP2pEnable(enable: Boolean) {
        wifiP2pEnable = enable
    }
    ////////////////////////////////////////////////////////////////////////////////////
    /////////// WifiDirect API 封装
    ////////////////////////////////////////////////////////////////////////////////////
    /**
     * discover available peer list
     * 调用本接口成功之后：
     * 1. 本设备可以被其它对等节点检测到
     * 2. 未知设备来连接时会询问用户是否连接
     * 3. 已知设备可直接连上
     * 4. 一旦与其它设备建立连接，则本设备的discover状态解除，本设备将不能再被其它设备检测到
     * （如果还需要被检测到，需要再次调用discoverPeers方法）
     */
    fun discoverPeers(): WifiDirectManager {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                println("discover success")
            }

            override fun onFailure(reason: Int) {
                println("discover fail: $reason")
            }
        })
        return this
    }

    /**
     * request the peer list available
     * 调用该接口可以获取本设备已经检测到的对等节点的列表
     */
    fun requestPeers(): WifiDirectManager {
        manager.requestPeers(channel) { peers ->
            //请求对等节点列表操作成功
            WifiDirectManager.updateWifiP2pDeviceList(peers)
        }
        return this
    }

    /**
     * request the group info
     * 调用该接口可以获取本设备所处群组的信息
     */
    fun requestGroup(): WifiDirectManager {
        manager.requestGroupInfo(channel) { group ->
            updateWifiP2pGroupInfo(group)
        }
        return this
    }

    /**
     * create a group
     * 调用本接口成功以后：
     * 1. 本设备会建立一个群组，并自动称为群主
     * 2. 本设备将会处于可以被其它设备检测的状态，并允许其它对等节点的连接
     * 3. 一旦与其它设备建立连接，本设备也将不能被其它的设备检测
     * （如果仍需被其它设备检测，则需要调用discoverPeers接口）
     */
    fun createGroup(): WifiDirectManager {
        manager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onFailure(reason: Int) {
                println("create group fail: $reason")

            }

            override fun onSuccess() {
                println("create group success")
            }
        })
        return this
    }

    /**
     * remove a group
     * 调用本接口可以移除当前群组：
     * 1. 本设备是群主： 则会解散本群，所有已建立的连接断开
     * 2. 本设备是组员： 则会退出当前所属群组
     */
    fun removeGroup(): WifiDirectManager {
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                println("remove group success")
            }

            override fun onFailure(reason: Int) {
                println("remove group success: $reason")
            }

        })
        return this
    }

    /**
     * connect by MAC address(hardware address)
     * 调用本接口尝试连接某个对等节点
     * 1. 如果本设备和对方设备均不是群主，则会建立一个群组，并随机选择一个个设备成为群主
     * 2. 如果两端设备有其中一端为群主，则另一端加入该群
     */
    fun connect(deviceAddress: String) {
        val config = WifiP2pConfig()
        config.deviceAddress = deviceAddress
        config.wps.setup = WpsInfo.PBC
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                println("connect operator success")
            }

            override fun onFailure(reason: Int) {
                println("connect operator fail: $reason")
            }
        })
    }

    /**
     * invoke this method to connect a p2p device
     */
    fun connect(device: WifiP2pDevice): WifiDirectManager {
        connect(device.deviceAddress)
        return this
    }

    /**
     * 取消连接
     * 1. 本接口只有在调用connect之后，连接成功之前调用有效
     * 2. 目的是取消当前正在尝试的连接操作
     */
    fun cancelConnect(): WifiDirectManager {
        manager.cancelConnect(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                println("cancel connect success")
            }

            override fun onFailure(reason: Int) {
                println("cancel connect fail: $reason")
            }
        })
        return this
    }


    //////////////////////////////////////////////////////////////////////////////////
    /////////////   WifiDirectListener
    //////////////////////////////////////////////////////////////////////////////////

    private var onWifiDirectStateChangeListener: OnWifiDirectStateChangeListener? = null

    fun setOnWifiDirectStateChangeListener(onWifiDirectStateChangeListener: OnWifiDirectStateChangeListener){
        this.onWifiDirectStateChangeListener = onWifiDirectStateChangeListener
    }

    interface OnWifiDirectStateChangeListener{

        /**
         * 可以在这个接口的回调里面获取当前可用对等节点列表
         */
        fun onDeviceListChange(wifiP2pDeviceList: WifiP2pDeviceList)

        /**
         * 可以在这个接口的回调里获取到
         * 1. 当前群组是否成功建立                        --> groupFormed
         * 2. 本设备是否是群主                           --> isGroupOwner
         * 3. 以及如果群组已建立的话，群主的IP地址信息是什么    --> groupOwnerAddress
         * groupFormed: true isGroupOwner: true groupOwnerAddress: /192.168.49.1
         */
        fun onWifiP2pInfoUpdate(wifiP2pInfo: WifiP2pInfo)

        /**
         * 可以在这个接口里面获取到当前群组的信息
         * 1. 如果群组没有建立，里面基本没啥有用的信息：
         *      network: null
                isGO: false
                GO: null
                interface: null
                networkId: 0
         * 2. 如果群组已经建立，这里面包含所有组员的信息和群组的信息（新成员的加入和成员的退出都会导致群组信息的更新，本回调就会被调用）
         *      network: DIRECT-BF-\xe9\xad\x85\xe8\x93\x9d Note3
                isGO: true
                GO: Device:
                deviceAddress: 2e:57:31:98:45:34
                primary type: null
                secondary type: null
                wps: 0
                grpcapab: 0
                devcapab: 0
                status: 4
                wfdInfo: WFD enabled: falseWFD DeviceInfo: 0
                WFD CtrlPort: 0
                WFD MaxThroughput: 0
                Client: Device: 魅蓝 note 2
                deviceAddress: 6a:3e:34:3f:af:78
                primary type: 10-0050F204-5
                secondary type: null
                wps: 392
         */
        fun onWifiP2pGroupInfoUpdate(wifiP2pGroup: WifiP2pGroup)
    }
}
package com.supcon.scanwriter

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.util.*

/**
 * @author : yaobing
 * @date   : 2020/8/3 15:27
 * @desc   : 搜索到准备绑定（bond）的搜索列表适配器
 */
class DeviceListForConnectAdapter(activity: DeviceListForScanActivity) : BaseQuickAdapter<BluetoothDevice, BaseViewHolder>(R.layout.item_ble_device_for_connect) {
    private var mLeDevices: ArrayList<BluetoothDevice>? = null
    private var rssiDevice : HashMap<BluetoothDevice,Int> = HashMap()

    init {
        mLeDevices = ArrayList()
    }

    @SuppressLint("SetTextI18n")
    override fun convert(holder: BaseViewHolder, item: BluetoothDevice) {
        holder.getView<TextView>(R.id.tv_name).text = item.name
        holder.getView<TextView>(R.id.tv_address).text = item.address
        var bondState = ""
        var canBeBonded = false
        when (item.bondState) {
            BluetoothDevice.BOND_BONDED -> {
                bondState = "已经绑定"
                canBeBonded = false
            }
            BluetoothDevice.BOND_BONDING -> {
                bondState = "正在绑定..."
                canBeBonded= false
            }
            BluetoothDevice.BOND_NONE -> {
                bondState = "未绑定"
                canBeBonded = true
            }
        }
        holder.getView<TextView>(R.id.tv_device_bond_state).text = bondState
        holder.getView<TextView>(R.id.tv_address).text = "信号强度：" + rssiDevice[item].toString()
        holder.getView<TextView>(R.id.tv_device_bond_state).text = bondState
    }

    fun addDevice(device: BluetoothDevice?) {
        if (!mLeDevices!!.contains(device!!) && !TextUtils.isEmpty(device.name) && device.name.contains("")) {
            mLeDevices!!.add(device)
        }
        setList(mLeDevices)
    }

    fun clear() {
        mLeDevices?.clear()
        notifyDataSetChanged()
    }

    fun setRssi(rssiDevice: HashMap<BluetoothDevice, Int>) {
        this.rssiDevice = rssiDevice
        notifyDataSetChanged()
    }
}
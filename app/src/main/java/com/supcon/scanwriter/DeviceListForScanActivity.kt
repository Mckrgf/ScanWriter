package com.supcon.scanwriter

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.supcon.supbeacon.event.HttpEvent
import com.yaobing.module_middleware.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_device_list.*
import kotlinx.android.synthetic.main.all_title.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Suppress("DEPRECATION")
class DeviceListForScanActivity : BaseActivity(), View.OnClickListener {
    val SCAN_PERIOD: Long = 16 * 1000                 //BLE单次搜索时间
    private val REQUEST_ENABLE_BT = 1

    var mBluetoothAdapter: BluetoothAdapter? = null
    var mScanning = false
    private var mHandler: Handler? = null
    private val mDeviceListAdapter = DeviceListForConnectAdapter(this)
    private var currentDevice: BluetoothDevice? = null

    private var scanTime: Long = 0
    private var mwaitdlg: ProgressDialog? = null

    private var currentDevicePosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        initView()
        initData()
        checkPermissionAndScanDevice()
    }

    private fun initData() {
        mHandler = Handler()
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        registerLoginBroadcast()
        mwaitdlg = ProgressDialog(this)
        mwaitdlg?.isIndeterminate = false //循环滚动

        mwaitdlg?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mwaitdlg?.setMessage("正在连接蓝牙")
        mwaitdlg?.setCancelable(false) //false不能取消显示，true可以取消显示
        iv_refresh.visibility = View.VISIBLE
        tv_search.setOnClickListener(this)
        tv_search.visibility = View.VISIBLE
        fragment_title_des.text = getString(R.string.app_name) + AppUtils.getAppVersionName()
        number_progress_bar.max = SCAN_PERIOD.toInt()
        bt_ble_status.setOnClickListener(this)
        bt_ip_setting.setOnClickListener(this)
//        bt_ble_status.visibility = View.VISIBLE
//        bt_ip_setting.visibility = View.VISIBLE
        rv_devices_scan.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_devices_scan.adapter = mDeviceListAdapter

        rv_devices_bond.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        rb_scan.isChecked = true
        iv_return.setOnClickListener { finish() }
        mDeviceListAdapter.setOnItemClickListener { adapter, view, position ->
            val device = adapter.data[position] as BluetoothDevice
            if (bleBonded(device)) {
                val intent = Intent(this, DeviceControlActivity::class.java)
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.name)
                intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.address)
                if (this.mScanning) {
                    this.mBluetoothAdapter?.stopLeScan(this.mLeScanCallback)
                    this.mScanning = false
                }
                startActivity(intent)
            } else {
                ToastUtils.showLong("连接设备之前需要 【长按】 以绑定设备")
            }
        }


        v_transparent_black.setOnClickListener { }
        val dialogAnimationUp = TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0F, TranslateAnimation.RELATIVE_TO_SELF, 0f, TranslateAnimation.RELATIVE_TO_SELF, 1f, TranslateAnimation.RELATIVE_TO_SELF, 0f)
        dialogAnimationUp.duration = 400L //设置动画的过渡时间
        val dialogAnimationDown = TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0F, TranslateAnimation.RELATIVE_TO_SELF, 0f, TranslateAnimation.RELATIVE_TO_SELF, 0f, TranslateAnimation.RELATIVE_TO_SELF, 1f)
        dialogAnimationDown.duration = 400L //设置动画的过渡时间
        tv_cancel.setOnClickListener {
            closeBottomDialog(dialogAnimationDown)
        }
        mDeviceListAdapter.setOnItemLongClickListener { adapter, view, position ->
            currentDevice = adapter.data[position] as BluetoothDevice
            currentDevicePosition = position
            if (bleBonded(adapter.data[position] as BluetoothDevice)) {
                //底部弹窗解绑设备
                LogUtils.d("弹窗解绑")
                showBottomDialog("解绑", dialogAnimationUp)
            } else {
                //底部弹窗绑定设备
                LogUtils.d("弹窗绑定")
                showBottomDialog("绑定", dialogAnimationUp)
            }
            true
        }

        tv_bond.setOnClickListener {
            when (currentDevice?.bondState) {
                BluetoothDevice.BOND_BONDED -> {
                    if (ClsUtils.removeBond(BluetoothDevice::class.java, currentDevice)) {
                        ToastUtils.showLong("解除绑定成功")
                        mDeviceListAdapter.notifyItemChanged(currentDevicePosition)
                    }else {
                        ToastUtils.showLong("该系统不支持在app内解除蓝牙绑定，请自行解绑")
                        object : Thread() {
                            override fun run() {
                                super.run()
                                sleep(2000) //休眠3秒
                                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                startActivity(intent)
                            }
                        }.start()

                    }
                }
                BluetoothDevice.BOND_BONDING -> {
                    ToastUtils.showLong("正在绑定中")
                }
                BluetoothDevice.BOND_NONE -> {
                    currentDevice?.createBond()
                }
            }
            closeBottomDialog(dialogAnimationDown)
        }
    }

    private fun closeBottomDialog(dialogAnimationDown: TranslateAnimation) {
        mPopupLayout.postDelayed(Runnable {
            mPopupLayout.visibility = View.VISIBLE
            mPopupLayout.startAnimation(dialogAnimationDown)
            mPopupLayout.visibility = View.GONE
        }, 1)
    }

    private fun showBottomDialog(str: String, ctrlAnimation: TranslateAnimation) {
        tv_bond.text = str
        mPopupLayout.postDelayed(Runnable {
            mPopupLayout.visibility = View.VISIBLE
            mPopupLayout.startAnimation(ctrlAnimation)
        }, 1)
    }

    fun bleBonded(device: BluetoothDevice): Boolean {
        return when (device.bondState) {
            BluetoothDevice.BOND_BONDED -> {
                true
            }
            BluetoothDevice.BOND_BONDING -> {
                false
            }
            BluetoothDevice.BOND_NONE -> {
                false
            }
            else -> false
        }
    }


    private fun scanDevice(enable: Boolean) {
        if (enable) {
            iv_refresh.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotaterepeat))
            scanTime = System.currentTimeMillis()
            mHandler!!.postDelayed({
                if (mScanning) {
                    //若在单位时间内手动结束上一次扫描，开始下一次扫描，如果上一次扫描还未结束，则会停止上一次扫描
                    mScanning = false
                    mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
                    iv_refresh.clearAnimation()
                    tv_search.text = getString(R.string.START_SCAN)
                }
            }, SCAN_PERIOD)
            mScanning = true
            tv_search.text = getString(R.string.STOP_SCAN)
            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
        } else {
            iv_refresh.clearAnimation()
            mScanning = false
            tv_search.text = getString(R.string.START_SCAN)
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
            mHandler!!.removeCallbacksAndMessages(null);
        }
        invalidateOptionsMenu()
    }

    val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, _, _ ->
        runOnUiThread {
            number_progress_bar.progress = (System.currentTimeMillis() - scanTime).toInt()
            if (number_progress_bar.progress > number_progress_bar.max-300) number_progress_bar.progress = number_progress_bar.max
            mDeviceListAdapter.addDevice(device)
            mDeviceListAdapter.notifyDataSetChanged()
        }
    }


    private fun checkPermissionAndScanDevice() {
        if (!mBluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {

            mDeviceListAdapter.clear()
            addBondedDevice()
            scanDevice(true)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bt_ble_status -> {
                scanDevice(!mScanning)
                if (mScanning) mDeviceListAdapter.clear()
            }
            R.id.bt_ip_setting -> {
//                showNetAddrDlg(this, "网络配置", NetUtil().getIP(), NetUtil().getPort())
            }
            R.id.tv_search -> {

                scanDevice(!mScanning)
                if (mScanning) {
                    //如果是开始扫描，就先清除设备，然后添加绑定好的设备
                    mDeviceListAdapter.clear()
                    addBondedDevice()
                }
            }
        }
    }

    private fun addBondedDevice() {
        if ((mBluetoothAdapter?.bondedDevices?.size)!! > 0) {
            for (i in 0 until mBluetoothAdapter?.bondedDevices!!.size) {
                val device = mBluetoothAdapter?.bondedDevices!!.distinct()[i]
                mDeviceListAdapter.addDevice(device)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: HttpEvent?) {
    }
    private var mReceiver: BroadcastReceiver? = null
    private fun registerLoginBroadcast() {
        if (mReceiver == null) {
            mReceiver = BlueToothBondReceiver()
            val intentFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            registerReceiver(mReceiver, intentFilter)
        }
    }

    /**
     * 蓝牙配对广播
     */
    inner class BlueToothBondReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                // 找到设备后获取其设备
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                when (device?.bondState) {
                    BluetoothDevice.BOND_BONDING -> {
                        //正在配对
                        LogUtils.d("正在配对")
                        if (!mwaitdlg?.isShowing!!) {
                            mwaitdlg?.show()
                        }
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        //配对结束
                        if (mwaitdlg?.isShowing!!) {
                            mwaitdlg?.dismiss()
                        }
                        mDeviceListAdapter.notifyItemChanged(currentDevicePosition)
                        LogUtils.d("配对成功，刷新界面")
                    }
                    BluetoothDevice.BOND_NONE -> {
                        //取消配对/未配对
                        LogUtils.d("取消配对/未配对")
                        if (mwaitdlg?.isShowing!!) {
                            mwaitdlg?.dismiss()
                        }
                        mDeviceListAdapter.notifyItemChanged(currentDevicePosition)
                    }
                    else -> {
                    }
                }
            }
        }
    }

    //取消注册
    private fun unRegisterLoginBroadcast() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
            mReceiver = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //取消注册
        unRegisterLoginBroadcast()
    }
}
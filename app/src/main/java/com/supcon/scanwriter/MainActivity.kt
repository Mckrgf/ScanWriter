package com.supcon.scanwriter

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.uuzuche.lib_zxing.activity.CaptureActivity
import com.uuzuche.lib_zxing.activity.CodeUtils
import com.yaobing.module_middleware.Utils.ToastUtil
import com.yaobing.module_middleware.activity.BaseActivity
import com.yaobing.module_middleware.interfaces.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        bt_scan.setOnClickListener {
            val intent = Intent()
            intent.setClass(this, DeviceListForScanActivity::class.java)
            startActivityForResult(intent,101)
        }
    }

    private fun checkPermission() {
        requestRunTimePermission(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        ), object : PermissionListener {
            override fun onGranted() {  //所有权限授权成功
                ToastUtil.showToast(this@MainActivity, "所有权限授予成功")
            }

            override fun onGranted(grantedPermission: List<String?>) { //授权成功权限集合
                val nnn = grantedPermission.size
                Log.i("failed", "" + nnn)
                ToastUtil.showToast(
                    this@MainActivity,
                    "部分权限授予成功" + grantedPermission.size
                )
            }

            override fun onDenied(deniedPermission: List<String?>) { //授权失败权限集合
                ToastUtil.showToast(
                    this@MainActivity,
                    "部分权限授予失败" + deniedPermission.size
                )
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (null != data) {
            val bundle = data.extras ?: return
            if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                val result = bundle.getString(CodeUtils.RESULT_STRING)
                Toast.makeText(this, "解析结果:$result", Toast.LENGTH_LONG).show()
            } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                Toast.makeText(this@MainActivity, "解析二维码失败", Toast.LENGTH_LONG).show()
            }
        }
    }
}
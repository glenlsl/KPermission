package com.solin.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.solin.kpermission.ActResultHelper
import com.solin.kpermission.PermissionType
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button2.setOnClickListener {
            ActResultHelper.from(this)
                .startActivityForResult(Intent(this, SecondActivity::class.java)) { _, dataIntent ->
                    dataIntent?.getStringExtra("test")?.let {
                        textView.text = it
                    }
                }
        }
        button3.setOnClickListener {
            val array = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } else {
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            ActResultHelper.from(this)
                .requestPermissions(*array) {
                    Log.e(this.localClassName, "是否成功:$it")
                }
            //获取APK安装权限
            /* ActResultHelper.from(this).requestPermissions(PermissionType.APK_PERMISSION) {
                 Log.e(this.localClassName, "是否成功:$it")
             }*/
        }
    }
}


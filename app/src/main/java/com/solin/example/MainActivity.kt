package com.solin.example

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.solin.kpermission.*
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
            /* val array = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                 arrayOf(
                     Manifest.permission.WRITE_EXTERNAL_STORAGE,
                     Manifest.permission.READ_EXTERNAL_STORAGE
                 )
             } else {
                 arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
             }
             ActResultHelper.from(this)
                 .isShowDialog(true)
                 .requestPermissionsByType(*array) {
                     Log.e(this.localClassName, "是否成功授权:$it")
                 }*/
            //获取APK安装权限
            ActResultHelper.from(this)
                .isShowDialog(false)
                .requestPermissionsByType(
                    PermissionType.ApkType,
                    PermissionType.FileReadWriteType,
                    PermissionType.CameraType
                ) {
                    Log.e(this.localClassName, "是否成功授权:$it")
                }
        }
    }

}


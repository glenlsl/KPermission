package com.solin.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import android.util.Log
import com.solin.example.ui.test.TestFragmentActivity
import com.solin.kpermission.ActResultHelper
import com.solin.kpermission.PermissionTypeFactory
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 设置contentFeature,可使用切换动画
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
//            init_explode()// 分解
//            init_Slide()//滑动进入
            init_fade()//淡入淡出
        }*/
        setContentView(R.layout.activity_main)
        button2.setOnClickListener {
            ActResultHelper.from(this)
                .startActivityForResult(Intent(this, SecondActivity::class.java)) { _, dataIntent ->
                    dataIntent?.getStringExtra("test")?.let {
                        Log.e("SecondActivity Callback", it)
                        textView.text = it
                    }
                }
        }
        button5.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    TestFragmentActivity::class.java
                )
            )
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
                .isShowDialog(true)
                .requestPermissionsByType(
                    PermissionTypeFactory.ApkType,
                    PermissionTypeFactory.FileReadWriteType,
                    PermissionTypeFactory.CameraType
                ) {
                    Log.e(this.localClassName, "是否成功授权:$it")
                }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun init_fade() {
        val transition = Fade().setDuration(200)
        window.enterTransition = transition
        window.exitTransition = transition
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun init_Slide() {
        val transition = Slide().setDuration(200)
        window.enterTransition = transition
        window.exitTransition = transition
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun init_explode() {
        val explode = Explode()
        explode.duration = 200
        window.enterTransition = explode
    }
}


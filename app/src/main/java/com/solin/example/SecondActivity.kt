package com.solin.example

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import kotlinx.android.synthetic.main.activity_second.*
import kotlin.random.Random
import kotlin.random.nextInt
import androidx.annotation.RequiresApi
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import com.solin.kpermission.ActResultHelper


class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 设置contentFeature,可使用切换动画
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
//            init_explode()// 分解
//            init_Slide()//滑动进入
            init_fade()//淡入淡出
        }*/
        setContentView(R.layout.activity_second)
        button.setOnClickListener {
            val num = Random.nextInt(1..20)
            val data = Intent()
            data.putExtra("test", "(result=$num)i am back from the second page")
            setResult(num, data)
            finish()
        }
        ActResultHelper.from(this)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun init_fade() {
        val transition = Fade().setDuration(500)
        window.enterTransition = transition
        window.exitTransition = transition
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun init_Slide() {
        val transition = Slide().setDuration(500)
        window.enterTransition = transition
        window.exitTransition = transition
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun init_explode() {
        val explode = Explode()
        explode.duration = 500
        window.enterTransition = explode
    }
}

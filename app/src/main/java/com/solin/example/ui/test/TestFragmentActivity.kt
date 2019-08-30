package com.solin.example.ui.test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.solin.example.R

class TestFragmentActivity : AppCompatActivity() {
    private val fragment by lazy { TestFragment.newInstance() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow()
        }
    }

    fun onClick(view: View) {
        fragment.onClick(view)
    }

    /**
     * todo 很奇怪的问题，不重写此方法的话 fragment中使用 ActResultHelper.from(this).startActivityForResult 会拿不到回调。正常情况是不用重写activity的 onActivityResult
     * todo 这难道是系统bug吗？
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //todo doNoting
    }
}

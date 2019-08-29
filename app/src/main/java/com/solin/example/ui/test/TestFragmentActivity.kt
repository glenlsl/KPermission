package com.solin.example.ui.test

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
}

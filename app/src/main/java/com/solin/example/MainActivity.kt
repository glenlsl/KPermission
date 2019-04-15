package com.solin.example

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.solin.kpermission.ActResultHelper
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
    }
}


package com.solin.example

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_second.*
import kotlin.random.Random
import kotlin.random.nextInt

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        button.setOnClickListener {
            val num = Random.nextInt(1..20)
            val data = Intent()
            data.putExtra("test", "(result=$num)i am back from the second page")
            setResult(num, data)
            finish()
        }

    }
}

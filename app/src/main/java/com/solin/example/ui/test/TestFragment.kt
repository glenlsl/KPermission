package com.solin.example.ui.test

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.solin.example.R
import com.solin.example.SecondActivity
import com.solin.kpermission.ActResultHelper
import kotlinx.android.synthetic.main.test_fragment.*

class TestFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = TestFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.test_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    fun onClick(view: View) {
        when (view) {
            button4 -> {
                val intent = Intent(activity, SecondActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ActResultHelper.from(this)
                    .startActivityForResult(intent) { resultCode, dataIntent ->
                        Log.e("TestFragment", resultCode.toString())
                        message.text = dataIntent?.getStringExtra("test")
                    }
            }
        }
    }
}

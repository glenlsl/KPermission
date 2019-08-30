package com.solin.kpermission

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

/**
 *  空白Fragment 中转作用
 */
class ActResultFragment : Fragment() {

    var openDialog = false
    var failureReturn = false//是否弹出提示(对话框/toast)
    private val mCallbackMap = WeakHashMap<Int, MutableLiveData<ActResult>>()
    //    private val mPermissionsMap = WeakHashMap<String, MutableLiveData<Permission>>()//权限申请
    private var mPermissionCallback: MutableLiveData<Boolean>? = null//权限申请

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置为 true，表示 configuration change 的时候，fragment 实例不会背重新创建
        retainInstance = true
    }

    fun startActForResult(intent: Intent, mutableLiveData: MutableLiveData<ActResult>) {
        val requestCode = makeRequestCode()
        mCallbackMap[requestCode] = mutableLiveData
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (mCallbackMap.containsKey(requestCode)) {
            val liveData = mCallbackMap[requestCode]
            liveData?.value = ActResult(resultCode, data)
            mCallbackMap.remove(requestCode)
        }
    }

    fun requestPermissions(needRequests: Array<String>, callback: (isGranted: Boolean) -> Unit) {
        if (needRequests.isNotEmpty()) {
            mPermissionCallback = MutableLiveData()
            mPermissionCallback?.observe(this, Observer {
                it?.run(callback)
            })
            requestPermissions(needRequests, REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            val noPermissions =
                permissions.filterIndexed { index, _ -> grantResults[index] == PackageManager.PERMISSION_DENIED }
                    .toMutableList()
            if (noPermissions.isNotEmpty()) {
                if (failureReturn) {
                    mPermissionCallback!!.value = false
                } else {
                    activity?.apply {
                        if (openDialog) {
                            PermissionRequestDialog(this, noPermissions).show()
                        } else {
                            window?.decorView?.findViewById<View>(android.R.id.content)?.let {
                                Snackbar.make(it, "提示:还有有未授权的权限", Snackbar.LENGTH_LONG)
                                    .setAction("弹出未授权列表") {
                                        PermissionRequestDialog(this, noPermissions).show()
                                    }.show()
                            }
                        }
                    }
                }
            } else {
                mPermissionCallback!!.value = true
            }
        }
    }

    /**
     * 返回999-65535之间的随机数
     */
    private fun makeRequestCode(): Int {
        var requestCode: Int
        do {
            requestCode = Random.nextInt(999..65535)
        } while (mCallbackMap.containsKey(requestCode))
        return requestCode
    }

    //TODO: 单例模式
    companion object {
        private const val REQUEST_CODE = 0x99//权限请求码
        private var instance = WeakReference(ActResultFragment())
            get() {
                if (field.get() == null) {
                    field.clear()
                    field = WeakReference(ActResultFragment())
                }
                return field
            }

        fun get() = instance.get()!!
    }
}

data class ActResult(var resultCode: Int, val resultIntent: Intent? = null)

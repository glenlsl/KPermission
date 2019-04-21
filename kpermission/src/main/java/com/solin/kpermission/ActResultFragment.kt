package com.solin.kpermission

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.SparseArray
import kotlin.random.Random
import kotlin.random.nextInt

/**
 *  空白Fragment 中转作用
 */
internal class ActResultFragment : Fragment() {
    private val REQUEST_CODE = 0x99//权限请求码
    var openDialog = true
    private val mCallbackMap = SparseArray<MutableLiveData<ActResult>>()
    //    private val mPermissionsMap = WeakHashMap<String, MutableLiveData<Permission>>()//权限申请
    private var mPermissionCallback: MutableLiveData<Boolean>? = null//权限申请

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置为 true，表示 configuration change 的时候，fragment 实例不会背重新创建
        retainInstance = true
    }

    fun startActForResult(intent: Intent, mutableLiveData: MutableLiveData<ActResult>) {
        val requestCode = makeRequestCode()
        mCallbackMap.put(requestCode, mutableLiveData)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (mCallbackMap.indexOfKey(requestCode) >= 0) {
            val liveData = mCallbackMap[requestCode]
            liveData.value = ActResult(resultCode, data)
            mCallbackMap.remove(requestCode)
        }
    }

    fun requestPermissions(needRequests: Array<String>, callback: (isGranted: Boolean) -> Unit) {
        if (needRequests.isNotEmpty()) {
            mPermissionCallback = MutableLiveData<Boolean>().apply {
                observe(instance, Observer {
                    callback.invoke(it!!)
                })
            }
            requestPermissions(needRequests, REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            val noPermissions =
                permissions.filterIndexed { index, _ -> grantResults[index] == PackageManager.PERMISSION_DENIED }
                    .toMutableList()
            if (noPermissions.isNotEmpty()) {
                if (openDialog) {
                    context?.run { PermissionRequestDialog(instance.activity!!, noPermissions).show() }
                }
                mPermissionCallback!!.value = false
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
        } while (mCallbackMap.indexOfKey(requestCode) >= 0)
        return requestCode
    }

    //TODO: 单例模式
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            ActResultFragment()
        }
    }
}

data class ActResult(var resultCode: Int, val resultIntent: Intent? = null)
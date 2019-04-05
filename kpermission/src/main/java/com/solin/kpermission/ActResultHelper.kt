package com.solin.kpermission

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.util.SparseArray

/**
 *  权限申请、结果反馈以回调形式封装
 *  @author Solin
 *  @time 2018-4-2 14:13
 */
class ActResultHelper private constructor() {
    private constructor(activity: FragmentActivity) : this() {
        commitFragment(activity.supportFragmentManager)
    }

    private constructor(fragment: Fragment) : this() {
        commitFragment(fragment.childFragmentManager)
    }

    //todo:伴生对象静态属性
    companion object {
        const val TAG = "ActResultHelper"
        const val REQUEST_CODE = 0x99//权限请求码
        fun from(activity: FragmentActivity) = ActResultHelper(activity)
        fun from(fragment: Fragment) = ActResultHelper(fragment)
    }

    private fun commitFragment(fragmentManager: FragmentManager) {
        val fragment = (fragmentManager.findFragmentByTag(TAG) ?: ActResultFragment.getInstance()) as ActResultFragment
        if (!fragment.isAdded) {
            fragmentManager.beginTransaction()
                .add(fragment, TAG)//将响应需要用的空白fragment添加到需要响应页面绑定生命周期
                .commitNow()
        }
    }

    fun startActivityForResult(intent: Intent, callback: (resultCode: Int, dataIntent: Intent?) -> Unit) {
        val mutableLiveData = MutableLiveData<ActResult>().apply {
            observe(ActResultFragment.getInstance(), Observer {
                callback(it!!.resultCode, it.resultIntent)
            })
        }
        ActResultFragment.getInstance().startActForResult(intent, mutableLiveData)
    }

    /**
     * 批量检测权限是否授权
     */
    fun checkPermissions(vararg permissions: String, hasPermission: (permission: String, isGranted: Boolean) -> Unit) =
        apply {
            for (permission in permissions) hasPermission(permission, checkPermission(permission))
        }

    private fun checkPermission(permission: String): Boolean {
        //todo 这里的上下文获取在手动关闭权限后返回app 这时app被回收报null，fragment没有跟着被回收，暂未有好的解决办法，只能在activity中不保存fragment，让其findFragmentByTag为null
        return ContextCompat.checkSelfPermission(
            ActResultFragment.getInstance().requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED//已授权
    }

    /**
     * 请求权限授权
     */
//    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(vararg permissions: String, callback: (isGranted: Boolean) -> Unit) {
//    fun requestPermissions(vararg permissions: String, callback: (permission: String, isGranted: Boolean) -> Unit) {
        ActResultFragment.getInstance().requestPermissions(
            permissions.filterNot { checkPermission(it) }.toTypedArray(),
            callback
        )
    }
}

internal class ActResultFragment : Fragment() {
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
        /*fun requestPermissions(needRequests: Array<String>, callback: (permission: String, isGranted: Boolean) -> Unit) {
            for (permission in needRequests) {
                if (!mPermissionsMap.containsKey(permission)) {
                    val mutableLiveData = MutableLiveData<Permission>().apply {
                        observe(ActResultFragment.getInstance(), Observer {
                            callback.invoke(it!!.permission, it.isGranted)
    //                        mPermissionsMap.remove(permission)
                        })
                    }
                    mPermissionsMap[permission] = mutableLiveData
                }
            }*/
        if (needRequests.isNotEmpty()) {
            mPermissionCallback = MutableLiveData<Boolean>().apply {
                observe(getInstance(), Observer {
                    callback.invoke(it!!)
                })
            }
            requestPermissions(needRequests, ActResultHelper.REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ActResultHelper.REQUEST_CODE) {
            /*for ((index, permission) in permissions.withIndex()) {
                mPermissionsMap[permission]?.let {
                    val isGranted = grantResults[index] == PackageManager.PERMISSION_GRANTED
                    it.value = Permission(permission, isGranted)
                }
            }
            mPermissionsMap.remove(requestCode)*/
            val noPermissions = permissions.filterIndexed { index, _ -> grantResults[index] == PackageManager.PERMISSION_DENIED }
                .toMutableList()
            if (noPermissions.isNotEmpty()) {
                context?.run { PermissionRequestDialog(this, noPermissions).show() }
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
        val intRange = 0x3e7..0xFFFF
        do {
            requestCode = intRange.shuffled().last()
        } while (mCallbackMap.indexOfKey(requestCode) >= 0)
        return requestCode
    }

    //TODO: 单例模式
    companion object {
        fun getInstance() = Holder.instance
    }

    private object Holder {
        val instance = ActResultFragment()
    }

}

//data class Permission(var permission: String, var isGranted: Boolean)
data class ActResult(var resultCode: Int, val resultIntent: Intent? = null)

package com.solin.kpermission

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *  权限申请、结果反馈以回调形式封装
 *  @author Solin
 *  @time 2018-4-2 14:13
 */
class ActResultHelper : LifecycleObserver {
    private var activity: WeakReference<FragmentActivity>? = null
    private var fragment: WeakReference<Fragment>? = null
    private var fragmentManager: FragmentManager? = null
    private var resultFragment: ActResultFragment? by WeakAny(ActResultFragment())

    private constructor() : super()

    private constructor(activity: FragmentActivity) : this() {
        this.activity = WeakReference(activity)
        commitFragment(this.activity!!.get()!!.supportFragmentManager)
        activity.lifecycle.addObserver(this)
    }

    private constructor(fragment: Fragment) : this() {
        this.fragment = WeakReference(fragment)
        commitFragment(this.fragment!!.get()!!.childFragmentManager)
        fragment.lifecycle.addObserver(this)
    }

    companion object {
        const val TAG = "ActResultHelper"
        private var instance: ActResultHelper? = null
        fun from(activity: FragmentActivity): ActResultHelper {
            if (instance == null) {
                instance = ActResultHelper(activity)
            }
            return instance!!
        }

        fun from(fragment: Fragment): ActResultHelper {
            if (instance == null) {
                instance = ActResultHelper(fragment)
            }
            return instance!!
        }
    }

    private fun commitFragment(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
        val fragment =
            (fragmentManager.findFragmentByTag(TAG) ?: resultFragment) as ActResultFragment
        if (!fragment.isAdded) {
            fragmentManager.beginTransaction()
                .add(fragment, TAG)//将响应需要用的空白fragment添加到需要响应页面绑定生命周期
                .commitNow()
        }
    }

    fun startActivityForResult(
        intent: Intent,
        callback: (resultCode: Int, dataIntent: Intent?) -> Unit
    ) {
        val mutableLiveData = MutableLiveData<ActResult>()
        resultFragment?.run {
            mutableLiveData.observe(this, Observer {
                it?.run {
                    callback(resultCode, resultIntent)
                }
            })
            this.startActForResult(intent, mutableLiveData)
        }

    }

    /**
     * 批量检测权限是否授权
     */
    fun checkPermissions(
        vararg permissions: String,
        hasPermission: (permission: String, isGranted: Boolean) -> Unit
    ) =
        apply {
            for (permission in permissions) hasPermission(permission, checkPermission(permission))
        }

    fun checkPermission(permission: String): Boolean {
        //todo 这里的上下文获取在手动关闭权限后返回app 这时app被回收报null，fragment没有跟着被回收，暂未有好的解决办法，只能在activity中不保存fragment，让其findFragmentByTag为null
        return resultFragment?.requireContext()?.let {
            ContextCompat.checkSelfPermission(it, permission)
        } == PackageManager.PERMISSION_GRANTED//已授权
    }

    /**
     * 请求权限授权
     */
//    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(vararg permissions: String, callback: (isGranted: Boolean) -> Unit) {
        val array = permissions.filterNot { checkPermission(it) }.toTypedArray()
        if (array.isEmpty()) {
            callback.invoke(true)
        } else {
            resultFragment?.requestPermissions(array, callback)
        }
    }

    /**
     * 按功能获取权限
     * @param types APK_PERMISSION,FILE_PERMISSION
     */
    fun requestPermissionsByType(
        vararg types: PermissionType,
        callback: (isGranted: Boolean) -> Unit
    ) {
        val set = mutableSetOf<String>()
        for (type in types) set += type.getPermissions(resultFragment?.context)
        require(set.isNotEmpty()) { "parameter cannot be empty" }
        val permissions = set.filterNot { checkPermission(it) }.toTypedArray()
        if (permissions.isEmpty()) {
            callback.invoke(true)
        } else {
            resultFragment?.requestPermissions(permissions, callback)
        }
    }

    fun isShowDialog(isShow: Boolean): ActResultHelper {
        resultFragment?.openDialog = isShow
        return this
    }

    fun failureReturn(failureReturn: Boolean) {
        resultFragment?.failureReturn = failureReturn
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        Log.d(TAG, "destroy")
        instance = instance?.run {
            //            resultFragment??.let {
//                fragmentManager?.run {
//                    beginTransaction()
//                        .detach(it)
//                        .remove(it)
//                        .commitAllowingStateLoss()
//                    it.onDestroyView()
//                }
//                fragmentManager = null
//            }
            resultFragment?.let {
                fragmentManager?.run {
                    beginTransaction()
                        .detach(it)
                        .remove(it)
                        .commitAllowingStateLoss()
                    it.onDestroyView()
                }
                fragmentManager = null
                resultFragment = null
            }
            activity = null
            fragment = null
            null
        }
    }
}

class WeakAny<T>(private val any: T) : ReadWriteProperty<Any, T?> {
    private var weakReference = WeakReference(any)
    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        if (weakReference.get() == null) {
            weakReference.clear()
            weakReference = WeakReference(any)
        }
        return weakReference.get()!!
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        if (value == null) {
            weakReference.clear()
        } else {
            weakReference = WeakReference(value)
        }
    }

}
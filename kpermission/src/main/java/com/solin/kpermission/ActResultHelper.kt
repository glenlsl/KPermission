package com.solin.kpermission

import android.arch.lifecycle.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.util.SparseArray
import java.lang.ref.WeakReference
import kotlin.random.Random
import kotlin.random.nextInt

/**
 *  权限申请、结果反馈以回调形式封装
 *  @author Solin
 *  @time 2018-4-2 14:13
 */
class ActResultHelper : LifecycleObserver {
    private var activity: WeakReference<FragmentActivity>? = null
    private var fragment: WeakReference<Fragment>? = null

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

    //todo:伴生对象静态属性
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
        val fragment = (fragmentManager.findFragmentByTag(TAG) ?: ActResultFragment.instance) as ActResultFragment
        if (!fragment.isAdded) {
            fragmentManager.beginTransaction()
                .add(fragment, TAG)//将响应需要用的空白fragment添加到需要响应页面绑定生命周期
                .commitNow()
        }
    }

    fun startActivityForResult(intent: Intent, callback: (resultCode: Int, dataIntent: Intent?) -> Unit) {
        val mutableLiveData = MutableLiveData<ActResult>().apply {
            observe(ActResultFragment.instance, Observer {
                callback(it!!.resultCode, it.resultIntent)
            })
        }
        ActResultFragment.instance.startActForResult(intent, mutableLiveData)
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
            ActResultFragment.instance.requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED//已授权
    }

    /**
     * 请求权限授权
     */
//    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(vararg permissions: String, callback: (isGranted: Boolean) -> Unit) {
        val array = permissions.filterNot { checkPermission(it) }
        if (array.isEmpty()) {
            callback.invoke(true)
        } else {
            ActResultFragment.instance.requestPermissions(
                permissions.filterNot { checkPermission(it) }.toTypedArray(),
                callback
            )
        }
    }

    fun isShowDialog(isShow: Boolean): ActResultHelper {
        ActResultFragment.instance.openDialog = isShow
        return this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun destroy() {
        instance = instance?.run {
            activity = null
            fragment = null
            null
        }
    }
}

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
                    context?.run { PermissionRequestDialog(this, noPermissions).show() }
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

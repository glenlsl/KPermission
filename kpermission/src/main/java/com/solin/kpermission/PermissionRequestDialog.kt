package com.solin.kpermission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.FragmentActivity
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_permission.*
import kotlinx.android.synthetic.main.dialog_permission_item.view.*
import java.io.File
import java.io.IOException

/**
 *  缺少权限时的提示框
 *  @author Solin
 *  @time 2018-4-4 12:22
 */
class PermissionRequestDialog @JvmOverloads constructor(
    private val context: FragmentActivity,
    mutableList: MutableList<String>,
    themeResId: Int = R.style.style_permission_dialog,
    private val apkFile: File? = null
) : AlertDialog(context, themeResId) {

    private var adapter = SimpleDataAdapter()

    init {
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        for (permission in mutableList) {
            val item = RecyclerItem(permission = permission)
            when (permission) {
                Manifest.permission.CALL_PHONE -> {
                    item.title = "拨号权限"
                    item.describe = "允许应用拨打电话和管理通话"
                }
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    item.title = "文件存储"
                    item.describe = "允许应用写入存储"
                }
                Manifest.permission.READ_EXTERNAL_STORAGE -> {
                    item.title = "文件读取"
                    item.describe = "允许应用读取存储"
                }
                Manifest.permission.REQUEST_INSTALL_PACKAGES -> {
                    item.title = "安装未知应用"
                    item.describe = "允许安装未知应用"
                }
            }
            adapter.datas.add(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_permission)//todo 布局
        initView()
    }

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        btnOk.setOnClickListener {
            requestPermission()
        }
    }

    private fun requestPermission() {
        if (adapter.datas.isNotEmpty()) {
            for (i in adapter.datas.indices) {
                val data = adapter.datas[i]
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    && data.permission == Manifest.permission.REQUEST_INSTALL_PACKAGES
                    && !context.packageManager.canRequestPackageInstalls()
                ) { //注意这个是8.0安装未知应用时需要的新API
                    val packageURI = Uri.parse("package:" + context.packageName)
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI)
                    ActResultHelper.from(context)
                        .startActivityForResult(intent) { _, _ ->
                            if (context.packageManager.canRequestPackageInstalls()) {
                                apkFile?.run { installAPK(this) }
                                adapter.datas.remove(data)
                                if (adapter.itemCount == 0) {
                                    dismiss()
                                    return@startActivityForResult
                                }
                                adapter.notifyItemRemoved(i)
                            }
                        }
                    return
                }
            }
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            ActResultHelper.from(context).startActivityForResult(intent) { _, _ ->
                ActResultHelper.from(context)
                    .checkPermissions(*adapter.datas.map { it.permission }.toTypedArray()) { permission, isGranted ->
                        if (isGranted) {
                            val index = adapter.datas.indexOfFirst { it.permission == permission }
                            adapter.datas.removeAt(index)
                            if (adapter.itemCount == 0) {
                                dismiss()
                                return@checkPermissions
                            }
                            adapter.notifyItemRemoved(index)
                        }
                    }
            }
        }
    }

    /**
     * 安装apk文件
     */
    private fun installAPK(apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        //更新包文件
        if (Build.VERSION.SDK_INT >= 24) {
            // Android7.0及以上版本 Log.d("-->最新apk下载完毕","Android N及以上版本");
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", apkFile)
            //参数二:应用包名+".fileProvider"(和步骤二中的Manifest文件中的provider节点下的authorities对应)
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            try {
                val command = arrayOf("chmod", "777", apkFile.canonicalPath)
                val builder = ProcessBuilder(*command)
                builder.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // Android7.0以下版本 Log.d("-->最新apk下载完毕","Android N以下版本");
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

class SimpleDataAdapter : RecyclerView.Adapter<ViewHolder>() {
    var datas = mutableListOf<RecyclerItem>()
    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.initWeight(datas[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)
}

class ViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.dialog_permission_item,
            parent,
            false
        )
    ) {

    fun initWeight(item: RecyclerItem) {
        itemView.permissionTv1.text = item.title
        itemView.permissionTv2.text = item.describe
    }
}

data class RecyclerItem(var title: String = " ", var describe: String = " ", val permission: String)

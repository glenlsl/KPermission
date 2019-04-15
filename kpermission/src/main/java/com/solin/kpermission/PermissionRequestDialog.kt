package com.solin.kpermission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_permission.*
import kotlinx.android.synthetic.main.dialog_permission_item.view.*

/**
 *  缺少权限时的提示框
 *  @author Solin
 *  @time 2018-4-4 12:22
 */
open class PermissionRequestDialog @JvmOverloads constructor(
    context: Context,
    mutableList: MutableList<String>,
    themeResId: Int = R.style.style_permission_dialog
) : AlertDialog(context, themeResId) {

    private val datas = mutableListOf<RecyclerItem>()

    init {
//        setCanceledOnTouchOutside(true)
//        setCancelable(true)
        datas.clear()
        var count = mutableList.size
        for (permission in mutableList) {
            val item = RecyclerItem(permission)
            when (permission) {
                Manifest.permission.CALL_PHONE -> {
                    item.title = "拨号权限"
                    item.describe = "允许应用拨打电话和管理通话"
                }
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    item.title = "文件存储"
                    item.describe = "允许应用读取、写入外部存储"
                }
            }
            datas.add(item)
            count--
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_permission)//todo 布局
        initView()
    }

    open fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val adapter = SimpleDataAdapter()
        recyclerView.adapter = adapter
        adapter.datas.addAll(datas)
        adapter.notifyDataSetChanged()
        btnOk.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            ActResultFragment.instance.startActivity(intent)
            dismiss()
        }
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

data class RecyclerItem(var title: String = " ", var describe: String = " ")

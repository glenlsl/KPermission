package com.solin.kpermission

import android.Manifest
import android.os.Build

interface PermissionType {
    fun getPermissions(): MutableSet<String>
}

sealed class PermissionTypeFactory {
    object ApkType : PermissionType {
        override fun getPermissions(): MutableSet<String> {
            val mutableSet = mutableSetOf<String>()
            mutableSet.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)//文件写入
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mutableSet.add(Manifest.permission.READ_EXTERNAL_STORAGE)//文件读取
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mutableSet.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)//安装未知应用
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ActResultFragment.get()?.run {
                            requireContext().let {
                                if (it.packageManager.canRequestPackageInstalls()) {
                                    mutableSet.remove(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                                }
                            }
                        }
                    }
                }
            }
            return mutableSet
        }
    }

    object FileReadWriteType : PermissionType {
        override fun getPermissions(): MutableSet<String> {
            val mutableSet = mutableSetOf<String>()
            mutableSet.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)//"文件写入"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mutableSet.add(Manifest.permission.READ_EXTERNAL_STORAGE)//文件读取
            }
            return mutableSet
        }
    }

    object CameraType : PermissionType {
        override fun getPermissions(): MutableSet<String> {
            val mutableSet = FileReadWriteType.getPermissions()
            mutableSet.add(Manifest.permission.CAMERA)
            return mutableSet
        }
    }
}


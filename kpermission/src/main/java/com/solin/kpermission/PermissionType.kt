package com.solin.kpermission

import android.Manifest
import android.content.Context
import android.os.Build

/**
 * 权限按类型获取
 */
interface PermissionType {
    fun getPermissions(context: Context? = null): MutableSet<String>
}

sealed class PermissionTypeFactory {
    /**
     * 安装apk权限
     */
    object ApkType : PermissionType {
        override fun getPermissions(context: Context?): MutableSet<String> {
            val mutableSet = mutableSetOf<String>()
            mutableSet.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)//文件写入
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mutableSet.add(Manifest.permission.READ_EXTERNAL_STORAGE)//文件读取
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mutableSet.add(Manifest.permission.REQUEST_INSTALL_PACKAGES)//安装未知应用
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context?.let {
                            if (it.packageManager.canRequestPackageInstalls()) {
                                mutableSet.remove(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                            }
                        }
                    }
                }
            }
            return mutableSet
        }
    }

    /**
     * 文件读取写入权限
     */
    object FileReadWriteType : PermissionType {
        override fun getPermissions(context: Context?): MutableSet<String> {
            val mutableSet = mutableSetOf<String>()
            mutableSet.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)//"文件写入"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mutableSet.add(Manifest.permission.READ_EXTERNAL_STORAGE)//文件读取
            }
            return mutableSet
        }
    }

    /**
     * 相机权限
     */
    object CameraType : PermissionType {
        override fun getPermissions(context: Context?): MutableSet<String> {
            val mutableSet = FileReadWriteType.getPermissions()
            mutableSet.add(Manifest.permission.CAMERA)
            return mutableSet
        }
    }

    /**
     * 定位虚权限
     */
    object LocationType : PermissionType {
        override fun getPermissions(context: Context?): MutableSet<String> {
            val mutableSet = FileReadWriteType.getPermissions()
            mutableSet.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            mutableSet.add(Manifest.permission.ACCESS_FINE_LOCATION)
            mutableSet.add(Manifest.permission.READ_PHONE_STATE)
            return mutableSet
        }

    }
}


package com.solin.kpermission;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 按功能需求分类权限
 * @author Solin
 * Creation time 2019-4-21 10:52
 */
@IntDef({PermissionType.APK_PERMISSION, PermissionType.FILE_PERMISSION})
@Retention(RetentionPolicy.SOURCE)
public @interface PermissionType {
    int APK_PERMISSION = 1;
    int FILE_PERMISSION = 2;
}

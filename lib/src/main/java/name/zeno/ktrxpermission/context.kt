package name.zeno.ktrxpermission

import android.content.Context
import android.content.pm.PackageManager

/**
 * @author 陈治谋 (513500085@qq.com)
 * @since 2017/12/6
 */

/**
 * 获取 AppName
 */
val Context.appName: String
  get() {
    try {
      val appInfo = packageManager.getApplicationInfo(packageName, 0)
      return packageManager.getApplicationLabel(appInfo) as String
    } catch (e: PackageManager.NameNotFoundException) {
      return "App"
    }
  }

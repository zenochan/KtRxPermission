package cn.izeno.ktrxpermission

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.os.Build.VERSION_CODES.O
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cn.izeno.ktrxpermission.lifecycle.LifecycleFragment
import cn.izeno.ktrxpermission.lifecycle.LifecycleListener
import cn.izeno.ktrxpermission.lifecycle.LifecycleObservable
import cn.izeno.ktrxpermission.lifecycle.SupportLifecycleFragment
import io.reactivex.Observable

val marshmallow = SDK_INT >= M


/**
 * 周期安全
 * - 在 [Activity.onPause] 之后调用，会在 [Activity.onResume] 执行权限请求
 */
fun FragmentActivity.rxPermissions(
    vararg permissions: String,
    rational: ((permission: String) -> String?)? = null
): Observable<Boolean> = RxPermissions(this).request(*permissions, rational = rational)

fun Fragment.rxPermissions(
    vararg permissions: String,
    rational: ((permission: String) -> String?)? = null
): Observable<Boolean> = Observable.create<Boolean> { e ->
  // 确保在正确的周期执行请求
  if (isResumed) {
    e.onNext(true)
    e.onComplete()
  } else {
    val nav = when {
      this is LifecycleObservable -> this
      SDK_INT < O -> lifecycleObservable()
      else -> {
        activity?.supportLifecycleObservable()
            ?: throw error("you can't request permission before onResume() or after onPaused()")
      }
    }

    nav.registerLifecycleListener(object : LifecycleListener {
      override fun onResume() {
        nav.unregisterLifecycleListener(this)
        e.onNext(true)
        e.onComplete()
      }
    })
  }
}.flatMap {
  RxPermissions(this).request(*permissions, rational = rational)
}


/**
 * 检测指定权限是否被策略撤销。通常，设备主人或配置文件拥有者(如各种厂商)可能会使用某些策略
 * (如华为不允许其他应用商店安装应用)。用户无法授予被策略取消的权限，要获得权限只能通过改变策略
 * - see [PackageManager.isPermissionRevokedByPolicy]
 */
@TargetApi(Build.VERSION_CODES.M)
fun Context.isPermissionRevoked(permission: String): Boolean =
    marshmallow && packageManager.isPermissionRevokedByPolicy(permission, packageName)


fun Context.isPermissionGranted(vararg permissions: String) = permissions.all {
  ContextCompat.checkSelfPermission(this, it) == ZPermission.GRANTED
}

private fun Fragment.lifecycleObservable(): LifecycleObservable {
  val tag = LifecycleFragment.TAG
  val fm = requireFragmentManager()
  var fragment: LifecycleObservable? = fm.findFragmentByTag(tag) as? LifecycleObservable
  if (fragment == null) {
    fragment = LifecycleFragment()
    fm.beginTransaction().add(fragment, tag).commit()
  }

  return fragment
}

private fun FragmentActivity.supportLifecycleObservable(): LifecycleObservable {
  val tag = SupportLifecycleFragment.TAG
  var fragment: LifecycleObservable? = supportFragmentManager.findFragmentByTag(tag) as? LifecycleObservable
  if (fragment == null) {
    fragment = SupportLifecycleFragment()
    supportFragmentManager.beginTransaction().add(fragment, tag).commit()
  }

  return fragment
}

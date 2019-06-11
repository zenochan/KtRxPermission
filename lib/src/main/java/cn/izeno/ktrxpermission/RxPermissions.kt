package cn.izeno.ktrxpermission

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class RxPermissions(fragmentManager: FragmentManager) {

  constructor(activity: Activity) : this(activity.fragmentManager)
  constructor(fragment: Fragment) : this(when {
    SDK_INT > JELLY_BEAN_MR1 -> fragment.childFragmentManager
    else -> fragment.fragmentManager
  })

  private var fragment: RxPermissionsFragment

  init {
    fragment = getRxPermissionsFragment(fragmentManager)
  }

  private fun getRxPermissionsFragment(fragmentManager: FragmentManager): RxPermissionsFragment {
    var fragment: RxPermissionsFragment? = fragmentManager.findFragmentByTag(TAG) as? RxPermissionsFragment
    if (fragment == null) {
      fragment = RxPermissionsFragment()
      fragmentManager.beginTransaction().add(fragment, TAG).commitAllowingStateLoss()
      fragmentManager.executePendingTransactions()
    }

    return fragment
  }

  fun request(
      vararg permissions: String,
      rational: ((permission: String) -> String?)? = null
  ): Observable<Boolean> = Observable.just(TRIGGER)
      .mergeWith(pending(*permissions))
      .flatMap {
        if (permissions.isEmpty()) {
          error("RxPermissions.request/requestEach requires at least one input permission")
        }
        requestImplementation(*permissions, rational = rational)
      }
      .buffer(permissions.size)
      .flatMap({ results ->
        if (results.isEmpty()) {
          // 屏幕旋转时重现
          // Occurs during orientation change, when the subject receives onComplete.
          // In that case we don't want to propagate that empty list to the
          // subscriber, only the onComplete.
          Observable.empty<Boolean>()
        } else {
          Observable.just(results.all { it.granted })
        }
      })

  private fun pending(vararg permissions: String): Observable<Any> {
    // 没有正在请求的权限
    val nonePermissionRequest = permissions.none { it in fragment }
    return when {
      nonePermissionRequest -> Observable.empty<Any>()
      else -> Observable.just(TRIGGER)
    }
  }

  private fun requestImplementation(
      vararg permissions: String,
      rational: ((permission: String) -> String?)? = null
  ): Observable<Permission> {
    val observables = ArrayList<Observable<Permission>>(permissions.size)
    val ungranted = ArrayList<String>()

    // In case of multiple permissions, we create an Observable for each of them.
    // At the end, the observables are combined to have a unique response.
    // 多个权限申请，为每个权限创建一个 Observable， 最后，observables 组合成响应序列
    permissions.forEach { permission ->
      when {
        // Already granted, or not Android M
        // Return a granted Permission object.
        // 已经授权，获取系统低于 Android M，添加到已授权权限列表
        fragment.isGranted(permission) -> observables.add(Observable.just(Permission(permission, true)))
        // Revoked by a policy, return a denied Permission object.
        // 被策略撤销权限
        fragment.isRevoked(permission) -> observables.add(Observable.just(Permission(permission, false)))
        else -> {
          // 需要用户授权
          val subject: PublishSubject<Permission> = when (permission) {
            in fragment -> fragment[permission]!!
            else -> {
              // Create a new subject if not exists
              ungranted.add(permission)
              fragment[permission] = PublishSubject.create<Permission>()
              fragment[permission]!!
            }
          }
          observables.add(subject)
        }
      }
    }

    if (ungranted.isNotEmpty()) {
      fragment.requestPermissions(ungranted.toTypedArray(), rational = rational)
    }
    return Observable.concat(observables)
  }

  companion object {
    const internal val TAG = "name.zeno.ktrxpermission.RxPermissions"
    internal val TRIGGER = Any()
  }
}

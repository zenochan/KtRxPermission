package name.zeno.ktrxpermission

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Fragment
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import android.os.Bundle
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * - [shouldShowRequestPermissionRationale]
 *    - 弹出的对话框会有一个类似于“拒绝后不再询问”的勾选项
 *    - 若用户打了勾，并选择拒绝，那么下次程序调用Activity.requestPermissions()方法时，将不会弹出对话框
 *    - 提示用户类似于“您已经拒绝了使用该功能所需要的权限，若需要使用该功能，请手动开启权限”的信息
 * - [onRequestPermissionsResult] 接收授权结果
 */
class RxPermissionsFragment : Fragment() {

  // 包含所有当前请求的 permission
  // 当允许或拒绝权限申请时，从中移除
  private val mSubjects = HashMap<String, PublishSubject<Permission>>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Cannot retain Fragment that is nested in other Fragment
    if (SDK_INT < JELLY_BEAN_MR1 || parentFragment == null) {
      retainInstance = true
    }
  }

  @TargetApi(Build.VERSION_CODES.M)
  internal fun requestPermissions(permissions: Array<String>) {
    requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
  }

  @TargetApi(Build.VERSION_CODES.M)
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    if (requestCode != PERMISSIONS_REQUEST_CODE) return


    val shouldShowRequestPermissionRationale = permissions.map {
      shouldShowRequestPermissionRationale(it)
    }.toBooleanArray()

    onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationale)
  }

  private fun onRequestPermissionsResult(permissions: Array<String>, grantResults: IntArray, shouldShowRequestPermissionRationale: BooleanArray) {
    var i = 0
    val size = permissions.size
    while (i < size) {
      val subject = mSubjects[permissions[i]] ?: return
      mSubjects.remove(permissions[i])
      val granted = grantResults[i] == ZPermission.GRANTED
      subject.onNext(Permission(permissions[i], granted, shouldShowRequestPermissionRationale[i]))
      subject.onComplete()
      i++
    }
  }

  internal fun isGranted(permission: String): Boolean = activity.isPermissionGranted(permission)

  @TargetApi(Build.VERSION_CODES.M)
  internal fun isRevoked(permission: String): Boolean = activity.isPermissionRevoked(permission)

  operator fun get(permission: String): PublishSubject<Permission>? = mSubjects[permission]
  operator fun contains(permission: String): Boolean = mSubjects.containsKey(permission)

  operator fun set(permission: String, subject: PublishSubject<Permission>) {
    mSubjects.put(permission, subject)
  }

  companion object {
    const private val PERMISSIONS_REQUEST_CODE = 42
  }

}

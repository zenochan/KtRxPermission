package cn.izeno.ktrxpermission

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlin.collections.ArrayList

/**
 * - [shouldShowRequestPermissionRationale]
 *    - 弹出的对话框会有一个类似于“拒绝后不再询问”的勾选项
 *    - 若用户打了勾，并选择拒绝，那么下次程序调用Activity.requestPermissions()方法时，将不会弹出对话框
 *    - 提示用户类似于“您已经拒绝了使用该功能所需要的权限，若需要使用该功能，请手动开启权限”的信息
 * - [onRequestPermissionsResult] 接收授权结果
 */
internal class RxPermissionsFragment : Fragment() {

  // 包含所有当前请求的 permission
  // 当允许或拒绝权限申请时，从中移除
  private val mSubjects = HashMap<String, PublishSubject<Permission>>()

  private val sharedPreferences by lazy { requireActivity().getSharedPreferences(TAG, 0) }
  private var activityResult: ((ok: Boolean, data: Intent?) -> Unit)? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Cannot retain Fragment that is nested in other Fragment
    if (parentFragment == null) {
      retainInstance = true
    }
  }

  @TargetApi(Build.VERSION_CODES.M)
  internal fun requestPermissions(
      permissions: Array<String>,
      rational: ((permission: String) -> String?)? = null
  ) {
    val edit = sharedPreferences.edit()
    val neverAsk = permissions.filter {
      val asked = sharedPreferences.getBoolean(it, false)
      if (asked) {
        !shouldShowRequestPermissionRationale(it)
      } else {
        sharedPreferences.edit().putBoolean(it, true).apply()
        false
      }
    }
    edit.apply()

    if (neverAsk.isEmpty()) {
      requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
    } else {
      detailFormPermission(neverAsk, rational) { requestPermissions(permissions, PERMISSIONS_REQUEST_CODE) }
    }
  }

  /**
   * 微信使用电话权限确定本机号码
   */
  private fun detailFormPermission(
      permissions: List<String>,
      rational: ((permission: String) -> String?)?,
      next: () -> Unit
  ) {
    activityResult = { _, _ -> next() }

    // 微信使用电话权限确定本机号码和设备
    // ID,以保证帐号登录的安全性。微信不
    // 会拨打其他号码或终止通话。
    val msgs = ArrayList<String>()

    var msg: String
    val appName = requireActivity().appName
    var groupName: String
    permissions.forEach {
      groupName = ZPermission.groupName(it)
      msg = rational?.invoke(it) ?: "<b>$appName</b> 需要使用 <b>$groupName</b> 权限"
      if (msg !in msgs) msgs.add(msg)
    }

    msg = StringBuffer()
        .append(msgs.joinToString("", "<p>", "</p>"))
        .append("请在 <b>设置-应用-$appName-权限</b> 中开启权限,以正常使用 $appName。")
        .toString()

    @Suppress("DEPRECATION")
    confirm(Html.fromHtml(msg)) {
      if (!it) {
        next()
      } else {
        activityResult = { _, _ -> next() }
        val uri = Uri.parse("package:" + requireActivity().packageName)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
          startActivityForResult(intent, 1)
        }
      }
    }
  }

  private fun confirm(msg: CharSequence, next: (ok: Boolean) -> Unit) {
    AlertDialog.Builder(requireContext())
        .setTitle("权限申请")
        .setMessage(msg)
        .setPositiveButton("去设置") { _, _ -> next(true) }
        .setNegativeButton("取消") { _, _ -> next(false) }
        .setCancelable(false)
        .create()
        .show()
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    activityResult?.invoke(resultCode == Activity.RESULT_OK, data)
    activityResult = null
  }

  @TargetApi(Build.VERSION_CODES.M)
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode != PERMISSIONS_REQUEST_CODE) return

    val neverAsk = permissions.map {
      !shouldShowRequestPermissionRationale(it)
    }.toBooleanArray()

    onRequestPermissionsResult(permissions, grantResults, neverAsk)
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

  internal fun isGranted(permission: String): Boolean = requireActivity().isPermissionGranted(permission)

  @TargetApi(Build.VERSION_CODES.M)
  internal fun isRevoked(permission: String): Boolean = requireActivity().isPermissionRevoked(permission)

  operator fun get(permission: String): PublishSubject<Permission>? = mSubjects[permission]
  operator fun contains(permission: String): Boolean = mSubjects.containsKey(permission)

  operator fun set(permission: String, subject: PublishSubject<Permission>) {
    mSubjects[permission] = subject
  }

  companion object {
    const val TAG = "name.zeno.ktrxpermission.RxPermissionsFragment"
    private const val PERMISSIONS_REQUEST_CODE = 42
  }
}

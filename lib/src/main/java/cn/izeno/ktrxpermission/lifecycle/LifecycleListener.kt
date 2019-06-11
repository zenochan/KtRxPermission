package cn.izeno.ktrxpermission.lifecycle

import android.content.Intent

/**
 * @author 陈治谋 (513500085@qq.com)
 * @since  16/6/25
 */
internal interface LifecycleListener {
  fun onStart() {}

  fun onCreate() {}

  /** fragment  */
  fun onViewCreated() {}

  fun onResume() {}

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

  fun onPause() {}

  fun onStop() {}

  /** fragment  */
  fun onDestroyView() {}

  fun onDestroy() {}
}

package name.zeno.ktrxpermission.lifecycle

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.View

/**
 * @author 陈治谋 (513500085@qq.com)
 * @since 2017/12/4
 */
internal class LifecycleFragment : Fragment(), LifecycleObservable {
  private val listenerList = ArrayList<LifecycleListener>()

  override fun registerLifecycleListener(listener: LifecycleListener) {
    if (!listenerList.contains(listener)) {
      listenerList.add(listener)
    }
  }

  override fun unregisterLifecycleListener(listener: LifecycleListener) {
    if (listenerList.contains(listener)) {
      listenerList.remove(listener)
    }
  }


  //<editor-fold desc="life circle">
  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    listenerList.forEach { it.onCreate() }
  }

  override fun onStart() {
    super.onStart()
    listenerList.forEach { it.onStart() }
  }

  @CallSuper
  override fun onResume() {
    super.onResume()
    listenerList.forEach { it.onResume() }
  }

  @CallSuper
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    listenerList.forEach { it.onActivityResult(requestCode, resultCode, data) }
  }


  @CallSuper
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    listenerList.forEach { it.onViewCreated() }
  }

  @CallSuper
  override fun onDestroyView() {
    super.onDestroyView()
    listenerList.forEach { it.onDestroyView() }
  }

  @CallSuper
  override fun onStop() {
    super.onStop()
    listenerList.forEach { it.onStop() }
  }

  override fun onPause() {
    super.onPause()
    listenerList.forEach { it.onPause() }
  }

  @CallSuper
  override fun onDestroy() {
    super.onDestroy()
    listenerList.forEach { it.onDestroy() }
  }

  companion object {
    const val TAG = "name.zeno.ktrxpermission.lifecycle.LifecycleFragment"
  }
}

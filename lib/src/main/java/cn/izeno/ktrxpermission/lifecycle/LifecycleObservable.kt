package cn.izeno.ktrxpermission.lifecycle

/**
 * @author 陈治谋 (513500085@qq.com)
 * @since 2017/1/13.
 */
internal interface LifecycleObservable {
  fun registerLifecycleListener(listener: LifecycleListener)
  fun unregisterLifecycleListener(listener: LifecycleListener)
}

# KtRxPermission
> 扩展 [RxPermissions](https://github.com/tbruyelle/RxPermissions)

#### FEATURES
- [x] 使用 kotlin 实现
- [x] 添加“不再询问”的权限获取
- [x] 已迁移至 AndroidX (2018年10月15日)

#### usage

```groovy
mavan { url "http://maven.mjtown.cn/"}
implementation "name.zeno:kt-rx-permission:$latest_version"
```
[latest_version](https://github.com/zenochan/KtRxPermission/releases)

```kotlin
rxPermissions(
    ZPermission.ACCESS_COARSE_LOCATION,
    ZPermission.ACCESS_FINE_LOCATION,
    ZPermission.CALL_PHONE,
    rational = { permission ->
      when (permission) {
        ZPermission.ACCESS_FINE_LOCATION,
        ZPermission.ACCESS_COARSE_LOCATION
        -> "大哥儿，给个位置权限呗~ 我又不跟踪你"
        else -> null
      }

    }
).subscribe { granted ->
  if (granted) {
    // do something
    Toast.makeText(this, "已授权", Toast.LENGTH_SHORT).show()
  }
}
```


## CHANGE LOG

#### 2.0.1810150
- 迁移至 AndroidX

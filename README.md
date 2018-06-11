# KtRxPermission
> 参考自 [RxPermissions](https://github.com/tbruyelle/RxPermissions)  
> 在其基础上添加了“不再询问”的权限获取  
> 并选用 kotlin 实现

#### dependencies

| -             | lib                   |   version |
|---            |---                    |---        |
|implementation | appcompat-v7          | 27.0.2    |
| api           | kotlin-stdlib-jre7    | 1.2.0     |
| api           | rxjava                | 2.1.7     |


#### usage

```groovy
mavan { url "http://maven.mjtown.cn/"}
implementation "name.zeno:kt-rx-permissions:1.0.1806111"
```

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


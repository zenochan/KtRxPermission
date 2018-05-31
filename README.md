# KtRxPermission
Kotlin Rx Permission

#### dependencies

| -             | lib                   |   version |
|---            |---                    |---        |
|implementation | appcompat-v7          | 27.0.2    |
|implementation | kotlin-stdlib-jre7    | 1.2.0     |
| api           | rxjava                | 2.1.7     |


#### usage

```groovy
mavan { url "http://maven.mjtown.cn/"}
implementation "name.zeno:KtRxPermissions:0.0.2.20180531"
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


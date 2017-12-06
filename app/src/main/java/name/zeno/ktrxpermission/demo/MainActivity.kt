package name.zeno.ktrxpermission.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import name.zeno.ktrxpermission.ZPermission
import name.zeno.ktrxpermission.rxPermissions

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    btn.setOnClickListener({
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
    })
  }
}

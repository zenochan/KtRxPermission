package name.zeno.ktrxpermission.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import io.reactivex.Observable
import name.zeno.ktrxpermission.ZPermission
import name.zeno.ktrxpermission.rxPermissions

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    rxPermissions(ZPermission.ACCESS_COARSE_LOCATION).subscribe {
      Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
    }

    val test = Any()
    Observable.merge(Observable.just(test), Observable.just(test))
        .flatMap {
          Log.e("20171204 in flatMap", it.toString())
          Observable.just("bbb")
        }
        .subscribe {
          Log.e("20171204", it)
        }
  }

}

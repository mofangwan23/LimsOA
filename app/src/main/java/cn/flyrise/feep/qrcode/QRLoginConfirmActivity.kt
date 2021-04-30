package cn.flyrise.feep.qrcode

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.flyrise.feep.R
import kotlinx.android.synthetic.main.qrcode_activity_login_confirm.*

/**
 * author : klc
 * data on 2018/6/5 10:53
 * Msg : 扫码登录确认界面
 */
class QRLoginConfirmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qrcode_activity_login_confirm)
        btLogin.setOnClickListener({
            setResult(Activity.RESULT_OK)
            finish()
        })
    }

}
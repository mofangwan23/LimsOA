package cn.flyrise.feep.meeting

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.text.TextUtils
import android.widget.ImageView
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.squirtlez.frouter.annotations.Route
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.jakewharton.rxbinding.view.RxView
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit


/**
 * @author ZYP
 * @since 2018-07-03 13:34
 *
 * Intent 请求参数：qrCode String 类型
 */
@Route("/meeting/qrcode")
class MeetingQRCodeActivity : BaseActivity() {

    private var ivQRCodeImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_qrcode)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        toolbar?.title = "二维码"
    }

    override fun bindView() {
        ivQRCodeImage = findViewById(R.id.ivMeetingQRCode)
        RxView
                .clicks(findViewById(R.id.tvSaveToSdCard))
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe {
                    LoadingHint.show(MeetingQRCodeActivity@ this)
                    val topic = intent.getStringExtra("title")
                    val storePath = "${Environment.getExternalStorageDirectory()}/${topic}"
                    val fileName = DateUtil.formatTimeForHms(System.currentTimeMillis())+".jpg"
                    val appFile = File(storePath)
                    if(!appFile.exists())appFile.mkdir()
                    val file = File(appFile,fileName)
                    Observable.just(storePath)
                            .map {
                                if (file.exists()) file.delete()
                                file
                            }
                            .map {
                                val bitmap = Bitmap.createBitmap(
                                        ivQRCodeImage?.measuredWidth ?: 0,
                                        ivQRCodeImage?.measuredHeight ?: 0,
                                        Bitmap.Config.ARGB_8888)

                                val canvas = Canvas(bitmap)
                                ivQRCodeImage?.draw(canvas)

                                val fos = FileOutputStream(it)
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                fos.flush()
                                fos.close()
                                200
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext {
                                LoadingHint.hide()
                                val uri = Uri.fromFile(file)
                                this.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                            }
                            .doOnError { LoadingHint.hide() }
                            .subscribe { FEToast.showMessage("保存成功") }

                }
    }

    override fun bindData() {
        val qrCode = intent.getStringExtra("qrCode")
        if (TextUtils.isEmpty(qrCode)) {
            finish()
            return
        }

        LoadingHint.show(this)
        Observable.just(qrCode)
                .delay(1, TimeUnit.SECONDS)
                .map {

                    val width = ivQRCodeImage?.measuredWidth ?: 0
                    val height = ivQRCodeImage?.measuredHeight ?: 0

                    val hints = HashMap<EncodeHintType, String>()
                    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8")
                    val bitMatrix = QRCodeWriter().encode(qrCode, BarcodeFormat.QR_CODE, width, height, hints)
                    val pixels = IntArray(width * height)

                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            pixels[y * width + x] = if (bitMatrix.get(x, y)) -0x1000000 else -0x1
                        }
                    }

                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                    bitmap
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { LoadingHint.hide() }
                .doOnError { LoadingHint.hide() }
                .subscribe({
                    ivQRCodeImage?.setImageBitmap(it)
                }, {
                    it.printStackTrace()
                    FEToast.showMessage("二维码生成失败，请联系管理员")
                })
    }

}
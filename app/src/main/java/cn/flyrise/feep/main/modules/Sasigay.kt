package cn.flyrise.feep.main.modules

import android.text.TextUtils
import android.util.EventLog
import cn.flyrise.android.protocol.entity.SwitchCompanyRequest
import cn.flyrise.feep.addressbook.model.Department
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.core.function.AppMenu
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.event.CompanyChangeEvent
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

/**
 * @author 社会主义接班人
 * @since 2018-07-27 14:11
 */
object Sasigay {

    private var selectedCompany: Department? = null

    /**
     * 从 SdCard 中获取用户保存的公司
     */
    fun readCompanyFromStorage(): Department? {
        val path = CoreZygote.getPathServices().userPath + File.separator + CommonUtil.getMD5("CompanySetting")
        return try {
            val optionsFile = File(path)
            if (!optionsFile.exists()) return null

            var bytes: ByteArray? = null
            FileInputStream(optionsFile).apply {
                val len = optionsFile.length()
                bytes = ByteArray(len.toInt())
                read(bytes)
                close()
            }

            if (bytes?.size == 0) return null
            GsonUtil.getInstance().fromJson<Department>(String(bytes!!), Department::class.java)
        } catch (exp: Exception) {
            null
        }
    }

    /**
     * 将用户选中的公司保存到 SdCard 目录
     */
    fun saveCompanyToStorage(company: Department) {
        val path = CoreZygote.getPathServices().userPath + File.separator + CommonUtil.getMD5("CompanySetting")
        val optionsFile = File(path)
        if (optionsFile.exists()) optionsFile.delete()
        FileWriter(optionsFile).apply {
            write(GsonUtil.getInstance().toJson(company))
            flush()
            close()
        }
    }

    fun notifyCompanyChange(company: Department) {
        if (TextUtils.isEmpty(company.deptId)) return
        EventBus.getDefault().post(CompanyChangeEvent(company))
        FEHttpClient.getInstance().post(SwitchCompanyRequest(company.deptId), object : ResponseCallback<ResponseContent>() {
            override fun onCompleted(t: ResponseContent?) {}
        })
    }

    fun saveCompany(d: Department?) {
        this.selectedCompany = d
    }

    fun getSelectedCompany() = selectedCompany
}


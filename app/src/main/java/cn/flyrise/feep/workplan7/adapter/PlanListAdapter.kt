package cn.flyrise.feep.workplan7.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.utils.Patches
import cn.flyrise.feep.workplan7.model.WorkPlanListItemBean
import kotlinx.android.synthetic.main.plan_view_list_item.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.text.SimpleDateFormat

/**
 * author : klc
 * Msg : 计划列表Adapter
 */
class PlanListAdapter(val context: Context, val isRecevier: Boolean) : BaseMessageRecyclerAdapter<WorkPlanListItemBean>() {

    private var data: ArrayList<WorkPlanListItemBean>? = null
    private var loginUser: String? = null
    var simpleD: SimpleDateFormat? = null

    init {
        @SuppressLint("SimpleDateFormat")
        simpleD = SimpleDateFormat(CoreZygote.getContext().getString(cn.flyrise.feep.core.R.string.time_format_yMdhm))
        loginUser == CoreZygote.getLoginUserServices().userId
    }

    private var typeTexts = context.resources.getStringArray(R.array.plan_types)

    fun setData(data: ArrayList<WorkPlanListItemBean>?) {
        this.data = data
        this.notifyDataSetChanged()
    }

    fun getData() = data

    fun addData(data: ArrayList<WorkPlanListItemBean>?) {
        if (!CommonUtil.isEmptyList(data)) {
            this.data?.addAll(data!!)
            notifyDataSetChanged()
        }
    }

    override fun getDataSourceCount(): Int = if (CommonUtil.isEmptyList(data)) 0 else data!!.size

    override fun getLoadBackgroundColor() = Color.parseColor("#F5F6F6")

    @SuppressLint("SetTextI18n")
    override fun onChildBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val view = (holder as ViewHolder).itemView
        val item = data!![position]
        view.ivHead.visibility = if (TextUtils.isEmpty(item.sendUserId)) View.GONE else View.VISIBLE
        CoreZygote.getAddressBookServices().queryUserDetail(item.sendUserId)
                .subscribe({
                    if (it != null) {
                        FEImageLoader.load(context, view.ivHead, CoreZygote.getLoginUserServices().serverAddress + it.imageHref, it.userId, it.name)
                    } else {
                        FEImageLoader.load(context, view.ivHead, R.drawable.administrator_icon)
                    }
                }, {
                    FEImageLoader.load(context, view.ivHead, R.drawable.administrator_icon)
                })

        if (isRecevier) {
            view.ivRead.visibility = if (item.isNews && loginUser != item.sendUser) View.VISIBLE else View.INVISIBLE
        } else {
            view.ivRead.visibility = View.GONE
        }

        view.tvUserName.text = item.sendUser
        view.tvTitle.text = if (FunctionManager.hasPatch(Patches.PATCH_PLAN)) {//兼容66
            "[${typeTexts[(item.type?.toInt() ?: 2) - 1]}]${item.title}"
        } else {
            item.title
        }
        view.tvTime.text = dateText(item.sendTime)
        view.setOnClickListener { onItemClickListener?.onItemClick(view, item) }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun onChildCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.plan_view_list_item, parent, false))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private fun dateText(text: String) = simpleD?.format(DateUtil.stringToDateTime(text)?.time) ?: text
}
package cn.flyrise.feep.workplan7.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.AddressBookDetailActivity
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.image.loader.FEImageLoader
import com.jakewharton.rxbinding.view.RxView
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * 新建：陈冕;
 *日期： 2018-6-28-15:57.
 */
class PlanClassifyListAdapter(val context: Context) : BaseRecyclerAdapter() {

    var planList: MutableList<String>? = arrayListOf()//一堆用户id

    fun setData(userIds: List<String>) {
        planList!!.clear()
        planList!!.addAll(userIds)
        notifyDataSetChanged()
    }

    fun addData(userIds: ArrayList<String>) {
        planList!!.addAll(userIds)
        notifyDataSetChanged()
    }

    override fun getDataSourceCount(): Int {
        return if (CommonUtil.isEmptyList(planList)) 0 else planList!!.size
    }

    override fun onChildBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        CoreZygote.getAddressBookServices().queryUserDetail(planList!![position])
                .subscribe({ f ->
                    if (f != null) {
                        viewHolder.planName.text = f.name
                        viewHolder.planDept.text = f.deptName
                        FEImageLoader.load(context, viewHolder.planIcon, CoreZygote.getLoginUserServices().serverAddress + f.imageHref, f.userId, f.name)
                        RxView.clicks(holder.planLayout).throttleFirst(1, TimeUnit.SECONDS).subscribe {
                            val intent = Intent(context, AddressBookDetailActivity::class.java)
                            intent.putExtra(K.addressBook.user_id, f.userId)
                            context.startActivity(intent)
                        }
                    } else {
                        FEImageLoader.load(context, viewHolder.planIcon, R.drawable.administrator_icon)
                    }
                }, {
                    FEImageLoader.load(context, viewHolder.planIcon, R.drawable.administrator_icon)
                })

//        viewHolder.planName.text = addressBook.name
//        viewHolder.planDept.text = addressBook.deptName
//        FEImageLoader.load(context, viewHolder.planIcon, CoreZygote.getLoginUserServices().serverAddress + addressBook.imageHref
//                , addressBook.userId, addressBook.name)
    }

    override fun onChildCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent!!.context)!!.inflate(R.layout.plan_classify_list_item_layout, parent, false))
    }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val planLayout = itemView
        var planIcon: ImageView = itemView.findViewById(R.id.plan_icon);
        var planName: TextView = itemView.findViewById(R.id.plan_name)
        var planDept: TextView = itemView.findViewById(R.id.plan_dept)
    }

}
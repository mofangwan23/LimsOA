package cn.flyrise.feep.workplan7.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.core.services.model.AddressBook
import kotlinx.android.synthetic.main.plan_view_user_item.view.*

/**
 * author : klc
 * data on 2018/6/19 15:35
 * Msg :
 */
class PlanUserLayoutAdapter(private val context: Context, private val userList: List<AddressBook>?) : RecyclerView.Adapter<PlanUserLayoutAdapter.UserViewHolder>() {

	override fun getItemCount(): Int = if (userList == null) 0 else userList!!.size

	override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
		val userInfo = userList!![position]
		val host = CoreZygote.getLoginUserServices().serverAddress
		val itemView = holder.itemView
		FEImageLoader.load(context, itemView.ivHead, host + userInfo.imageHref, userInfo.userId, userInfo.name)
		itemView.tvName.text = userInfo.name
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
		return UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.plan_view_user_item, parent, false))
	}

	inner class UserViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView)
}
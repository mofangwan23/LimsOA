package cn.flyrise.feep.collaboration.search

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.R
import cn.flyrise.feep.commonality.bean.FEListItem
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.image.loader.FEImageLoader
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author ZYP
 * @since 2018-05-16 11:52
 */
class ApprovalSearchAdapter(val emptyView: ImageView) : BaseRecyclerAdapter() {

    private var mDataSources: MutableList<FEListItem>? = null
    private lateinit var mItemClickListener: (listItem: FEListItem) -> Unit
    private var mKeyword: String? = null
    private val mHost: String

    init {
        mHost = CoreZygote.getLoginUserServices().serverAddress
    }

    fun setOnApprovalItemClickListener(itemClickListener: (listItem: FEListItem) -> Unit) {
        this.mItemClickListener = itemClickListener
    }

    fun setDataSources(dataSource: MutableList<FEListItem>) {
        this.mDataSources = dataSource
        if (CommonUtil.isEmptyList(mDataSources)) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
        notifyDataSetChanged()
    }

    fun appendDataSource(dataSource: MutableList<FEListItem>) {
        if (CommonUtil.isEmptyList(mDataSources)) {
            mDataSources = ArrayList()
        }
        mDataSources!!.addAll(dataSource)

        if (CommonUtil.isEmptyList(mDataSources)) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
        notifyDataSetChanged()
    }

    fun searchFailure() {
        emptyView.visibility = if (CommonUtil.isEmptyList(mDataSources)) View.VISIBLE else View.GONE
        notifyDataSetChanged()
    }

    fun setKeyword(keyword: String?) {
        this.mKeyword = keyword
    }

    override fun getDataSourceCount(): Int {
        return if (CommonUtil.isEmptyList(mDataSources)) 0 else mDataSources!!.size
    }

    override fun onChildBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val listItem = mDataSources!![position]
        holder as ApprovalSearchViewHolder

        holder.tvTitle.text = Html.fromHtml(deepenOn(listItem.title))
        holder.tvAuthor.text = Html.fromHtml(deepenOn(listItem.sendUser))

        // 设置他妈狗日的头像
        if (TextUtils.isEmpty(listItem.sendUserId)) {
            holder.ivAvatar.visibility = View.GONE
        } else {
            CoreZygote.getAddressBookServices().queryUserDetail(listItem.sendUserId)
                    .subscribe({
                        if (it != null) {
                            holder.ivAvatar.visibility = View.VISIBLE
                            FEImageLoader.load(CoreZygote.getContext(), holder.ivAvatar, mHost + it.imageHref, listItem.sendUserId, listItem.sendUser)
                        } else {
                            holder.ivAvatar.visibility = View.GONE
                        }
                    }, { error ->
                        holder.ivAvatar.visibility = View.GONE
                    })
        }

        // 设置他妈的日期
        if (TextUtils.isEmpty(listItem.sendUser)) {
            holder.tvDate.visibility = View.GONE
        } else {
            holder.tvDate.visibility = View.VISIBLE
            holder.tvDate.text = DateUtil.formatTimeForList(listItem.sendTime)
        }

        holder.tvImportant.visibility = View.VISIBLE
        holder.tvImportant.text = listItem.important
        if (TextUtils.equals(listItem.important, "特急")) {
            holder.tvImportant.setBackgroundResource(R.drawable.bg_approval_search_important_extra_urgen)
            holder.tvImportant.setTextColor(Color.parseColor("#FF3B2F"))
        } else if (TextUtils.equals(listItem.important, "急件") || TextUtils.equals(listItem.important, "加急")) {
            holder.tvImportant.setBackgroundResource(R.drawable.bg_approval_search_important_urgen)
            holder.tvImportant.setTextColor(Color.parseColor("#F28149"))
        } else {
            holder.tvImportant.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (mItemClickListener != null) {
                mItemClickListener.invoke(listItem)
            }
        }
    }

    override fun onChildCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val convertView = LayoutInflater.from(parent?.context).inflate(R.layout.item_approval_search, parent, false)
        return ApprovalSearchViewHolder(convertView)
    }

    override fun getItemId(position: Int) = position.toLong()

    fun hasImpormantApproval(): Boolean {
        if (CommonUtil.isEmptyList(mDataSources)) return false

        for (listItem in mDataSources!!) {
            if (!TextUtils.isEmpty(listItem.level)) return true
        }

        return false
    }

    fun clearDataSources() {
        mDataSources?.clear()
        removeFooterView()
        notifyDataSetChanged()
    }

    private fun deepenOn(text: String): String {
        if (TextUtils.isEmpty(text)) return text
        if (TextUtils.isEmpty(mKeyword)) return text

        val deepenOn = "<font color=\"#28B9FF\">${mKeyword}</font>"
        return text.replace(mKeyword!!, deepenOn)
    }

    class ApprovalSearchViewHolder(convertView: View) : RecyclerView.ViewHolder(convertView) {

        val tvTitle: TextView
        val tvAuthor: TextView
        val tvDate: TextView
        val ivAvatar: ImageView
        val tvImportant: TextView

        init {
            tvTitle = convertView.findViewById(R.id.tvApprovalItemTitle)
            tvAuthor = convertView.findViewById(R.id.tvApprovalItemAuthor)
            tvDate = convertView.findViewById(R.id.tvApprovalItemDate)
            ivAvatar = convertView.findViewById(R.id.ivApprovalAvatar)
            tvImportant = convertView.findViewById(R.id.tvApprovalImportant)
        }
    }
}


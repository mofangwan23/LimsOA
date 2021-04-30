package cn.flyrise.feep.meeting7.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.meeting7.R

/**
 * @author ZYP
 * @since 2018-06-28 17:34
 */
class MeetingAttendeeAdapter(private val context: Context) :
        RecyclerView.Adapter<MeetingAttendeeAdapter.ViewHolder>() {

    private val host = CoreZygote.getLoginUserServices().serverAddress
    private var attendUsers: MutableList<AddressBook>? = null
    private var clickFunc: ((AddressBook) -> Unit)? = null
    private var attendUsersOld: MutableList<AddressBook> = ArrayList()

    fun setClickFunc(func: ((AddressBook) -> Unit)?) {
        this.clickFunc = func
    }

    fun setAttendees(attendees: MutableList<AddressBook>?) {
        this.attendUsers = attendees
        this.notifyDataSetChanged()
    }

    fun setAttendeesOld(attendees: MutableList<AddressBook>?){
        attendUsersOld.addAll(attendees!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.nms_item_meeting_attendee, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = attendUsers!!.get(position)
        holder.tvUserName.text = user.name
        if(attendUsersOld!=null){
            if(position < attendUsersOld.size){
                holder.ivUserAvatar.setAlpha(0.5f)
                holder.itemView.isClickable = false
            }else{
                holder.itemView.setAlpha(1.0f)
                holder.itemView.isClickable = true
                holder.itemView.setOnClickListener { clickFunc?.invoke(user) }
            }
        }
        FEImageLoader.load(context, holder.ivUserAvatar, host + user.imageHref, user.userId, user.name)
    }

    override fun getItemCount(): Int {
        return if (CommonUtil.isEmptyList(attendUsers)) 0 else attendUsers!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var ivUserAvatar: ImageView
        var tvUserName: TextView

        init {
            ivUserAvatar = itemView.findViewById(R.id.nmsIvUserIcon)
            tvUserName = itemView.findViewById(R.id.nmsTvUserName)
        }
    }
}

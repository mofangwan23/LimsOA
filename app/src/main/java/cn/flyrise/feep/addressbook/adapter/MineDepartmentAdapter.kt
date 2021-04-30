package cn.flyrise.feep.addressbook.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import cn.flyrise.feep.R
import cn.flyrise.feep.core.image.loader.FEImageLoader

/**
 * @author ZYP
 * @since 2018-05-22 16:22
 */
class MineDepartmentAdapter(context: Context) : ContactAdapter(context) {

    override fun onChildBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val contactHolder = holder as BaseContactAdapter.ContactViewHolder
        val addressBook = mContacts[position]
        FEImageLoader.load(mContext, contactHolder.ivUserIcon, mHostUrl + addressBook.imageHref,
                addressBook.userId, addressBook.name)
        contactHolder.tvUserName.text = addressBook.name
        val deptName = if (TextUtils.isEmpty(addressBook.deptName)) "" else addressBook.deptName + "-"
        contactHolder.tvUserPosition.text = deptName + addressBook.position

        // 设置 Letter 显示隐藏
        val ch = addressBook.pinyin.toLowerCase().get(0).toInt()
        if (position == 0) {
            contactHolder.tvLetter.visibility = View.VISIBLE
            contactHolder.tvLetter.setText(Character.toUpperCase(ch).toChar() + "")
        } else {
            val preAddressBook = mContacts[position - 1]
            val preCh = preAddressBook.pinyin.toLowerCase().get(0).toInt()
            if (ch == preCh) {
                contactHolder.tvLetter.visibility = View.GONE
            } else {
                contactHolder.tvLetter.visibility = View.VISIBLE
                contactHolder.tvLetter.setText(Character.toUpperCase(ch).toChar() + "")
            }
        }

        contactHolder.ivContactCheck.visibility = if (withSelect) View.VISIBLE else View.GONE


        if (withSelect) {
            if (cannotSelectContacts.contains(addressBook)) {
                if (TextUtils.equals(addressBook.userId, mLoginUser)) {
                    contactHolder.ivContactCheck.visibility = View.GONE
                } else {
                    contactHolder.ivContactCheck.visibility = View.VISIBLE
                    contactHolder.ivContactCheck.setImageResource(if (cannotSelectContacts.contains(addressBook))
                        R.drawable.no_choice
                    else
                        R.drawable.no_select_check)
                }
            } else {
                contactHolder.ivContactCheck.visibility = View.VISIBLE
                contactHolder.ivContactCheck.setImageResource(if (selectedContacts.contains(addressBook))
                    R.drawable.node_current_icon
                else
                    R.drawable.no_select_check)
            }
        }

        holder.itemView.setOnClickListener { view ->
            if (mItemClickListener != null) {
                if (cannotSelectContacts.contains(addressBook)) {
                    return@setOnClickListener
                }
                mItemClickListener.onItemClick(addressBook, position)
            }
        }
    }
}

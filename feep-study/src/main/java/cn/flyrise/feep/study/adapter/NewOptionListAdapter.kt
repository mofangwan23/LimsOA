package cn.flyrise.feep.study.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.study.entity.GetQuestionResponse
import cn.flyrise.study.R

class NewOptionListAdapter(val context: Context?, val optionList: List<GetQuestionResponse.DatalistBean.DBBean>?, val listView: ListView,
                           val qType: String?, val isAnalyse: Boolean, val key: String?, val userAnswer: String?) :
        BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var contentView = convertView
        val holder: ViewHolder
        if (contentView == null) {
            contentView = LayoutInflater.from(parent?.context).inflate(R.layout.stu_question_item_option, parent, false)
            holder = ViewHolder(contentView)
            contentView.tag = holder
        } else {
            holder = contentView.tag as ViewHolder
        }
        if (TextUtils.equals("3", qType)) {
            holder.tvOption.text = optionList!![position].salisa
        } else {
            holder.tvOption.text = optionList!![position].salisa + ". " + optionList!![position].soption
        }

//        if (isAnalyse) {
//            if (TextUtils.equals("2", qType)) {
//                var keyArray = key?.toCharArray()
//                if (keyArray?.size!!>0){
//                    for (i in 0 until keyArray?.size){
//                        if (TextUtils.equals(keyArray[i].toString(), optionList[position].salisa)) {
//                            holder.tvRight.visibility = View.VISIBLE
//                        } else {
//                            holder.tvRight.visibility = View.GONE
//                        }
//                    }
//                }
//            } else {
//                if (TextUtils.equals(key, optionList[position].salisa)) {
//                    holder.tvRight.visibility = View.VISIBLE
//                } else {
//                    holder.tvRight.visibility = View.GONE
//                }
//
//            }
//        }


        updateBackground(position, holder.rlOption)

        return contentView!!
    }

    override fun getItem(position: Int) = optionList?.get(position)

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = optionList?.size ?: 0

    inner class ViewHolder(val convertView: View?) {
        var tvOption: TextView
        var rlOption: RelativeLayout
        var tvRight: TextView
        var tvWrong: TextView

        init {
            tvOption = convertView!!.findViewById(R.id.tv_option) as TextView
            rlOption = convertView.findViewById(R.id.rl_option) as RelativeLayout
            tvRight = convertView.findViewById(R.id.tv_riht) as TextView
            tvWrong = convertView.findViewById(R.id.tv_wrong) as TextView
        }
    }

    private fun updateBackground(position: Int, view: View) {
        val backgroundId: Int
        if (listView.isItemChecked(position)) {
            backgroundId = R.drawable.stu_shape_rg_on
        } else {
            backgroundId = R.drawable.stu_shape_rg_off
        }
        val background = context!!.getResources().getDrawable(backgroundId)
        view.setBackgroundDrawable(background)
    }

    fun updateBackgroundAndStatus(position: Int, holder: ViewHolder, key: String?, userAnswer: String?) {
        val backgroundId: Int
        if (listView.isItemChecked(position)) {
            backgroundId = R.drawable.stu_shape_rg_on
        } else {
            backgroundId = R.drawable.stu_shape_rg_off
        }

        if (TextUtils.equals(key, userAnswer)) {
            holder.tvRight.visibility = View.VISIBLE
            holder.tvWrong.visibility = View.GONE
        }
        val background = context!!.getResources().getDrawable(backgroundId)
        holder.rlOption.setBackgroundDrawable(background)
    }

    fun onWindowFocusChanged(hasFocus: Boolean) {

    }


}
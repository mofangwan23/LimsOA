package cn.flyrise.feep.main.modules

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.function.AppMenu
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.core.function.IPreDefinedModuleRepository
import cn.flyrise.feep.core.image.loader.FEImageLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * @author 社会主义接班人
 * @since 2018-07-26 17:25
 */
class ShortCutAdapter(val context: Context?, val shortCuts: List<AppMenu>?) : BaseAdapter() {

    private val host: String
    private val repository: IPreDefinedModuleRepository

    init {
        host = CoreZygote.getLoginUserServices().serverAddress
        repository = FunctionManager.getDefinedModuleRepository()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var contentView = convertView
        var holder: ViewHolder
        if (contentView == null) {
            contentView = LayoutInflater.from(parent?.context).inflate(R.layout.item_main_shortcut, parent, false)
            holder = ViewHolder(contentView)
            contentView.tag = holder
        } else {
            holder = contentView.tag as ViewHolder
        }

        val menu = shortCuts?.get(position)!!
        holder.tvQuick.text = menu.menu
        holder.ivQuick.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        if (TextUtils.isEmpty(menu.icon)) {
            holder.ivQuick.setImageResource(menu.imageRes)
        } else {
            var defaultImageRes = menu.imageRes
            val sc = FunctionManager.getDefinedModuleRepository().getShortCut(menu.menuId)
            if (sc != null) {
                defaultImageRes = sc.imageRes
            }
            val imageUrl = if (menu.icon.startsWith(host)) menu.icon else host + menu.icon
            FEImageLoader.load(parent?.context, holder.ivQuick, imageUrl, defaultImageRes, object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    holder.ivQuick.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                    return false;
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    holder.ivQuick.clearColorFilter()
                    return false;
                }

            })
        }
        return contentView!!
    }

    override fun getItem(position: Int) = shortCuts?.get(position)

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = shortCuts?.size ?: 0

    inner class ViewHolder(val convertView: View) {
        val ivQuick: ImageView
        val tvQuick: TextView

        init {
            ivQuick = convertView.findViewById(R.id.ivQuick)
            tvQuick = convertView.findViewById(R.id.tvQuick)
        }

    }

}
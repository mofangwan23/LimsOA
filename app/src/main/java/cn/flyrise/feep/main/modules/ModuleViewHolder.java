package cn.flyrise.feep.main.modules;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2017-05-17 10:44
 */
public class ModuleViewHolder {

    public View layoutModule;
    public ImageView ivIcon;
    public TextView tvName;
    public ImageView ivOperator;

    public ModuleViewHolder(View convertView) {
        ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
        tvName = (TextView) convertView.findViewById(R.id.tvText);
        ivOperator = (ImageView) convertView.findViewById(R.id.ivOperator);
        layoutModule = convertView.findViewById(R.id.layoutModule);
    }

}

package com.hyphenate.easeui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojicon.Type;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import java.util.List;


public class EmojiconGridAdapter extends ArrayAdapter<EaseEmojicon>{

    private Type emojiconType;


    public EmojiconGridAdapter(Context context, int textViewResourceId, List<EaseEmojicon> objects, EaseEmojicon.Type emojiconType) {
        super(context, textViewResourceId, objects);
        this.emojiconType = emojiconType;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            if(emojiconType == Type.BIG_EXPRESSION){
                convertView = View.inflate(getContext(), R.layout.ease_row_big_expression, null);
            }else{
                convertView = View.inflate(getContext(), R.layout.ease_row_expression, null);
            }
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_expression);
        TextView textView = (TextView) convertView.findViewById(R.id.tv_name);
        EaseEmojicon emojicon = getItem(position);
        if(textView != null && emojicon.getName() != null){
            textView.setText(emojicon.getName());
        }

        if(EaseSmileUtils.DELETE_KEY.equals(emojicon.getEmojiText())){
            imageView.setImageResource(R.drawable.ease_delete_expression);
        }else{
            if(emojicon.getIcon() != 0){
                FEImageLoader.load(getContext(),imageView,emojicon.getIcon());
            }else if(emojicon.getIconPath() != null){
                FEImageLoader.load(getContext(),imageView,emojicon.getIconPath(),R.drawable.ease_default_expression);
            }
        }

        return convertView;
    }
    
}

package cn.flyrise.feep.email.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016/6/29 14:11
 */
public class ParticipantsAdapter extends BaseAdapter {

    private ArrayList<Participants> datalist;

    private XOnClickListener onClickListener;

    public ParticipantsAdapter(ArrayList<Participants> datalist, XOnClickListener onClickListener) {
        this.datalist = datalist;
        this.onClickListener = onClickListener;
    }

    @Override public int getCount() {
        return datalist.size();
    }

    @Override public Object getItem(int position) {
        return datalist.get(position);
    }

    @Override public long getItemId(int position) {
        return 0;
    }

    @Override public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.email_item_participants, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Participants participants = datalist.get(position);
        if ("发件人".equals(participants.name) || "收件人".equals(participants.name) || "抄送人".equals(participants.name)) {
            holder.contentlayout.setVisibility(View.GONE);
            holder.tvTitle.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(participants.name);
            convertView.setEnabled(false);
            return convertView;
        }

        holder.contentlayout.setVisibility(View.VISIBLE);
        holder.tvTitle.setVisibility(View.GONE);
        holder.tvName.setText(participants.name);

        if (participants.id.contains("@")) {
            holder.tvEmail.setVisibility(View.VISIBLE);
            holder.tvEmail.setText(participants.id);
        }
        else {
            holder.tvEmail.setVisibility(View.GONE);
        }

        holder.ivIcon.setVisibility(View.VISIBLE);
        convertView.setEnabled(true);
        holder.ivIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onEmailIconClick(participants);
                }
            }
        });

        return convertView;
    }

    private class ViewHolder {
        public TextView tvTitle;
        public View contentlayout;
        public TextView tvName;
        public TextView tvEmail;
        public ImageView ivIcon;

        public ViewHolder(View convertView) {
            tvTitle = (TextView) convertView.findViewById(R.id.tvParticipantsTitle);
            tvName = (TextView) convertView.findViewById(R.id.tvParticipantsName);
            tvEmail = (TextView) convertView.findViewById(R.id.tvParticipantsEmail);
            ivIcon = (ImageView) convertView.findViewById(R.id.ivEmail);
            contentlayout = convertView.findViewById(R.id.content_layout);
        }
    }

    public static class Participants {
        public String name;
        public String id;

        public Participants(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }

    public interface XOnClickListener {
        void onEmailIconClick(Participants participants);

        void onNameClick(Participants participants);
    }

}

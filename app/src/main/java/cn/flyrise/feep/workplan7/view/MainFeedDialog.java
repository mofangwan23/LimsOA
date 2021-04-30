package cn.flyrise.feep.workplan7.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016-09-29 16:33
 */
public class MainFeedDialog extends DialogFragment {

    private String mMainFeed;
    private String mCopyToFeed;
    private String mNoticeFeed;
    private String mStartTime;
    private String mEndTime;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View bubbleView = inflater.inflate(R.layout.workplan_bubblewindow, container, false);
        final TextView receiveUserstv = (TextView) bubbleView.findViewById(R.id.workplan_receiveUsers);
        if (!TextUtils.isEmpty(mMainFeed)) {
            bubbleView.findViewById(R.id.workplan_receiveUsers_layout).setVisibility(View.VISIBLE);
            receiveUserstv.setText(mMainFeed);
        }
        else {
            bubbleView.findViewById(R.id.workplan_receiveUsers_layout).setVisibility(View.GONE);
        }

        final TextView CCUserstv = (TextView) bubbleView.findViewById(R.id.workplan_ccusers);
        if (!TextUtils.isEmpty(mCopyToFeed)) {
            bubbleView.findViewById(R.id.workplan_ccusers_layout).setVisibility(View.VISIBLE);
            CCUserstv.setText(mCopyToFeed);
        }
        else {
            bubbleView.findViewById(R.id.workplan_ccusers_layout).setVisibility(View.GONE);
        }

        final TextView noticeUserstv = (TextView) bubbleView.findViewById(R.id.workplan_noticeUsers);
        if (!TextUtils.isEmpty(mNoticeFeed)) {
            bubbleView.findViewById(R.id.workplan_noticeUsers_layout).setVisibility(View.VISIBLE);
            noticeUserstv.setText(mNoticeFeed);
        }
        else {
            bubbleView.findViewById(R.id.workplan_noticeUsers_layout).setVisibility(View.GONE);
        }

        final TextView startTimeTv = (TextView) bubbleView.findViewById(R.id.workplan_start_time);
        startTimeTv.setText(mStartTime);

        final TextView endTimeTv = (TextView) bubbleView.findViewById(R.id.workplan_end_time);
        endTimeTv.setText(mEndTime);

        return bubbleView;
    }

    public void setMainFeed(String mainFeed) {
        this.mMainFeed = mainFeed;
    }

    public void setCopyToFeed(String CopyToFeed) {
        this.mCopyToFeed = CopyToFeed;
    }

    public void setNoticeFeed(String noticeFeed) {
        this.mNoticeFeed = noticeFeed;
    }

    public void setStartTime(String startTime) {
        this.mStartTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.mEndTime = endTime;
    }

    public static class Builder {
        private String mainFeed;
        private String copyToFeed;
        private String noticeFeed;
        private String startTime;
        private String endTime;

        public Builder mainFeed(String mainFeed) {
            this.mainFeed = mainFeed;
            return this;
        }

        public Builder copyToFeed(String copyToFeed) {
            this.copyToFeed = copyToFeed;
            return this;
        }

        public Builder noticeFeed(String noticeFeed) {
            this.noticeFeed = noticeFeed;
            return this;
        }

        public Builder startTime(String startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(String endTime) {
            this.endTime = endTime;
            return this;
        }

        public MainFeedDialog build() {
            MainFeedDialog dialog = new MainFeedDialog();
            dialog.setMainFeed(mainFeed);
            dialog.setCopyToFeed(copyToFeed);
            dialog.setNoticeFeed(noticeFeed);
            dialog.setStartTime(startTime);
            dialog.setEndTime(endTime);
            return dialog;
        }
    }
}

package cn.flyrise.feep.robot.adapter.holder;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.robot.adapter.PlayVoiceItemAdapter;
import cn.flyrise.feep.robot.contract.RobotEntityContractKt;
import cn.flyrise.feep.robot.entity.RobotResultItem;
import cn.flyrise.feep.robot.manager.PlayVoiceManager;
import cn.flyrise.feep.robot.util.LMediaPlayerUtil;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-12-4-14:06.
 */

public class PlayVoiceViewHolder extends RobotViewHodler implements
		PlayVoiceItemAdapter.OnClickeItemListener
		, PlayVoiceManager.OnPlayVoiceListener {

	private LinearLayout mHeadLayout;
	private TextView mTvHeadTitle;
	private TextView mTvHeadNums;

	private LinearLayout mConLayout;
	private ImageView mConIcon;
	private TextView mTvConTitle;
	private TextView mTvConText;
	private TextView mTvConNote;
	private TextView mTvConMore;

	private ImageView mImgPlayLast;
	private ImageView mImgPlayState;
	private ImageView mImgPlayNext;

	private SeekBar mSeekBar;
	private TextView mTvProgress;

	private RecyclerView mRecyclerView;
	private PlayVoiceItemAdapter mAdapter;

	private Context mContext;
	private String service;

	private PlayVoiceManager mPlayManager;

	public PlayVoiceViewHolder(View itemView, Context context) {
		super(itemView);
		this.mContext = context;
		mHeadLayout = itemView.findViewById(R.id.head_layout);
		mTvHeadTitle = itemView.findViewById(R.id.music_name);
		mTvHeadNums = itemView.findViewById(R.id.music_nums);

		mConLayout = itemView.findViewById(R.id.conent_layout);
		mConIcon = itemView.findViewById(R.id.conent_icon);
		mTvConTitle = itemView.findViewById(R.id.content_title);
		mTvConText = itemView.findViewById(R.id.content_text);
		mTvConNote = itemView.findViewById(R.id.tv_note);
		mTvConMore = itemView.findViewById(R.id.tv_more);

		mImgPlayLast = itemView.findViewById(R.id.play_last);
		mImgPlayState = itemView.findViewById(R.id.play_state);
		mImgPlayNext = itemView.findViewById(R.id.play_next);

		mTvProgress = itemView.findViewById(R.id.seek_progress);
		mSeekBar = itemView.findViewById(R.id.seek_bar);
		mRecyclerView = itemView.findViewById(R.id.recycler_view);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
	}

	public void setPlayVoiceViewHolder() {
		service = item.service;
		if (CommonUtil.isEmptyList(item.results)) {
			return;
		}
		mPlayManager = new PlayVoiceManager(item.results, this);
		initView(item.results);
		mPlayManager.newPlayingVoice();
	}

	private void initView(List<RobotResultItem> results) {
		if (mAdapter == null) {
			mAdapter = new PlayVoiceItemAdapter(results, this);
			mRecyclerView.setAdapter(mAdapter);
		}
		else {
			mAdapter.setResultItems(results);
		}
		mTvConMore.setOnClickListener(v -> {
			mTvConNote.setMaxLines(Integer.MAX_VALUE);
			mTvConMore.setVisibility(View.GONE);
		});
		mImgPlayState.setImageResource(R.drawable.robot_play_voice_stop);
		mImgPlayLast.setOnClickListener(v -> mPlayManager.lastPlaying());
		mImgPlayState.setOnClickListener(v -> statePlaying());
		mImgPlayNext.setOnClickListener(v -> mPlayManager.nextPlaying());
	}

	//开始或暂停播放
	private void statePlaying() {
		if (LMediaPlayerUtil.getInstance().getStatePause()) {
			mPlayManager.statePlaying(true);
			mImgPlayState.setImageResource(R.drawable.robot_play_voice_start);
		}
		else {
			mPlayManager.statePlaying(false);
			mImgPlayState.setImageResource(R.drawable.robot_play_voice_stop);
		}
	}

	@Override
	public void onProgress(int progress) {
		mSeekBar.setProgress(progress);
		mTvProgress.setText(progress + "%");
	}

	@Override
	public void setRefreshView(RobotResultItem item) {
		mConIcon.setFocusableInTouchMode(true);
		mConIcon.requestFocus();
		if (TextUtils.isEmpty(service) || TextUtils.equals(service, RobotEntityContractKt.joke)) {
			mHeadLayout.setVisibility(View.VISIBLE);
			mConLayout.setVisibility(View.GONE);
			mTvHeadTitle.setText(item.title);
			mTvHeadNums.setText(mPlayManager.getIndex() + "/" + mPlayManager.getResultItemSize());
		}
		else {
			mHeadLayout.setVisibility(View.GONE);
			mConLayout.setVisibility(View.VISIBLE);
			mTvConTitle.setText(item.content);
			mTvConText.setText(item.title);
			mTvConNote.setText(item.note);
			setNoteMore();
			mTvConTitle.setVisibility(TextUtils.isEmpty(item.content) ? View.GONE : View.VISIBLE);
			mTvConText.setVisibility(TextUtils.isEmpty(item.title) ? View.GONE : View.VISIBLE);
			mTvConNote.setVisibility(TextUtils.isEmpty(item.note) ? View.GONE : View.VISIBLE);
			FEImageLoader.load(mContext, mConIcon, item.imgUrl);
		}
		mAdapter.setSelected(mPlayManager.getIndex() - 1);
	}

	@Override
	public void refreshStatePlay() {
		mImgPlayState.setImageResource(R.drawable.robot_play_voice_stop);
	}

	private void setNoteMore() {
		if (mPlayManager.isSelectedItem()) {
			return;
		}
		mTvConNote.setMaxLines(Integer.MAX_VALUE);
		mTvConMore.setVisibility(View.GONE);
		mTvConNote.post(() -> {
			if (mTvConNote.getLineCount() > 2) {
				mTvConNote.setMaxLines(2);
				mTvConNote.setEllipsize(TextUtils.TruncateAt.END);
				mTvConMore.setVisibility(View.VISIBLE);
			}
			else {
				mTvConMore.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void onDestroy() {
		mPlayManager.onDestroy();
	}

	@Override
	public void clickeItem(int position) {
		mPlayManager.clickeItem(position);
	}

}

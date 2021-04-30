package cn.flyrise.feep.main;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.main.message.MessageConstant;
import cn.flyrise.feep.main.message.MessageVO;
import cn.flyrise.feep.utils.Patches;
import com.drop.DropCover;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-04-01 10:45
 */
public class NewMainMessageHeadView extends LinearLayout {

	private NewMainMessageItemView mTaskMessage;//任务消息
	private NewMainMessageItemView mUnReadMessage;//待阅消息
	private NewMainMessageItemView mGroupMessage;//圈子消息
	private NewMainMessageItemView mSystemMessage;//系统消息
	private List<MessageVO> mMessageVOs;
	private int circleCount;//朋友圈未读消息
	private MessageVO circleMessage;

	public NewMainMessageHeadView(Context context) {
		this(context, null);
	}

	public NewMainMessageHeadView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NewMainMessageHeadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		inflate(context, R.layout.view_new_main_message_head, this);
		mTaskMessage = findViewById(R.id.taskMessage);
		mUnReadMessage = findViewById(R.id.unReadMessage);
		mGroupMessage = findViewById(R.id.groupMessage);
		mSystemMessage = findViewById(R.id.systemMessage);
	}

	public void setDataSource(List<MessageVO> messageVOs) {
		if (CommonUtil.isEmptyList(messageVOs)) {
			return;
		}
		this.mMessageVOs = messageVOs;
		boolean hasAssociate = FunctionManager.hasPatch(Patches.PATCH_GROUP_MESSAGE);
		mGroupMessage.setVisibility(hasAssociate ? View.GONE : View.VISIBLE);

		for (int i = 0; i < messageVOs.size(); i++) {
			MessageVO messageVO = messageVOs.get(i);
			switch (messageVO.getCategory()) {
				case MessageConstant.MISSION:
					mTaskMessage.setMessageVO(messageVO);
					break;
				case MessageConstant.NOTIFY:
					mUnReadMessage.setMessageVO(messageVO);
					break;
				case MessageConstant.CIRCLE:
					mGroupMessage.setMessageVO(messageVO);
					circleCount = CommonUtil.parseInt(messageVO.getBadge());
					circleMessage = messageVO;
					break;
				case MessageConstant.SYSTEM:
					mSystemMessage.setMessageVO(messageVO);
					break;
			}
		}
	}

	//监听气泡拖动
	public void setOnDragCompeteListener(OnDragCompeteListener listener) {
		if (mTaskMessage != null) {
			mTaskMessage.setOnDragCompeteListener(new DropCover.OnDragCompeteListener() {
				@Override public void onDrag() {
					listener.onDragCompete(mTaskMessage.getMessageVO());
				}

				@Override public void onDownDrag(boolean isDownDrag) {
					listener.onDownDrag(isDownDrag);
				}
			});
		}
		if (mUnReadMessage != null) {
			mUnReadMessage.setOnDragCompeteListener(new DropCover.OnDragCompeteListener() {
				@Override public void onDrag() {
					listener.onDragCompete(mUnReadMessage.getMessageVO());
				}

				@Override public void onDownDrag(boolean isDownDrag) {
					listener.onDownDrag(isDownDrag);
				}
			});
		}
		if (mGroupMessage != null) {
			mGroupMessage.setOnDragCompeteListener(new DropCover.OnDragCompeteListener() {
				@Override public void onDrag() {
					listener.onDragCompete(mGroupMessage.getMessageVO());
				}

				@Override public void onDownDrag(boolean isDownDrag) {
					listener.onDownDrag(isDownDrag);
				}
			});
		}
		if (mSystemMessage != null) {
			mSystemMessage.setOnDragCompeteListener(new DropCover.OnDragCompeteListener() {
				@Override public void onDrag() {
					listener.onDragCompete(mSystemMessage.getMessageVO());
				}

				@Override public void onDownDrag(boolean isDownDrag) {
					listener.onDownDrag(isDownDrag);
				}
			});
		}

	}

	public void setOnTaskMessageClickListener(OnMessageItemClickListener listener) {
		if (mTaskMessage != null) {
			mTaskMessage.setOnClickListener(view -> {
				if (listener != null) {
					listener.onMessageItemClick(mTaskMessage.getMessageVO());
				}
			});
		}
	}

	public void setOnUnReadMessageClickListener(OnMessageItemClickListener listener) {
		if (mUnReadMessage != null) {
			mUnReadMessage.setOnClickListener(view -> {
				if (listener != null) {
					listener.onMessageItemClick(mUnReadMessage.getMessageVO());
				}
			});
		}
	}

	public void setOnGroupMessageClickListener(OnMessageItemClickListener listener) {
		if (mGroupMessage != null) {
			mGroupMessage.setOnClickListener(view -> {
				if (listener != null) {
					listener.onMessageItemClick(mGroupMessage.getMessageVO());
				}
			});
		}
	}

	public void setOnSystemMessageClickListener(OnMessageItemClickListener listener) {
		if (mSystemMessage != null) {
			mSystemMessage.setOnClickListener(view -> {
				if (listener != null) {
					listener.onMessageItemClick(mSystemMessage.getMessageVO());
				}
			});
		}
	}

	public int getUnReadMessageCount() {
		if (CommonUtil.isEmptyList(mMessageVOs)) return 0;
		int totalCount = 0;
		for (MessageVO vo : mMessageVOs) {
			if (isGroupMessage(vo.getCategory())) continue;
			if (vo.getBadge() != null) {
				int number = Integer.valueOf(vo.getBadge());
				if (number > 0) totalCount += number;
			}
		}
		return totalCount;
	}

	//圈子消息，并且关闭了补丁列表才会显示数量
	private boolean isGroupMessage(String category) {
		return TextUtils.equals(MessageConstant.CIRCLE, category) && FunctionManager.hasPatch(Patches.PATCH_GROUP_MESSAGE);
	}

	public int getCircleUnReadMessageCount() {

		return circleCount;
	}

	public void setCircleCount(int circleCount) {
		this.circleCount = circleCount;
	}

	public MessageVO getCircleMessage() {
		return circleMessage;
	}


	public interface OnMessageItemClickListener {

		void onMessageItemClick(MessageVO messageVO);
	}

	public interface OnDragCompeteListener {

		void onDragCompete(MessageVO messageVO);

		void onDownDrag(boolean isDownDrag);
	}
}

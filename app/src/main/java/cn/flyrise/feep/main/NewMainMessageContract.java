package cn.flyrise.feep.main;

import com.hyphenate.chat.EMConversation;

import java.util.List;

import cn.flyrise.feep.main.message.MessageVO;

/**
 * @author ZYP
 * @since 2017-04-01 14:04
 */
public interface NewMainMessageContract {

	interface IView {

		String ALL_MESSAGE_READ = "A";//标记全部消息为已读

		void onMessageLoadSuccess(List<MessageVO> messageVOs);

		void onConversationLoadSuccess(List<EMConversation> messages);

		void showLoading();

		void hideLoading();

		void onCircleMessageIdListSuccess(List<String> messageIds);//获取朋友圈消息id

		void onCircleMessageReadSuccess();//朋友圈消息已读成功

	}

	interface IPresenter {

		void start();

		void fetchMessageList();

		void fetchConversationList();

		void allMessageListRead(String categorys);

		void requestCircleMessageList(int totalCount, int pageNumber);

		void circleMessageListRead(List<String> messageIds);

	}

}

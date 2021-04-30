package cn.flyrise.feep.protocol;

import static cn.flyrise.feep.dbmodul.table.ConversationSetting_Table.convId;

import android.text.TextUtils;
import cn.flyrise.feep.core.services.IConvSTService;
import cn.flyrise.feep.dbmodul.table.ConversationSetting;
import com.raizlabs.android.dbflow.sql.language.SQLite;

/**
 * @author ZYP
 * @since 2017-08-11 09:44
 */
public class FeepConvSTService implements IConvSTService {

	@Override
	public boolean isSilence(String convId) {
		ConversationSetting convST = getConvST(convId);
		return convST != null && convST.silence == 1;
	}

	@Override
	public void makeConversationSilence(String convId, String conversation) {
		ConversationSetting convST = getConvST(convId);
		if (convST == null) {
			insertConvST(convId, conversation, 1);
		}
		else {
			convST.silence = 1;
			convST.update();
		}
	}

	@Override
	public void makeConversationActive(String convId, String conversation) {
		ConversationSetting convST = getConvST(convId);
		if (convST == null) {
			insertConvST(convId, conversation, 0);
		}
		else {
			convST.conversation = conversation;
			convST.silence = 0;
			convST.update();
		}
	}

	@Override
	public void makeConversationGroud(String convId, String conversation) {
		ConversationSetting convST = getConvST(convId);
		if (convST == null) {
			insertConvST(convId, conversation, 0);
		}
		else {
			convST.conversation = conversation;
			convST.update();
		}
	}

	@Override
	public String getCoversationName(String convId) {
		ConversationSetting convST = getConvST(convId);
		return convST != null && !TextUtils.isEmpty(convST.conversation) ? convST.conversation : convId;
	}

	@Override
	public boolean coversationExist(String convId) {
		ConversationSetting convST = getConvST(convId);
		return convST != null;
	}


	private void insertConvST(String convId, String conversation, int silence) {
		ConversationSetting convST = new ConversationSetting();
		convST.silence = silence;
		convST.convId = convId;
		convST.conversation = conversation;
		convST.insert();
	}

	/**
	 * 获取指定 ID 的会话
	 */
	private ConversationSetting getConvST(String conversationId) {
		return SQLite.select().from(ConversationSetting.class).where(convId.eq(conversationId)).querySingle();
	}
}

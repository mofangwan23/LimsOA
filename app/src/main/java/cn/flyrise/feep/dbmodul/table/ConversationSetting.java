package cn.flyrise.feep.dbmodul.table;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * @author ZYP
 * @since 2017-08-11 09:15
 * 用于保存环信的会话设置
 */
@Table(database = FeepOADataBase.class)
public class ConversationSetting extends BaseModel {

	/**
	 * 主键：自增
	 */
	@PrimaryKey(autoincrement = true) public long id;

	/**
	 * 会话 Id(包括群聊 Id，单聊 Id)
	 */
	@Column public String convId;

	/**
	 * 会话名称
	 */
	@Column public String conversation;

	/**
	 * 消息免打扰：默认不开启
	 * 0：不开启
	 * 1：针对当前会话开启消息免打扰
	 */
	@Column public int silence;
}

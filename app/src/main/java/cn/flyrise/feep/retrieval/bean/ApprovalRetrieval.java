package cn.flyrise.feep.retrieval.bean;

import static cn.flyrise.feep.core.common.X.RequestType.Done;
import static cn.flyrise.feep.core.common.X.RequestType.Sended;
import static cn.flyrise.feep.core.common.X.RequestType.ToDo;
import static cn.flyrise.feep.core.common.X.RequestType.ToDoDispatch;
import static cn.flyrise.feep.core.common.X.RequestType.ToDoNornal;
import static cn.flyrise.feep.core.common.X.RequestType.ToDoRead;

import android.text.TextUtils;

/**
 * @author ZYP
 * @since 2018-05-07 17:08
 */
public class ApprovalRetrieval extends BusinessRetrieval {

	public String type;         // 类型 0：待办 1：已办 4：已发

	public int getRequestType() {
		if (TextUtils.equals(type, "0")) return ToDo;             // 待办
		if (TextUtils.equals(type, "1")) return Done;             // 已办
		if (TextUtils.equals(type, "4")) return Sended;           // 已发
		if (TextUtils.equals(type, "23")) return ToDoDispatch;    // 急件
		if (TextUtils.equals(type, "24")) return ToDoNornal;      // 平件
		if (TextUtils.equals(type, "25")) return ToDoRead;        // 阅件
		return ToDo;
	}

}

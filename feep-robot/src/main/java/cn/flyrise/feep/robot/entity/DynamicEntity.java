package cn.flyrise.feep.robot.entity;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.util.RobotFilterChar;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 新建：陈冕;
 * 日期： 2017-11-24-14:17.
 * 动态实体操作
 */

public class DynamicEntity {

	private static final String DIMENSION_NAME = "contacts"; //讯飞平台上定义的维度名
	private String DIMENSION_VAULE; //讯飞平台上定义的维度值
	private static final String DIMENSION_AOTU_NAME = "userName"; //动态实体主字段
	private static final String DIMENSION_RES = "MUSICFEEP.person_name"; //资源名称

	private static final String[] default_contacts = {"小飞", "我"};

	private Context mContext;

	private int subNum = 0;

	public DynamicEntity(Context context) {
		mContext = context;
		DIMENSION_VAULE = TextUtils.isEmpty(DevicesUtil.getDeviceUniqueId()) ? "001" : DevicesUtil.getDeviceUniqueId();
	}

	//上传动态实体
	public AIUIMessage postSyncSchemaContact() {
		return new RobotAiuiMessage.Builder()
				.setMessageType(AIUIConstant.CMD_SYNC)
				.setArg1(AIUIConstant.SYNC_DATA_SCHEMA)
				.setArg2(0)
				.setParams("")
				.setData(uploadDynamic(getContacts())).build().create();
	}

	//查询上传结果
	public AIUIMessage searchPostContact() {
		return new RobotAiuiMessage.Builder()
				.setMessageType(AIUIConstant.CMD_QUERY_SYNC_STATUS)
				.setArg1(AIUIConstant.SYNC_DATA_SCHEMA)
				.setArg2(0)
				.setParams(searchParamsJson().toString())
				.setData(null).build().create();
	}

	//动态实体生效
	public AIUIMessage takeEffect() {
		return new RobotAiuiMessage.Builder()
				.setMessageType(AIUIConstant.CMD_SET_PARAMS)
				.setArg1(0)
				.setArg2(0)
				.setParams(takeEffectData(DIMENSION_VAULE))
				.setData(null).build().create();
	}

	private String takeEffectData(String contacts) {
		StringBuffer sb = new StringBuffer();
		sb.append("{\"audioparams\": {\"pers_param\": \"");
		sb.append(persParam(contacts));
		sb.append("\"}}");

		return sb.toString();
	}

	//uid的值，为contacts的key
	private String persParam(String contactsValue) {
		StringBuffer sb = new StringBuffer();
		sb.append("{\\\"appid\\\":\\\"");
		sb.append(mContext.getResources().getString(R.string.app_id));
		sb.append("\\\",\\\"uid\\\":\\\"");
		sb.append(DIMENSION_NAME);
		sb.append("\\\",\\\"");
		sb.append(DIMENSION_NAME);
		sb.append("\\\":\\\"");
		sb.append(contactsValue);
		sb.append("\\\"}\\\"}}");
		return sb.toString();
	}

	private JSONObject searchParamsJson() {
		String mSyncSid = SpUtil.get("SYNC_SID", "");
		if (TextUtils.isEmpty(mSyncSid)) {
			return null;
		}

		JSONObject paramsJson = new JSONObject();
		try {
			return paramsJson.put("sid", mSyncSid);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	//获取前一万人名字
	private List<String> getContacts() {
		List<AddressBook> addressBooks = CoreZygote.getAddressBookServices().queryAllAddressBooks();
		if (addressBooks == null) {
			return null;
		}
		List<String> contacts = new ArrayList<>();
		contacts.addAll(Arrays.asList(default_contacts));
		try {
			for (AddressBook addressBook : addressBooks) {
				if (addressBook == null || TextUtils.isEmpty(addressBook.name)) {
					continue;
				}
				if (subNum > 10000) {
					break;
				}
				if (!RobotFilterChar.isChinese(addressBook.name)) {
					continue;
				}
				contacts.add(addressBook.name);
				subNum++;
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return contacts;
	}

	private byte[] uploadDynamic(List<String> contacts) {
		if (CommonUtil.isEmptyList(contacts)) {
			return null;
		}
		StringBuffer menuItemData = new StringBuffer();
		for (String item : contacts) {
			menuItemData.append(String.format("{\"" + DIMENSION_AOTU_NAME + "\": \"%s\"}\n", item));
		}
		byte[] bytes = menuItemData.toString().getBytes();
		JSONObject syncSchemaJson = new JSONObject();
		JSONObject paramJson = new JSONObject();
		try {
			paramJson.put("appid", mContext.getResources().getString(R.string.app_id));
			paramJson.put("id_name", DIMENSION_NAME);
			paramJson.put("res_name", DIMENSION_RES);
			paramJson.put("id_value", DIMENSION_VAULE);
			syncSchemaJson.put("param", paramJson);
			syncSchemaJson.put("data", Base64.encodeToString(bytes
					, Base64.DEFAULT | Base64.NO_WRAP));
			// 传入的数据一定要为utf-8编码
			FELog.i("-->>>>动态实体：" + GsonUtil.getInstance().toJson(syncSchemaJson));
			return syncSchemaJson.toString().getBytes("utf-8");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}

package cn.flyrise.feep.core.network.entry;

import cn.flyrise.feep.core.network.request.ResponseContent;
import com.google.gson.annotations.SerializedName;

public class RsaResponse extends ResponseContent {

	@SerializedName("IsSuccess")
	public boolean isSuccess;
	@SerializedName("Msg")
	public String msg;
	@SerializedName("Data")
	public RsaPublickey data;

	public class RsaPublickey {

		public String publicKey;
	}


}

package cn.flyrise.feep.core.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * @author ZYP
 * @since 2017-02-07 15:52
 */
public class GsonUtil {

	private static GsonUtil sInstance;

	private Gson mGson;

	private GsonUtil() {
		mGson = new GsonBuilder().enableComplexMapKeySerialization().create();
	}

	public static GsonUtil getInstance() {
		if (sInstance == null) {
			sInstance = new GsonUtil();
		}
		return sInstance;
	}

	public Gson getGson() {
		return mGson;
	}

	public String toJson(Object object) {
		return mGson.toJson(object);
	}

	public String toJson(Object object, Type type) {
		return mGson.toJson(object, type);
	}

	public <T> T fromJson(String json, Type type) {
		return mGson.fromJson(json, type);
	}

	public <T> T fromJson(String json, Class<T> clazz) {
		return mGson.fromJson(json, clazz);
	}

}

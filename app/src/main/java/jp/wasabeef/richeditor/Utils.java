package jp.wasabeef.richeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;
import cn.flyrise.feep.core.CoreZygote;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (C) 2017 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public final class Utils {

	private Utils() throws InstantiationException {
		throw new InstantiationException("This class is not for instantiation");
	}

	public static String toBase64(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] bytes = baos.toByteArray();

		return Base64.encodeToString(bytes, Base64.NO_WRAP);
	}

	public static Bitmap toBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		int width = drawable.getIntrinsicWidth();
		width = width > 0 ? width : 1;
		int height = drawable.getIntrinsicHeight();
		height = height > 0 ? height : 1;

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static Bitmap decodeResource(Context context, int resId) {
		return BitmapFactory.decodeResource(context.getResources(), resId);
	}

	public static long getCurrentTime() {
		return System.currentTimeMillis();
	}

	public static String tryAddHostToImageBeforeEdit(String content) {
		if (TextUtils.isEmpty(content)) {
			return content;
		}
		final String host = CoreZygote.getLoginUserServices().getServerAddress();
		String regex = "<img.*?src=\"(.*?)\".*?(/>|></img>|>)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			String group = matcher.group(1);
			if (group.startsWith("http") || group.startsWith("https")) {
				continue;
			}

			content = content.replace(group, host + group);
		}
		return content;
	}
}

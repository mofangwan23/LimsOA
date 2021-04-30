/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatui.db;

import static com.hyphenate.chatui.db.DBKey.MSG_DELETE_MSG;
import static com.hyphenate.chatui.db.DBKey.MSG_SPEAKER_ON;
import static com.hyphenate.chatui.db.DBKey.MSG_VIBRATE;
import static com.hyphenate.chatui.db.DBKey.MSG_NOTIFY;
import static com.hyphenate.chatui.db.DBKey.MSG_RECEIVE_MSG;
import static com.hyphenate.chatui.db.DBKey.MSG_SETTING_TABLE;
import static com.hyphenate.chatui.db.DBKey.MSG_SILENCE_ET;
import static com.hyphenate.chatui.db.DBKey.MSG_SILENCE_MODE;
import static com.hyphenate.chatui.db.DBKey.MSG_SILENCE_ST;
import static com.hyphenate.chatui.db.DBKey.MSG_SOUND;
import static com.hyphenate.chatui.db.DBKey.MSG_USER_ID;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hyphenate.chatui.utils.EmHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static String DATABASE_NAME;
	private static DbOpenHelper instance;


	private static final String CREATE_MSG_SETTING_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ MSG_SETTING_TABLE + "("
			+ MSG_USER_ID + " TEXT PRIMARY KEY, "           // userID 主键
			+ MSG_RECEIVE_MSG + " INTEGER, "                // 是否接收消息
			+ MSG_NOTIFY + " INTEGER, "                     // 是否显示通知栏
			+ MSG_SOUND + " INTEGER, "                      // 声音
			+ MSG_VIBRATE + " INTEGER, "                    // 震动
			+ MSG_SILENCE_MODE + " INTEGER, "               // 勿扰
			+ MSG_SILENCE_ST + " TEXT, "                    // 勿扰开始时间
			+ MSG_SILENCE_ET + " TEXT,"                     // 勿扰结束时间
			+ MSG_DELETE_MSG + " INTEGER,"                  //退出群聊是否删除群聊
			+ MSG_SPEAKER_ON + " INTEGER);";                //语音消息是否外放

	private DbOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static synchronized void init(Context cxt,String userID) {
		if (instance == null) {
			DATABASE_NAME = userID +"_im.db";
			instance = new DbOpenHelper(cxt);
		}
	}

	public static DbOpenHelper getInstance() {
		if (instance == null) {
			throw new RuntimeException("please init  DbOpenHelper first!");
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_MSG_SETTING_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void closeDB() {
		if (instance != null) {
			try {
				SQLiteDatabase db = instance.getWritableDatabase();
				db.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			instance = null;
		}
	}

}

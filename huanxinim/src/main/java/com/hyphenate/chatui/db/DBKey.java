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

public interface DBKey {
    String MSG_SETTING_TABLE = "MessageSettingTable";
    String MSG_USER_ID = "user_id";
    String MSG_RECEIVE_MSG = "receive_msg";
    String MSG_NOTIFY = "msg_notify";
    String MSG_SOUND = "msg_sound";
    String MSG_VIBRATE = "msg_vibrate";
    String MSG_SILENCE_MODE = "msg_silence_mode";
    String MSG_SILENCE_ST = "msg_silence_start_time";
    String MSG_SILENCE_ET = "msg_silence_end_time";
    String MSG_DELETE_MSG = "msg_delete";
    String MSG_SPEAKER_ON = "msg_speaker_on";
}

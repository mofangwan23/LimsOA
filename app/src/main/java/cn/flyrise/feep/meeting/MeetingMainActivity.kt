package cn.flyrise.feep.meeting

import android.content.Intent
import android.os.Bundle
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.meeting.old.MeetingListActivity
import cn.flyrise.feep.meeting7.ui.MeetingManagerActivity
import cn.flyrise.feep.utils.Patches

/**
 * @author 社会主义接班人
 * @since 2018-08-01 14:15
 */
class MeetingMainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FunctionManager.hasPatch(Patches.PATCH_MEETING_MANAGER)) {
            startActivity(Intent(this, MeetingManagerActivity::class.java))
        } else {
            startActivity(Intent(this, MeetingListActivity::class.java))
        }
        finish()
    }

}
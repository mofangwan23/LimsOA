package cn.flyrise.feep.meeting7

import android.os.Bundle
import cn.flyrise.feep.core.base.component.BaseActivity

/**
 * @author ZYP
 * @since 2018-06-14 16:45
 */
class HomeActivity : BaseActivity() {

    var times = mutableListOf<String>("08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nms_activity_main)

//        findViewById(R.id.btnDate).setOnClickListener {
//            TimeWheelSelectionFragment.newInstance("开始时间", 2018, 5, 27, 17, 30, { selectedHour, selectedMinte ->
//                Toast.makeText(applicationContext, String.format("%02d:%02d", selectedHour, selectedMinte), Toast.LENGTH_SHORT).show()
//            }).show(supportFragmentManager, "Fuck")
//        }


    }

}
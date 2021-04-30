package cn.flyrise.feep.robot.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.robot.R
import kotlinx.android.synthetic.main.robot_quick_promp_fragment.*

/**
 * 新建：陈冕;
 *日期： 2018-7-24-11:45.
 * 快速提示用户
 */

class RobotQuickPrompFragment : Fragment() {

    companion object {
        const val startPromp = 101//开始说话
        const val shortPromp = 102//说话太短
        const val noTalkPromp = 103//没有说话
        const val networkEorror = 104//网络异常
    }

    private var type: Int = startPromp

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.robot_quick_promp_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        showType(type)
    }

    fun showType(type: Int) {
        this.type = type
        prompTitle?.text = when (type) {
            startPromp -> getString(R.string.robot_hint_say_title)
            shortPromp -> getString(R.string.robot_hint_say_short)
            noTalkPromp -> getString(R.string.robot_hint_no_say)
            networkEorror -> getString(R.string.robot_no_opneration)
            else -> " "
        }

        prompSubTitle?.text = when (type) {
            shortPromp -> getString(R.string.robot_hint_say_hint)
            noTalkPromp -> getString(R.string.robot_hint_say_correct)
            networkEorror -> getString(R.string.robot_network_error)
            else -> " "
        }
    }


}

package cn.flyrise.feep.study.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import cn.flyrise.feep.study.common.Key
import cn.flyrise.study.R
import kotlinx.android.synthetic.main.new_fragment_study_base.*

/**
 * 在线学习Fragment
 */

class StudyMainFragment : StudyBaseFragment(){

    override fun initTitleBar() {
        feToolBar.setTitle(R.string.study_on_line)
        feToolBar.setNavigationVisibility(View.GONE)
    }

    override fun initFragments(): MutableList<Fragment> {
        val fragments = mutableListOf<Fragment>()
        fragments.add(TrainBaseFragment.newInstance(1))
        fragments.add(TrainBaseFragment.newInstance(2))
        return fragments
    }

    override fun initFragmentTitles(): MutableList<String> {
        val titles = mutableListOf<String>()
        titles.add("个人培训任务")
        titles.add("已培训任务")
        return titles
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindData()
    }


}
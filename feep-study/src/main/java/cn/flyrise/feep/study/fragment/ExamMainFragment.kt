package cn.flyrise.feep.study.fragment

import android.support.v4.app.Fragment
import android.view.View
import cn.flyrise.study.R
import kotlinx.android.synthetic.main.new_fragment_study_base.*

class ExamMainFragment : StudyBaseFragment() {

    override fun initTitleBar() {
        feToolBar.setTitle(R.string.exam_on_line)
        feToolBar.setNavigationVisibility(View.GONE)
    }

    override fun initFragments(): MutableList<Fragment> {
        val fragments = mutableListOf<Fragment>()
        fragments.add(ExamBaseFragment.newInstance(1))
        fragments.add(ExamBaseFragment.newInstance(2))
        return fragments
    }

    override fun initFragmentTitles(): MutableList<String> {
        val titles = mutableListOf<String>()
        titles.add("我的试卷")
        titles.add("考试记录")
        return titles
    }

}
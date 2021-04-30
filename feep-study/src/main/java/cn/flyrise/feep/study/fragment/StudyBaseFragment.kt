package cn.flyrise.feep.study.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.study.R
import kotlinx.android.synthetic.main.new_fragment_study_base.*

/**
 * 在线学习+在线考试的 BaseFragment
 */
abstract class StudyBaseFragment : Fragment(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
        bindData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.new_fragment_study_base, container, false)
    }

    fun bindData(){
        val titleList = initFragmentTitles()
        val fragments = initFragments()
        if(CommonUtil.nonEmptyList(titleList) && CommonUtil.nonEmptyList(fragments)){
            tabLayout.newTab().text = titleList[0]
            tabLayout.newTab().text = titleList[1]
            viewPager.adapter = BaseFragmentPagerAdapter(childFragmentManager, fragments, titleList)
            viewPager.offscreenPageLimit = fragments.size
            tabLayout.setupWithViewPager(viewPager)
        }
    }

    fun bindView(){
       initTitleBar()
    }


    abstract fun initFragments(): MutableList<Fragment>

    abstract fun initFragmentTitles() : MutableList<String>

    abstract fun initTitleBar()


}

class BaseFragmentPagerAdapter(fm: FragmentManager, val fragments: List<Fragment>, val titles: List<String>) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? = if (CommonUtil.isEmptyList(fragments)) null else fragments[position]
    override fun getCount(): Int = if (CommonUtil.isEmptyList(fragments)) 0 else fragments.size
    override fun getPageTitle(position: Int) = if (CommonUtil.isEmptyList(titles)) super.getPageTitle(position) else titles[position]
}
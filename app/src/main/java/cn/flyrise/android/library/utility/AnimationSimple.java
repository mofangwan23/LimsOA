/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-6
 */
package cn.flyrise.android.library.utility;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * <b>类功能描述：</b><div style="margin-left:40px;margin-top:-10px"> 动画设置器(封装了简单的动画) 升级,改用建造者模式~~ </div>
 *
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class AnimationSimple {
    /**
     * 长时间的动画展示时间
     */
    public static final int DURATIONTIME_1000 = 1000;
    /**
     * 较长的动画展示时间
     */
    public static final int DURATIONTIME_500 = 500;
    /**
     * 较短的动画展示时间
     */
    public static final int DURATIONTIME_300 = 300;
    /**
     * 极短的动画展示时间
     */
    public static final int DURATIONTIME_100 = 100;

    /*--此工具类无需实例化--*/
    private AnimationSimple () {
    }

    /**
     * 自身现在位置移动至新位置,两点之间移动动画
     *
     * @param view         需要绑定动画的控件,or Null
     * @param durationTime 动画持续时间
     * @param distanceX    位移X
     * @param distanceY    位移Y
     * @return 移动动画
     */
    public static TranslateAnimation move (View view, int durationTime, float distanceX, float distanceY, Interpolator i) {
        // view.getAnimation().setAnimationListener(new AnimationListener() {
        //
        // @Override
        // public void onAnimationStart(Animation animation) {
        // }
        //
        // @Override
        // public void onAnimationRepeat(Animation animation) {
        // }
        //
        // @Override
        // public void onAnimationEnd(Animation animation) {
        // }
        // });
        final AnimationSet as = new AnimationSet (true);
        final TranslateAnimation ta = new TranslateAnimation (Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, distanceX, Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, distanceY);
        ta.setDuration (durationTime);
        as.addAnimation (ta);
        if (i != null) {
            as.setInterpolator (i);
        }
        if (view != null) {
            view.startAnimation (as);
        }
        return ta;
    }

    /**
     * 自身渐隐消失动画
     *
     * @param view         需要绑定动画的控件,or Null
     * @param durationTime 动画持续时间
     * @return 消失动画
     */
    public static AlphaAnimation disappear (View view, int durationTime) {
        final AnimationSet as = new AnimationSet (true);
        final AlphaAnimation ta = new AlphaAnimation (0.01f, 0);
        ta.setDuration (durationTime);
        as.addAnimation (ta);
        if (view != null) {
            view.startAnimation (as);
        }
        return ta;
    }

    /**
     * 自身渐显出现动画
     *
     * @param view         需要绑定动画的控件,or Null
     * @param durationTime 动画持续时间
     * @return 出现动画
     */
    public static AlphaAnimation appear (View view, int durationTime) {
        final AnimationSet as = new AnimationSet (true);
        final AlphaAnimation ta = new AlphaAnimation (0, 1);
        ta.setDuration (durationTime);
        as.addAnimation (ta);
        if (view != null) {
            view.startAnimation (as);
        }
        return ta;
    }

    /**
     * 缩放动画
     *
     * @param view         需要绑定动画的控件,or Null
     * @param durationTime 动画持续时间
     * @param behind       比例尺寸
     * @return 缩放动画
     */
    public static ScaleAnimation scale (View view, int durationTime, float before, float behind) {
        final AnimationSet as = new AnimationSet (true);
        final ScaleAnimation sa = new ScaleAnimation (before, behind, before, behind, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration (durationTime);
        as.addAnimation (sa);
        if (view != null) {
            view.startAnimation (as);
        }
        return sa;
    }
}

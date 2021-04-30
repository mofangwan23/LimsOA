package cn.flyrise.feep.location.contract;

/**
 * 新建：陈冕;
 * 日期： 2017-12-22-11:05.
 */

public interface RxCountDownTimerContract {

    void startCountDown(int time);//秒

    void unSubscription();//注销

    interface RxCountDownTimerListener {
        void onCompleted();//计时结束

        void onNext(String integer); //时间更新
    }
}

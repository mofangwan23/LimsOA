package cn.flyrise.feep.location.util;

import java.util.concurrent.TimeUnit;

import cn.flyrise.feep.location.contract.RxCountDownTimerContract;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 新建：陈冕;
 * 日期： 2017-12-22-11:05.
 * 计时器
 */

public class RxCountDownTimer implements RxCountDownTimerContract {

    private Subscription mSubscription;

    private RxCountDownTimerListener mListener;

    public RxCountDownTimer(RxCountDownTimerListener listener) {
        this.mListener = listener;
    }

    @Override
    public void startCountDown(int time) {
        mSubscription = startTimer(time);
    }

    private Subscription startTimer(int maxTime) {
        return Observable.interval(0, 1, TimeUnit.SECONDS)
                .map(Integer -> String.valueOf(maxTime - Integer.intValue()))
                .take(maxTime + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        mListener.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(String integer) {
                        mListener.onNext(integer);
                    }
                });
    }

    @Override
    public void unSubscription() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }
}

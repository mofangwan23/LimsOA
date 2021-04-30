package cn.flyrise.feep.addressbook.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;

import cn.flyrise.feep.addressbook.model.DismissEvent;
import cn.flyrise.feep.core.common.FELog;

/**
 * @author ZYP
 * @since 2016-12-06 10:30
 */
public abstract class BaseFilterFragment extends Fragment {

    protected int mMaxHeight;

    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = createContentView(inflater, container, savedInstanceState);
        contentView.setOnClickListener(view -> {
            FELog.i("onCreate View content view click...");
            EventBus.getDefault().post(new DismissEvent());
        });
        return contentView;
    }

    public abstract View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public void resetContentHeight(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (mMaxHeight * 0.75F);
        view.setLayoutParams(layoutParams);
    }


}

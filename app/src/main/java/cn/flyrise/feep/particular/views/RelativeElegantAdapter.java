package cn.flyrise.feep.particular.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

/**
 * @author ZYP
 * @since 2016-10-23 14:53
 */
public abstract class RelativeElegantAdapter<T> {

    protected RelativeElegantLayout mRelativeElegantLayout;
    protected final Context mContext;
    protected final int mLayoutId;
    protected List<T> mData;

    public RelativeElegantAdapter(Context context, int layoutId, List<T> data) {
        this.mContext = context;
        this.mLayoutId = layoutId;
        this.mData = data;
    }

    protected void setRelativeElegantViewGroup(RelativeElegantLayout relativeElegantLayout) {
        this.mRelativeElegantLayout = relativeElegantLayout;
    }

    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    public T getItem(int position) {
        return mData == null ? null : mData.get(position);
    }

    public int getItemId(int position) {
        return position;
    }

    public View getView(int position) {
        View view = createItemView(position);
        if(view == null) {
            view = LayoutInflater.from(mContext).inflate(mLayoutId, null);
        }
        initItemViews(view, position, mData.get(position));
        return view;
    }

    public void update(List<T> data) {
        this.mData = data;
        notifyDataChange();
    }

    public void notifyDataChange() {
        if (mRelativeElegantLayout != null) {
            mRelativeElegantLayout.updateContainer();
        }
    }

    public TextView getTextView(View view, int id) {
        View childView = view.findViewById(id);
        return (TextView) childView;
    }

    public abstract void initItemViews(View view, int position, T item);

    public View createItemView(int position) {
        return null;
    }

}

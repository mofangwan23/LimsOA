package cn.flyrise.feep.main.modules;

import android.content.Context;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * @author 社会主义接班人
 * @since 2018-08-06 11:13
 */
public class ModuleGridView extends GridView {

	public ModuleGridView(Context context) {
		super(context);
	}

	public ModuleGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ModuleGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}

package cn.flyrise.feep.main.modules;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.AppMenu;
import cn.flyrise.feep.core.function.Category;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.PreDefinedShortCut;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import com.drag.framework.DragGridBaseAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-05-16 18:01
 */
public class ModuleSettingDragAdapter extends BaseAdapter implements DragGridBaseAdapter {

	private final LayoutInflater mInflater;
	private final String mHost;

	private OnModuleDeleteListener mModuleDeleteListener;
	private List<AppMenu> mModules;
	private int mHidePosition = -1;
	private boolean isShowDelete;       // 根据这个变量来判断是否显示删除图标，true是显示，false是不显示
	private Context mContext;
	private Category mCategory;

	public void setCategory(Category c) {
		this.mCategory = c;
	}

	public ModuleSettingDragAdapter(Context context, List<AppMenu> modules) {
		this.mModules = modules;
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.mHost = CoreZygote.getLoginUserServices().getServerAddress();
	}

	public void setModules(List<AppMenu> modules) {
		this.mModules = new ArrayList<>();
		if (CommonUtil.nonEmptyList(modules)) {
			for (AppMenu module : modules) {
				this.mModules.add(module);
			}
		}

		if (TextUtils.equals(mCategory.key, "10086") && mModules.size() < 4) {
			int count = 4 - mModules.size();
			for (int i = 0; i < count; i++) {
				this.mModules.add(AppMenu.emptyShortCutMenu());
			}
		}

		this.notifyDataSetChanged();
	}

	public void setOnModuleDeleteListener(OnModuleDeleteListener listener) {
		this.mModuleDeleteListener = listener;
	}

	public void appendModule(AppMenu module) {
		if (CommonUtil.isEmptyList(mModules)) {
			mModules = new ArrayList<>();
		}

		if (TextUtils.equals(mCategory.key, "10086")) {
			// 将那堆东西都找出来。然后移除
			List<AppMenu> toBeDelete = new ArrayList<>(4);
			for (AppMenu menu : mModules) {
				if (menu.menuId == AppMenu.ID_EMPTY_SHORT_CUT) {
					toBeDelete.add(menu);
				}
			}
			mModules.removeAll(toBeDelete);
		}

		mModules.add(module);
		if (TextUtils.equals(mCategory.key, "10086") && mModules.size() < 4) {
			int count = 4 - mModules.size();
			for (int i = 0; i < count; i++) {
				this.mModules.add(AppMenu.emptyShortCutMenu());
			}
		}
		this.notifyDataSetChanged();
	}

	@Override public int getCount() {
		return CommonUtil.isEmptyList(mModules) ? 0 : mModules.size();
	}

	@Override public Object getItem(int position) {
		return CommonUtil.isEmptyList(mModules) ? null : mModules.get(position);
	}

	@Override public long getItemId(int position) {
		return position;
	}

	public int getShortCutCounts() {
		int totalCount = 0;
		for (AppMenu menu : mModules) {
			if (menu.menuId != AppMenu.ID_EMPTY_SHORT_CUT) {
				totalCount++;
			}
		}
		return totalCount;
	}

	/**
	 * 由于复用 convertView 导致 item 在拖拽的时候消失了，所以这里不复用item，
	 */
	@Override public View getView(final int position, View convertView, ViewGroup parent) {
		if (TextUtils.equals(mCategory.key, "10086")) {
			convertView = mInflater.inflate(R.layout.item_module_setting_item, parent, false);
		}
		else {
			convertView = mInflater.inflate(R.layout.item_module_setting_basic, parent, false);
		}

		ImageView ivIcon = convertView.findViewById(R.id.ivIcon);
		TextView tvName = convertView.findViewById(R.id.tvText);
		ImageView ivOperator = convertView.findViewById(R.id.ivOperator);

		AppMenu module = mModules.get(position);
		if (module.menuId == AppMenu.ID_EMPTY_SHORT_CUT) {
			ivIcon.setVisibility(View.GONE);
			tvName.setVisibility(View.GONE);
			ivOperator.setVisibility(View.GONE);
			convertView.setBackgroundResource(R.drawable.bg_short_cut_app);
			return convertView;
		}

		ivIcon.setVisibility(View.VISIBLE);
		tvName.setVisibility(View.VISIBLE);
		ivOperator.setVisibility(View.VISIBLE);
		if (TextUtils.equals(mCategory.key, "10086")) {
			convertView.setBackgroundColor(Color.parseColor("#F2F2F2"));
		}

		tvName.setText(module.menu);
		setCustomIcon(module, ivIcon);

		if (position == mHidePosition) {
			convertView.setVisibility(View.INVISIBLE);
		}

		ivOperator.setOnClickListener(v -> {
			if (mModuleDeleteListener != null) {
				mModuleDeleteListener.onModuleDelete(module);
			}
		});

		return convertView;
	}

	public void removeModule(AppMenu deletedModuleInfo) {
		mModules.remove(deletedModuleInfo);
		if (TextUtils.equals(mCategory.key, "10086") && mModules.size() < 4) {
			int count = 4 - mModules.size();
			for (int i = 0; i < count; i++) {
				this.mModules.add(AppMenu.emptyShortCutMenu());
			}
		}
		this.notifyDataSetChanged();
	}

	private void setCustomIcon(AppMenu module, ImageView mImageView) {
		mImageView.setVisibility(View.VISIBLE);
		if (TextUtils.isEmpty(module.icon)) {
			mImageView.setImageResource(module.imageRes);
			return;
		}

		int defaultImageRes = module.imageRes;
		if (TextUtils.equals(mCategory.key, "10086")) {
			PreDefinedShortCut sc = FunctionManager.getDefinedModuleRepository().getShortCut(module.menuId);
			if (sc != null) {
				defaultImageRes = sc.imageRes;
			}
		}

		String imageUrl = module.icon.startsWith(mHost) ? module.icon : mHost + module.icon;
		FEImageLoader.load(mContext, mImageView, imageUrl, defaultImageRes);
	}

	@Override public boolean isEnabled(int position) {
		AppMenu menu = mModules.get(position);
		return menu.menuId != AppMenu.ID_EMPTY_SHORT_CUT;
	}

	@Override public boolean areAllItemsEnabled() {
		return true;
	}

	@Override public void reorderItems(int oldPosition, int newPosition) {
		final AppMenu temp = mModules.get(oldPosition);
		if (oldPosition < newPosition) {
			for (int i = oldPosition; i < newPosition; i++) {
				Collections.swap(mModules, i, i + 1);
			}
		}
		else if (oldPosition > newPosition) {
			for (int i = oldPosition; i > newPosition; i--) {
				Collections.swap(mModules, i, i - 1);
			}
		}

		mModules.set(newPosition, temp);
	}


	@Override public void setHideItem(int hidePosition) {
		this.mHidePosition = hidePosition;
		notifyDataSetChanged();
	}


	@Override public void setIsShowDelete(boolean isShowDelete) {
		this.isShowDelete = isShowDelete;
		this.notifyDataSetChanged();
	}

	public interface OnModuleDeleteListener {

		void onModuleDelete(AppMenu moduleInfo);
	}

	public List<AppMenu> getEditedModules() {
		return this.mModules;
	}

}

package cn.flyrise.feep.main.modules;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.AppMenu;
import cn.flyrise.feep.core.function.Category;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.PreDefinedShortCut;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-05-17 09:01
 */
public class ModuleSettingAdapter extends BaseAdapter {

	private List<AppMenu> mModules;
	private OnModuleAddListener mAddModuleListener;
	private final String mHost;
	private Category mCategory;
	private boolean isAddEnable;

	public ModuleSettingAdapter(List<AppMenu> modules) {
		this.mModules = modules;
		this.mHost = CoreZygote.getLoginUserServices().getServerAddress();
	}

	public void setOnModuleAddListener(OnModuleAddListener moduleAddListener) {
		this.mAddModuleListener = moduleAddListener;
	}

	public void setCategory(Category c) {
		this.mCategory = c;
	}

	public void setModules(List<AppMenu> standardModules) {
		this.mModules = standardModules;
		this.notifyDataSetChanged();
	}

	public void removeModule(AppMenu menu) {
		mModules.remove(menu);
		notifyDataSetChanged();
	}

	public void appendModule(AppMenu module) {
		if (CommonUtil.isEmptyList(mModules)) {
			mModules = new ArrayList<>();
		}
		mModules.add(module);
		this.notifyDataSetChanged();
	}

	public void enableAddShortCuts(boolean enable) {
		this.isAddEnable = enable;
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

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		ModuleViewHolder holder;
		if (convertView == null) {
			if (TextUtils.equals(mCategory.key, "10086")) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module_setting_item, parent, false);
			}
			else {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module_setting_basic, parent, false);
			}
			holder = new ModuleViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ModuleViewHolder) convertView.getTag();
		}

		AppMenu menuInfo = mModules.get(position);
		String iconUrl = menuInfo.icon;
		holder.ivIcon.setVisibility(View.VISIBLE);
		if (TextUtils.isEmpty(iconUrl)) {
			holder.ivIcon.setImageResource(menuInfo.imageRes);
		}
		else {
			int defaultImageRes = menuInfo.imageRes;
			if (TextUtils.equals(mCategory.key, "10086")) {
				PreDefinedShortCut sc = FunctionManager.getDefinedModuleRepository().getShortCut(menuInfo.menuId);
				if (sc != null) {
					defaultImageRes = sc.imageRes;
				}
			}
			String imageUrl = menuInfo.icon.startsWith(mHost) ? menuInfo.icon : mHost + menuInfo.icon;
			FEImageLoader.load(convertView.getContext(), holder.ivIcon, imageUrl, defaultImageRes);
		}

		holder.tvName.setText(menuInfo.menu);
		if (TextUtils.equals(mCategory.key, "10086")) {
			if (isAddEnable) {
				holder.ivOperator.setImageResource(R.drawable.icon_module_add);
			}
			else {
				holder.ivOperator.setImageResource(R.drawable.icon_module_add_unable);
			}
		}
		else {
			holder.ivOperator.setImageResource(R.drawable.icon_module_add);
		}
		convertView.setOnClickListener(v -> {
			if (mAddModuleListener != null) {
				mAddModuleListener.onModuleAdd(menuInfo);
			}
		});

		return convertView;
	}


	public interface OnModuleAddListener {

		void onModuleAdd(AppMenu moduleInfo);
	}
}

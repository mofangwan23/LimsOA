package cn.flyrise.feep.main.modules;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.function.AppMenu;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import com.jakewharton.rxbinding.view.RxView;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ZYP
 * @since 2018-03-19 11:39
 */
public class MainModuleAdapter extends RecyclerView.Adapter<ViewHolder> {

	private static final int TYPE_CATEGORY = 1;
	private static final int TYPE_MODULE = 2;
	private Map<Integer, Boolean> mBadgeMap;

	private String mHost;
	private Context mContext;
	private List<AppMenu> mModules;
	private OnModuleClickListener mClickListener;
	private OnModuleLongClickListener mLongClickListener;

	public MainModuleAdapter(Context context, String host) {
		this.mContext = context;
		this.mHost = host;
	}

	public void setModules(List<AppMenu> modules) {
		this.mModules = modules;
		if (mBadgeMap != null && !mBadgeMap.isEmpty()) {
			for (AppMenu module : mModules) {
				int id = module.menuId;
				module.hasNews = mBadgeMap.containsKey(id) ? mBadgeMap.get(id) : false;
			}
		}
		this.notifyDataSetChanged();
	}

	public List<AppMenu> getDisplayModules() {
		return this.mModules;
	}

	public void setModuleBadge(Map<Integer, Boolean> badgeMap) {
		this.mBadgeMap = badgeMap;
		if (badgeMap == null) return;
		if (CommonUtil.isEmptyList(mModules)) return;

		for (AppMenu module : mModules) {
			int id = module.menuId;
			module.hasNews = badgeMap.containsKey(id) ? badgeMap.get(id) : false;
		}
		this.notifyDataSetChanged();
	}

	public void setOnModuleClickListener(OnModuleClickListener listener) {
		this.mClickListener = listener;
	}

	public void setOnModuleLongClickListener(OnModuleLongClickListener listener) {
		this.mLongClickListener = listener;
	}

	@Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		final GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
		layoutManager.setSpanSizeLookup(new SpanSizeLookup() {
			@Override public int getSpanSize(int position) {
				int viewType = getItemViewType(position);
				return (viewType == TYPE_CATEGORY) ? layoutManager.getSpanCount() : 1;
			}
		});
	}

	@Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ViewHolder viewHolder;
		if (viewType == TYPE_CATEGORY) {
			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module_category, parent, false);
			viewHolder = new CategoryViewHolder(itemView);
		}
		else {
			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.extend_module_grid_item_v7, parent, false);
			viewHolder = new ModuleViewHolder(itemView);
		}
		return viewHolder;
	}

	@Override public void onBindViewHolder(ViewHolder holder, int position) {
		AppMenu module = mModules.get(position);
		if (module.menuId == AppMenu.ID_CATEGORY) {    // 类别
			CategoryViewHolder cHolder = (CategoryViewHolder) holder;
			cHolder.itemView.setPadding(0, PixelUtil.dipToPx(20), 0, 0);
			cHolder.tvCategory.setText(module.menu);
			cHolder.itemView.setOnClickListener(null);
			return;
		}

		if (module.menuId == AppMenu.ID_EMPTY) {       // 空格，只为填满的傻逼玩意
			((ModuleViewHolder) holder).makeModuleEmpty();
			holder.itemView.setBackgroundResource(R.drawable.bg_main_module_item);
			holder.itemView.setOnClickListener(null);
			return;
		}

		ModuleViewHolder mHolder = (ModuleViewHolder) holder;
		mHolder.itemView.setBackgroundResource(R.drawable.drag_gridview_item_border);
		mHolder.tvModuleName.setText(module.menu);
		// 1. 设置 Icon
		mHolder.ivModuleIcon.setVisibility(View.VISIBLE);
		setModuleIcon(mHolder.ivModuleIcon, module.icon, module.imageRes);

		// 2. 检查是否有小红点
		mHolder.ivBadge.setVisibility(module.hasNews ? View.VISIBLE : View.GONE);

		// 4. 设置点击事件
		RxView.clicks(mHolder.itemView).throttleFirst(1, TimeUnit.SECONDS)
				.subscribe(it -> {
					if (mClickListener != null) {
						mClickListener.onModuleClick(position, module);
					}
				});

		// 5. 设置长按事件
		mHolder.itemView.setOnLongClickListener(view -> {
			if (mLongClickListener != null) {
				mLongClickListener.onModuleLongClick(position, module);
			}
			return true;
		});
	}

	private void setModuleIcon(ImageView mImg, String iconPath, int imageRes) {
		if (TextUtils.isEmpty(iconPath)) {
			FEImageLoader.clear(mContext, mImg);
			try {
				mImg.setImageResource(imageRes);
			} catch (NotFoundException exp) {
				mImg.setImageResource(R.drawable.ic_unknown);
			}
		}
		else {
			String url = iconPath.startsWith(mHost) ? iconPath : mHost + iconPath;
			FEImageLoader.load(mContext, mImg, url, imageRes);
		}
	}

	@Override public void onViewRecycled(@NonNull ViewHolder holder) {
		if (holder instanceof ModuleViewHolder) FEImageLoader.clear(mContext, ((ModuleViewHolder) holder).ivModuleIcon);
		super.onViewRecycled(holder);
	}

	@Override public int getItemCount() {
		return CommonUtil.isEmptyList(mModules) ? 0 : mModules.size();
	}

	@Override public int getItemViewType(int position) {
		AppMenu module = mModules.get(position);
		return module.menuId == AppMenu.ID_CATEGORY ? TYPE_CATEGORY : TYPE_MODULE;
	}

	public class CategoryViewHolder extends RecyclerView.ViewHolder {

		TextView tvCategory;

		CategoryViewHolder(View itemView) {
			super(itemView);
			tvCategory = itemView.findViewById(R.id.tvModuleCategory);
		}
	}

	public class ModuleViewHolder extends RecyclerView.ViewHolder {

		ImageView ivModuleIcon;
		TextView tvModuleName;
		ImageView ivBadge;

		ModuleViewHolder(View convertView) {
			super(convertView);
			ivModuleIcon = convertView.findViewById(R.id.item_image);
			tvModuleName = convertView.findViewById(R.id.item_text);
			ivBadge = convertView.findViewById(R.id.item_badge);
		}

		void makeModuleEmpty() {
			ivModuleIcon.setVisibility(View.GONE);
			tvModuleName.setText("");
			ivBadge.setVisibility(View.GONE);
		}
	}

	public interface OnModuleClickListener {

		void onModuleClick(int position, AppMenu module);
	}

	public interface OnModuleLongClickListener {

		void onModuleLongClick(int position, AppMenu module);
	}


}

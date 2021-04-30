package cn.flyrise.feep.commonality.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.interpolator.BackInterpolator;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.MainMenuRecyclerViewActivity;
import cn.flyrise.feep.commonality.bean.MainMenuRecyclerItem;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2016-7-12.
 */
public class MainMenuRecyclerAdapter extends RecyclerView.Adapter<MainMenuRecyclerAdapter.MyViewHolder> {

	private Context myContext;
	private List<MainMenuRecyclerItem> datas;
	private final int startTime = 160;
	private final int endTime = 120;
	private OnMenuItemClickListener onItemClickListener;
	private final int MENU_CLICK_LISTENER = 0X010066;
	private String currentType;
	private RecyclerView listView;
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == MENU_CLICK_LISTENER) {
				if (onItemClickListener != null) {
					onItemClickListener.MenuItem((MainMenuRecyclerItem) msg.obj);
				}
			}
		}
	};

	public MainMenuRecyclerAdapter(Context context, RecyclerView listView, List<MainMenuRecyclerItem> items, String currentType) {
		this.myContext = context;
		this.datas = items;
		this.currentType = currentType;
		this.listView = listView;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		MyViewHolder viewHolder = new MyViewHolder(
				LayoutInflater.from(myContext).inflate(R.layout.main_menu_recyclerview_item, parent, false));
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		final MainMenuRecyclerItem item = datas.get(position);
		if (MainMenuRecyclerViewActivity.WORKPLAN_MENU.equals(currentType)) {
			holder.tv_name.setText(item.menuName);
			String userId = item.userId;
			if (TextUtils.isEmpty(userId)) {
				FEImageLoader.load(myContext, holder.img_icon, item.menuIcon);
			}
			else {
				String host = CoreZygote.getLoginUserServices().getServerAddress();
				CoreZygote.getAddressBookServices().queryUserDetail(userId)
						.subscribe(it -> {
							if (it != null) {
								FEImageLoader.load(myContext, holder.img_icon, host + it.imageHref, it.userId, it.name);
							}
							else {
								FEImageLoader.load(myContext, holder.img_icon, item.menuIcon);
							}
						}, error -> {
							FEImageLoader.load(myContext, holder.img_icon, item.menuIcon);
						});
			}
		}
		else {
			holder.tv_name.setText(item.menuName);
			holder.img_icon.setBackgroundResource(item.menuIcon);
		}
		holder.layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setMenuClickAnimator(v);
				if (v.isEnabled()) {
					Message message = myHandler.obtainMessage();
					message.what = MENU_CLICK_LISTENER;
					message.obj = item;
					myHandler.sendMessageDelayed(message, 60);//稍微延迟一丢丢，不然会引起还原动画卡顿
				}
			}
		});
		randomAnimationTime(holder.layout, holder.tv_name, position);
	}

	@Override
	public int getItemCount() {
		return datas == null ? 0 : datas.size();
	}

	class MyViewHolder extends RecyclerView.ViewHolder {

		private TextView tv_name;
		private ImageView img_icon;
		private LinearLayout layout;

		public MyViewHolder(View itemView) {
			super(itemView);
			tv_name = (TextView) itemView.findViewById(R.id.item_tv);
			img_icon = (ImageView) itemView.findViewById(R.id.item_img);
			layout = (LinearLayout) itemView.findViewById(R.id.layout);
		}
	}

	private void randomAnimationTime(final View view, final View name, final int position) {
		int random = getRandomTop(position);
		setAnimation(view, name, startTime + (160 / random), endTime + (140 / random));
	}

	private int getRandomTop(int position) {
		if (position <= 2) {
			return 3;
		}
		else if (position > 2 && position <= 5) {
			return 2;
		}
		else if (position > 5) {
			return 1;
		}
		return 1;
	}

	private void setAnimation(final View view, final View name, int startTime, int endTime) {
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator animatorA = ObjectAnimator.ofFloat(view, "alpha", 0f, 0.7f);
		animatorA.setDuration(startTime);
		ObjectAnimator animatorTy = ObjectAnimator.ofFloat(view, "translationY", -90f, 45f);
		animatorTy.setDuration(startTime);
		ObjectAnimator animatorAEnd = ObjectAnimator.ofFloat(view, "alpha", 0.7f, 1f);
		animatorA.setDuration(endTime);
		ObjectAnimator animatorTyEnd = ObjectAnimator.ofFloat(view, "translationY", 45f, 0f);
		animatorTyEnd.setDuration(endTime);

		set.play(animatorA).with(animatorTy);
		set.play(animatorA).before(animatorAEnd);
		set.play(animatorA).before(animatorTyEnd);
		set.setInterpolator(new BackInterpolator(2f));
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				view.setVisibility(View.VISIBLE);
				listView.setEnabled(true);
			}
		});
		set.start();
	}

	private void setMenuClickAnimator(View view) {
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator animatorSx = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f);
		ObjectAnimator animatorSy = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f);
		ObjectAnimator animatorSendx = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1f);
		ObjectAnimator animatorSendy = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1f);
		set.setDuration(50);
		set.play(animatorSx).with(animatorSy);
		set.play(animatorSx).before(animatorSendx);
		set.play(animatorSx).before(animatorSendy);
		set.start();
	}

	public interface OnMenuItemClickListener {

		void MenuItem(MainMenuRecyclerItem clickedMenu);
	}

	public void setOnItemClickListener(OnMenuItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}
}

package cn.flyrise.feep.location.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.location.bean.LocusPersonLists;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 类描述：
 * @author 罗展健
 * @version 1.0
 */
public class LocationPersonAdapter extends BaseSelectedAdapter {

	private List<LocusPersonLists> personLists;
	private final Context context;

	public LocationPersonAdapter(Context context, List<LocusPersonLists> personLists) {
		this.context = context;
		this.personLists = personLists;
	}

	@Override
	public int getDataSourceCount() {
		return personLists == null ? 0 : personLists.size();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ViewHolder viewHolder = (ViewHolder) holder;
		final LocusPersonLists person = personLists.get(position);
		viewHolder.check.setVisibility(TextUtils.equals(id, person.getUserId()) ? View.VISIBLE : View.GONE);

		CoreZygote.getAddressBookServices().queryUserDetail(person.getUserId())
				.subscribe(it -> {
					if (it != null) {
						viewHolder.name.setText(it.name);
						viewHolder.post.setText(it.position);
						String host = CoreZygote.getLoginUserServices().getServerAddress();
						FEImageLoader.load(context, viewHolder.headImg, host + it.imageHref, it.userId, it.name);
						viewHolder.layout.setOnClickListener(v -> {
							setCurrentPosition(it.userId);
							mListener.onSelectedClickeItem(person.getUserId(), position);
						});
					}
					else {
						FEImageLoader.load(context, viewHolder.headImg, R.drawable.administrator_icon);
					}
				}, error -> {
					FEImageLoader.load(context, viewHolder.headImg, R.drawable.administrator_icon);
				});
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.location_person_item, null));
	}

	private class ViewHolder extends RecyclerView.ViewHolder {

		private ImageView headImg;
		private TextView iconName;
		public TextView name;
		public TextView post;
		public TextView letter;
		public ImageView check;
		public View layout;

		public ViewHolder(View itemView) {
			super(itemView);
			check = itemView.findViewById(R.id.person_checked);
			headImg = itemView.findViewById(R.id.person_icon);
			iconName = itemView.findViewById(R.id.user_name);
			name = itemView.findViewById(R.id.person_name);
			post = itemView.findViewById(R.id.person_post);
			letter = itemView.findViewById(R.id.person_char);
			layout = itemView;
		}
	}

	public int getCurrentPosition() {
		if (CommonUtil.isEmptyList(personLists)) {
			return 0;
		}
		for (int i = 0; i < personLists.size(); i++) {
			if (personLists.get(i) == null) {
				continue;
			}
			if (TextUtils.equals(id, personLists.get(i).getUserId())) {
				return i;
			}
		}
		return 0;
	}
}

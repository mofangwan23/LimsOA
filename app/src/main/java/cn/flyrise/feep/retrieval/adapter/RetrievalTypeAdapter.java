package cn.flyrise.feep.retrieval.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.retrieval.vo.RetrievalType;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-07 15:12
 */
public class RetrievalTypeAdapter extends BaseAdapter {

	private List<RetrievalType> mRetrievalTypes;

	public void setDataSource(List<RetrievalType> retrievalTypes) {
		this.mRetrievalTypes = retrievalTypes;
		this.notifyDataSetChanged();
	}

	@Override public int getCount() {
		return CommonUtil.isEmptyList(mRetrievalTypes) ? 0 : mRetrievalTypes.size();
	}

	@Override public Object getItem(int position) {
		return CommonUtil.isEmptyList(mRetrievalTypes) ? null : mRetrievalTypes.get(position);
	}

	@Override public long getItemId(int position) {
		return position;
	}

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.dr_item_retrieval_search_type, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		RetrievalType retrievalType = mRetrievalTypes.get(position);
		holder.tvSearchType.setText(retrievalType.value);
		if (position == 0) {
			holder.splitLine.setVisibility(View.VISIBLE);
		}
		else {
			int mod = (position + 1) % 3;
			if (mod == 0) {
				holder.splitLine.setVisibility(View.GONE);
			}
			else {
				holder.splitLine.setVisibility(View.VISIBLE);
			}
		}

		holder.splitLine.setVisibility(((position + 1) % 3 == 0 || position == mRetrievalTypes.size() - 1) ? View.GONE : View.VISIBLE);
		return convertView;
	}

	private static class ViewHolder {

		private TextView tvSearchType;
		private View splitLine;

		public ViewHolder(View itemView) {
			tvSearchType = itemView.findViewById(R.id.drTvSearchType);
			splitLine = itemView.findViewById(R.id.drViewSplitLine);
		}

	}

}

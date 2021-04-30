package cn.flyrise.feep.knowledge.adpater;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.component.LargeTouchCheckBox;
import cn.flyrise.feep.knowledge.model.ListBaseItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by KLC on 2016/12/6.
 */

public abstract class KnowledgeListBaseAdapter<T> extends BaseRecyclerAdapter {

	protected List<T> dataList;
	private boolean canChoice;
	int choiceCount;
	SelectAllOrNotListener selectAllOrNotListener;


	public void setItems(List<T> dataList) {
		this.dataList = dataList;
	}

	public void refreshData(List<T> dataList) {
		this.dataList = dataList;
		this.notifyDataSetChanged();
	}

	public void addData(List<T> dataList) {
		if (dataList == null) {
			this.dataList = new ArrayList<>();
		}
		this.dataList.addAll(dataList);
		this.notifyDataSetChanged();
	}

	public void setSelectAllOrNotListener(SelectAllOrNotListener selectAllOrNotListener) {
		this.selectAllOrNotListener = selectAllOrNotListener;
	}

	protected T getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public int getDataSourceCount() {
		return dataList == null ? 0 : dataList.size();
	}

	public List<T> getDataList() {
		return dataList;
	}

	@Override
	public abstract void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position);


	protected void setListener(final KnowledgeListBaseAdapter.ItemViewHolder viewHolder, final ListBaseItem baseItem) {
		viewHolder.view.setOnClickListener(v -> {
			if (onItemClickListener != null && !canChoice)
				onItemClickListener.onItemClick(viewHolder.view, baseItem);
			else {
				onCheckBoxClickEvent(baseItem, viewHolder);
			}
		});
		viewHolder.view.setOnLongClickListener(v -> {
			onCheckBoxClickEvent(baseItem, viewHolder);
			if (onItemLongClickListener != null) {
				onItemLongClickListener.onItemLongClick(viewHolder.view, baseItem);
				return true;
			}
			return false;
		});
	}

	protected void onCheckBoxClickEvent(ListBaseItem file, KnowledgeListBaseAdapter.ItemViewHolder viewHolder) {
		boolean choice = !file.isChoice;
		file.isChoice = choice;
		viewHolder.checkBox.setChecked(choice);
		if (choice) {
			choiceCount++;
		}
		else {
			choiceCount--;
		}
		if (choiceCount == getDataSourceCount() && selectAllOrNotListener != null) {
			selectAllOrNotListener.selectAll();
		}
		else if (choiceCount == getDataSourceCount() - 1 && selectAllOrNotListener != null) {
			selectAllOrNotListener.notAll();
		}
	}


	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.knowledge_list_item, parent, false);
		return new ItemViewHolder(convertView);
	}

	class ItemViewHolder extends RecyclerView.ViewHolder {

		View view;
		LinearLayout llTitle;
		LargeTouchCheckBox checkBox;
		ImageView imageView;
		TextView fileName;
		TextView time;
		TextView reciverTv;
		LinearLayout llTime;

		ItemViewHolder(View itemView) {
			super(itemView);
			this.view = itemView;
			this.llTitle = itemView.findViewById(R.id.knowledge_list_item_ll_name);
			this.checkBox = itemView.findViewById(R.id.checkbox);
			this.imageView = itemView.findViewById(R.id.file_icon);
			this.fileName = itemView.findViewById(R.id.file_name);
			this.time = itemView.findViewById(R.id.time);
			this.reciverTv = itemView.findViewById(R.id.receiver);
			this.llTime = itemView.findViewById(R.id.ll_time);
		}
	}

	public void setCanChoice(boolean canCheck) {
		if (!canCheck) choiceCount = 0;
		this.canChoice = canCheck;
		if (!canCheck) {
			for (T item : dataList) {
				((ListBaseItem) item).isChoice = false;
			}
		}
		notifyDataSetChanged();
	}

	public boolean isCanChoice() {
		return this.canChoice;
	}

	public void selectAllOrNoOne() {
		if (dataList.size() == choiceCount) {
			unSelectAll();
		}
		else {
			selectAll();
		}
	}

	protected void selectAll() {
		for (T item : dataList) {
			((ListBaseItem) item).isChoice = true;
		}
		choiceCount = dataList.size();
		notifyDataSetChanged();
		if (selectAllOrNotListener != null)
			selectAllOrNotListener.selectAll();
	}

	protected void unSelectAll() {
		for (T item : dataList) {
			((ListBaseItem) item).isChoice = false;
		}
		choiceCount = 0;
		notifyDataSetChanged();
		if (selectAllOrNotListener != null)
			selectAllOrNotListener.notAll();

	}


	public interface SelectAllOrNotListener {

		void selectAll();

		void notAll();
	}

}
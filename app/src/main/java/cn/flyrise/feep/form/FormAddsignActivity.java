/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-7-12
 */
package cn.flyrise.feep.form;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.library.view.DeleteButton;
import cn.flyrise.android.library.view.ResizeTextView;
import cn.flyrise.android.library.view.addressbooklistview.AddressBookListView;
import cn.flyrise.android.library.view.addressbooklistview.AddressBookListView.OnLoadListener;
import cn.flyrise.android.library.view.addressbooklistview.adapter.AddressBookBaseAdapter;
import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.protocol.entity.FormSendDoRequest;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.utility.DataStack;
import cn.flyrise.feep.collaboration.view.PersonPositionSwitcher;
import cn.flyrise.feep.collaboration.view.workflow.WorkFlowTranslater;
import cn.flyrise.feep.commonality.PersonSearchActivity;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.AddressBookFilterType;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.event.EventFormAddsignSearchPersonData;
import cn.flyrise.feep.form.util.FormDataProvider;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * <b>类功能描述：</b><div style="margin-left:40px;margin-top:-10px"> 选择需要加签的人 (与流程图左侧人员岗位选择,返回键与列表提取出来view,泛型) </div>
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class FormAddsignActivity extends BaseActivity {

	public static final String PERSONKEY = "signPerson";

	private PersonPositionSwitcher switcher;
	private RelativeLayout searchLayout;
	private ResizeTextView back_Tv;
	private AddressBookListView select_Lv;                                   // 左侧可供添加人员岗位列表
	private ListView added_Lv;

	private PersonAdapter personSelectAdapter;
	private PersonAdapter personAddedAdapter;
	private ArrayList<AddressBookItem> added;
	private boolean isPerson = true;

	private String mCurrentFlowNodeGUID;
	private List<String> attachemnts;
	private FormDataProvider mFormDataProvider;


	public static void startActivity(Activity activity, FormSendDoRequest formSendDoRequest, String flowGUID, List<String> attachemnts) {
		Intent intent = new Intent(activity, FormAddsignActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("requestData", formSendDoRequest);
		intent.putExtra("flowGUID", flowGUID);
		intent.putExtra("attachemnt_path", GsonUtil.getInstance().toJson(attachemnts));
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_addsign);
		EventBus.getDefault().register(this);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.form_titleadd);
		toolbar.setRightText(R.string.form_submit);
		toolbar.setRightTextClickListener(v -> {
			List<FormNodeItem> nodes = getResult();
			if (nodes != null && nodes.size() != 0 && nodes.get(0).getValue() != null) {
				Bundle bundle = getIntent().getExtras();
				FormSendDoRequest request = bundle.getParcelable("requestData");
				request.setNodes(nodes);
				mFormDataProvider = new FormDataProvider(FormAddsignActivity.this, request.getId(), null);
				if (!mFormDataProvider.isAllowSend) return;
				mFormDataProvider.isAllowSend = false;
				mFormDataProvider.submit(request, attachemnts);
			}
			else {
				FEToast.showMessage(getResources().getString(R.string.form_null_addsign));
			}
		});
	}

	@Override
	public void bindView() {
		switcher = findViewById(R.id.switcher);
		back_Tv = findViewById(R.id.back);
		select_Lv = findViewById(R.id.select);
		select_Lv.setDiverHide();
		added_Lv = findViewById(R.id.added);
		searchLayout = findViewById(R.id.searchBar);
	}

	@Override
	public void bindData() {
		getIntentData();
		added = (ArrayList<AddressBookItem>) DataStack.getInstance().get(PERSONKEY);
		personSelectAdapter = new PersonAdapter(this, PersonAdapter.LEFT);
		select_Lv.setAdapter(personSelectAdapter);
		personAddedAdapter = new PersonAdapter(this, PersonAdapter.RIGHT);
		personAddedAdapter.addAllPerson(added);
		added_Lv.setAdapter(personAddedAdapter);
		setBackText(getResources().getString(R.string.flow_loading));
		select_Lv.setPostToCurrentDepartment(true);
		select_Lv.setFilterType(AddressBookFilterType.Authority);
		select_Lv.setPersonType(AddressBookType.Staff);
		select_Lv.setAutoJudgePullRefreshAble(true);
		select_Lv.requestRoot(1);
		back_Tv.setMaxLines(2);
	}

	private void getIntentData() {
		if (getIntent() == null) return;
		mCurrentFlowNodeGUID = getIntent().getExtras().getString("flowGUID");
		String data = getIntent().getStringExtra("attachemnt_path");
		if (TextUtils.isEmpty(data)) return;
		attachemnts = GsonUtil.getInstance().fromJson(data, new TypeToken<List<String>>() {}.getType());
	}

	@Override
	public void bindListener() {
		searchLayout.setOnClickListener(v -> {
			PersonSearchActivity.setPersonSearchActivity(getSearchType(), "FormAddsignActivity");
			Intent intent = new Intent(FormAddsignActivity.this, PersonSearchActivity.class);
			intent.putExtra(PersonSearchActivity.REQUESTNAME, getResources().getString(R.string.flow_titleadd));
			startActivity(intent);
		});
		back_Tv.setOnClickListener(v -> {
			if (select_Lv.isCanGoBack()) {
				LoadingHint.show(FormAddsignActivity.this);
				select_Lv.goBack();
			}
		});
		select_Lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final AddressBookItem clickPerson = personSelectAdapter.getItem(position).getAddressBookItem();
				processingPerson(clickPerson);
			}
		});
		switcher.setOnBoxClickListener(b -> {
			select_Lv.setPersonType(b ? AddressBookType.Staff : AddressBookType.Position);
			select_Lv.requestRoot(1);
			isPerson = b;
		});
		select_Lv.setOnLoadListener(new OnLoadListener() {
			@Override
			public void Loading(AddressBookListItem bookListItem) {
				LoadingHint.show(FormAddsignActivity.this);
				back_Tv.setText(getResources().getString(R.string.flow_loading));
			}

			@Override
			public void Loaded(AddressBookListItem bookListItem) {
				if (LoadingHint.isLoading()) {
					LoadingHint.hide();
				}
				if (bookListItem != null) {
					final String name = bookListItem.getItemName();
					if (name == null) {
						back_Tv.setText(getResources().getString(R.string.flow_btnback));
					}
					else if ("-1".equals(name)) {
						back_Tv.setText(getResources().getString(R.string.flow_root));
					}
					else if (bookListItem.getTotalNums() != 0) {
						back_Tv.setText(name);
					}
				}
			}
		});
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void eventBusSearchData(EventFormAddsignSearchPersonData data) {
		processingPerson(data.addingNode);
	}

	private int getSearchType() {
		return (isPerson ? AddressBookType.Staff : AddressBookType.Position);
	}

	/*--点击键盘返回键，搜索键等--*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				super.onKeyDown(keyCode, event);
				break;
			case KeyEvent.KEYCODE_SEARCH:
				// 此处添加搜索功能
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void processingPerson(AddressBookItem clickPerson) {
		if (clickPerson.getType() == AddressBookType.Staff || clickPerson.getType() == AddressBookType.Position) {
			for (final AddressBookItem person : added) {
				if (person.getName().equals(clickPerson.getName())) {
					FEToast.showMessage(getString(R.string.form_personadded));
					return;
				}
			}
			personAddedAdapter.addPerson(clickPerson);
			personAddedAdapter.notifyDataSetChanged();
			added.add(clickPerson);
		}
	}

	private void setBackText(String text) {
		if ("-1".equals(text)) {
			text = getString(R.string.flow_btnback);
		}
		text = CommonUtil.toDBC(text);
		if (text.length() > 10) {
			back_Tv.setText(text.substring(0, 5) + "\n" + text.substring(5, 9) + "..");
			back_Tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		}
		else if (text.length() == 10) {
			back_Tv.setText(text.substring(0, 5) + "\n" + text.substring(5, 10));
			back_Tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		}
		else if (text.length() > 5) {
			back_Tv.setText(text.substring(0, 5) + "\n" + text.substring(5, text.length()));
			back_Tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		}
		else {
			back_Tv.setText(text);
			back_Tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.FormAddSign);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.FormAddSign);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

	/*--人员列表适配器--*/
	private final class PersonAdapter extends AddressBookBaseAdapter {

		public static final int LEFT = 1;
		public static final int RIGHT = 2;
		private int location = LEFT;
		private final Context context;
		private final List<AddressBookListItem> persons = new ArrayList<>();

		PersonAdapter(Context context, int location) {
			this.context = context;
			this.location = location;
		}

		@Override
		public int getCount() {
			return persons.size();
		}

		@Override
		public AddressBookListItem getItem(int position) {
			return persons.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				if (location == LEFT) {
					final RelativeLayout.LayoutParams name_Lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
							PixelUtil.dipToPx(46));
					convertView = new RelativeLayout(context);
					final TextView name = new TextView(context);
					name.setText(shortString(persons.get(position).getAddressBookItem().getName()));
					name_Lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					name_Lp.addRule(RelativeLayout.CENTER_VERTICAL);
					name_Lp.setMargins(PixelUtil.dipToPx(3), 0, 3, 0);
					name.setLayoutParams(name_Lp);
					name.setGravity(Gravity.CENTER_VERTICAL);
					name.setTextColor(0xff333333);
					final ImageView iv = new ImageView(context);
					iv.setImageDrawable(FormAddsignActivity.this.getResources().getDrawable(R.drawable.add_btn_gray_fe));
					final RelativeLayout.LayoutParams iv_Lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					iv_Lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					iv_Lp.addRule(RelativeLayout.CENTER_VERTICAL);
					iv_Lp.setMargins(0, 50, 5, 50);
					iv.setLayoutParams(iv_Lp);
					if (persons.get(position).getAddressBookItem().getType() != AddressBookType.Staff
							&& persons.get(position).getAddressBookItem().getType() != AddressBookType.Position) {
						iv.setVisibility(View.INVISIBLE);
					}
					else {
						name.setPadding(0, 0, PixelUtil.dipToPx(50), 0);
					}
					final TextView line = new TextView(context);
					final RelativeLayout.LayoutParams line_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
					line_lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					line.setBackgroundColor(getResources().getColor(R.color.detail_line));
					line.setLayoutParams(line_lp);
					((RelativeLayout) convertView).addView(name);
					((RelativeLayout) convertView).addView(iv);
					((RelativeLayout) convertView).addView(line);
				}
				else {
					final RelativeLayout.LayoutParams name_Lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
							PixelUtil.dipToPx(42));
					convertView = new RelativeLayout(context);
					convertView.setPadding(0, 0, PixelUtil.dipToPx(20), 0);
					final TextView name = new TextView(context);
					final DeleteButton db = new DeleteButton(context);
					final int buttonID = 184618345;
					db.setId(buttonID);
					name.setTextColor(0xff333333);
					name.setText(shortString(persons.get(position).getAddressBookItem().getName()));
					name_Lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					name_Lp.addRule(RelativeLayout.CENTER_VERTICAL);
					name_Lp.setMargins(20, 25, 10, 30);
					name_Lp.addRule(RelativeLayout.LEFT_OF, buttonID);
					name.setLayoutParams(name_Lp);
					name.setGravity(Gravity.CENTER_VERTICAL);
					final RelativeLayout.LayoutParams db_Lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					db_Lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					db_Lp.addRule(RelativeLayout.CENTER_VERTICAL);
					db_Lp.setMargins(0, 20, 0, 20);
					db.setLayoutParams(db_Lp);
					db.setOnConfirmClickListener(v -> {
						added.remove(position);
						persons.remove(position);
						PersonAdapter.this.notifyDataSetChanged();
					});
					final TextView line = new TextView(context);
					final RelativeLayout.LayoutParams line_lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
					line_lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					line_lp.leftMargin = PixelUtil.dipToPx(3);
					line.setBackgroundColor(getResources().getColor(R.color.detail_line));
					line.setLayoutParams(line_lp);
					((RelativeLayout) convertView).addView(name);
					((RelativeLayout) convertView).addView(db);
					((RelativeLayout) convertView).addView(line);
				}
			}
			else {
				((TextView) ((RelativeLayout) convertView).getChildAt(0))
						.setText(shortString(persons.get(position).getAddressBookItem().getName()));
				if (location == LEFT && (persons.get(position).getAddressBookItem().getType() == AddressBookType.Staff
						|| persons.get(position).getAddressBookItem().getType() == AddressBookType.Position)) {
					((RelativeLayout) convertView).getChildAt(1).setVisibility(View.VISIBLE);
					((RelativeLayout) convertView).getChildAt(0).setPadding(0, 0, PixelUtil.dipToPx(20), 0);
				}
				else if (location == LEFT) {
					((RelativeLayout) convertView).getChildAt(1).setVisibility(View.INVISIBLE);
					((RelativeLayout) convertView).getChildAt(0).setPadding(0, 0, 0, 0);
				}
				else if (location == RIGHT) {
					((DeleteButton) ((RelativeLayout) convertView).getChildAt(1)).recoverButtonImmediately();
					((DeleteButton) ((RelativeLayout) convertView).getChildAt(1)).setOnConfirmClickListener(v -> {
						added.remove(position);
						persons.remove(position);
						PersonAdapter.this.notifyDataSetChanged();
					});
				}
			}
			convertView.setBackgroundResource(R.drawable.listview_item_bg);
			return convertView;
		}

		void addPerson(AddressBookItem person) {
			final AddressBookListItem abli = new AddressBookListItem();
			abli.setAddressBookItem(person);
			persons.add(abli);
		}

		void addAllPerson(List<AddressBookItem> all) {
			for (final AddressBookItem addressBookItem : all) {
				final AddressBookListItem abli = new AddressBookListItem();
				abli.setAddressBookItem(addressBookItem);
				persons.add(abli);
			}
		}

		private String shortString(String s) {
			s = CommonUtil.toDBC(s);
			return s;
		}

		@Override
		public void refreshAdapter(ArrayList<AddressBookListItem> listDatas) {
			if (listDatas != null) {
				if (listDatas.size() == 0 && location == LEFT) {
					select_Lv.goBack();
					FEToast.showMessage(getResources().getString(R.string.flow_nocontent));
				}
				else {
					persons.clear();
					for (final AddressBookListItem listItem : listDatas) {
						persons.add(listItem);
					}
				}
				notifyDataSetChanged();
			}
		}
	}

	/**
	 * 获取表单加签的数据
	 * @return 用户选择结果
	 */
	public List<FormNodeItem> getResult() {
		final List<FormNodeItem> nodeItems = new ArrayList<>();
		WorkFlowTranslater.translateNode2FormSendDoItem(added, nodeItems, mCurrentFlowNodeGUID);
		return nodeItems;
	}
}

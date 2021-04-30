//
// feep
//
// Created by ZhongYJ on 2012-02-10.
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.feep.form;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.FormNodeResponse;
import cn.flyrise.android.protocol.entity.FormSendDoRequest;
import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.android.protocol.model.FormNodeItem.FromNodeType;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.common.X.FormNode;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.form.adapter.FormNodeAdapter;
import cn.flyrise.feep.form.adapter.FormSubNodeAdapter;
import cn.flyrise.feep.form.adapter.SpinnerAdapter;
import cn.flyrise.feep.form.been.FormDisposeData;
import cn.flyrise.feep.form.been.FormExitToNodeItem;
import cn.flyrise.feep.form.been.FormNodeToSubNode;
import cn.flyrise.feep.form.been.FormSubNodeInfo;
import cn.flyrise.feep.form.util.ChooseSubNodeUtil;
import cn.flyrise.feep.form.util.FormDataProvider;
import cn.flyrise.feep.utils.Patches;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FormSendToDisposeActivity extends BaseActivity {

	private LinearLayout mNodeLayout;

	private TextView nodeDisposeTv;
	private LinearLayout nodeDisposeLayout;
	private ListView mSubNodeList;
	private ListView mNodeList;
	private RadioGroup nodeChangeRG;
	private View errorLayout;

	private FEToolbar mToolBar;
	private RadioButton radioButton;
	private SpinnerAdapter mSpinnerAdapter;
	private FormNodeAdapter mNodeAdapter;
	private FormSubNodeAdapter subNodeAdapter;
	private boolean isPerson = true;

	private EditText mEditText;
	private ImageView mIvCelar;

	private UISwitchButton mMultichoice;
	private LinearLayout mMultichoiceLayout;
	private LinearLayout mPersonPostLayout;
	private boolean isMultichoice;
	private int mRequestType;
	private int mExitRequestType;

	private boolean isDispose;//标记是送办还是退回，true为送办
	private String mId;
	private String mSuggestion;
	private String mRequiredData;
	private boolean isWaiting, isTracing, isReturnTheNode;
	private FormDataProvider mDataProvider;
	private Resources resources;

	private int currentExitNodeIndex;//当前出口节点index
	private ChooseSubNodeUtil chooseSubNodeUtil;
	private ChooseSubNodeObjec chooseSubNodeObjec;

	private int currentNodeIndex;//当前出口节点index
	private ArrayList<FormExitToNodeItem> formNodeDatas;

	private int itemNums;//节点数
	private FormDisposeData disposeData;
	//用于保存新建出来的默认办理人
	private HashMap<FormNodeItem, List<FormSubNodeInfo>> defaultDisponeNode = new HashMap<>();


	public static void startActivity(Activity activity, FormDisposeData disposeData) {
		Intent intent = new Intent(activity, FormSendToDisposeActivity.class);
		intent.putExtra("dispose_data", GsonUtil.getInstance().toJson(disposeData));
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		activity.startActivity(intent);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.form_senddo);
		setTitle();
		requestDataWithType();
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;
	}

	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 10011) {
				ArrayList<FormNodeToSubNode> nodeItems = (ArrayList<FormNodeToSubNode>) msg.obj;
				if (nodeItems == null) return;
				mNodeList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
						, itemNums > 5 ? PixelUtil.dipToPx(36) * 5 : ViewGroup.LayoutParams.WRAP_CONTENT));
			}
		}
	};

	@Override
	public void bindView() {
		mNodeLayout = findViewById(R.id.node_layout);
		nodeDisposeTv = findViewById(R.id.node_dispose_text);
		nodeDisposeLayout = findViewById(R.id.form_choice_spinner_layout);
		mNodeList = findViewById(R.id.form_node_choice_listview);
		mSubNodeList = findViewById(R.id.form_deliver_choice_person_listview);// 人员或岗位list
		mMultichoice = findViewById(R.id.form_choice_morenode);
		mMultichoiceLayout = findViewById(R.id.form_choice_morenode_layout);
		mPersonPostLayout = findViewById(R.id.personandpost_Layout);
		nodeChangeRG = findViewById(R.id.form_dispose_radio_layout);
		errorLayout = findViewById(R.id.error_tip_lyt);
		radioButton = findViewById(R.id.form_dispose_radio_persion);
		mEditText = findViewById(R.id.etSearch);
		mIvCelar = findViewById(R.id.ivDeleteText);
	}

	@Override
	public void bindData() {
		super.bindData();
		getIntentData();
		mDataProvider = new FormDataProvider(this, mId, mGetDataHandler);
		resources = getResources();
		/* 选择节点工具 */
		chooseSubNodeUtil = new ChooseSubNodeUtil();
		chooseSubNodeObjec = new ChooseSubNodeObjec();
		mSpinnerAdapter = new SpinnerAdapter(this, new ArrayList<>(), isDispose);
		mNodeAdapter = new FormNodeAdapter(this);
		mNodeList.setAdapter(mNodeAdapter);
		subNodeAdapter = new FormSubNodeAdapter(this, chooseSubNodeObjec);
		mSubNodeList.setAdapter(subNodeAdapter);
	}

	private void getIntentData() {
		if (getIntent() == null) return;
		String data = getIntent().getStringExtra("dispose_data");
		if (TextUtils.isEmpty(data)) return;
		disposeData = GsonUtil.getInstance().fromJson(data, FormDisposeData.class);
		if (disposeData == null) return;
		mId = disposeData.id;
		mSuggestion = disposeData.content;
		mRequestType = disposeData.requestType;
		mExitRequestType = disposeData.exitRequestType;
		isWaiting = disposeData.isWait;
		isTracing = disposeData.isTrace;
		isReturnTheNode = disposeData.isReturnCurrentNode;
		isDispose = mRequestType != X.FormRequestType.Return;
		mRequiredData = disposeData.requiredData;
	}

	//设置标题
	private void setTitle() {
		this.mToolBar.setTitle(isDispose ? R.string.form_dispose_sendtodo : R.string.form_dispose_returnback);
	}

	//根据处理类型请求数据
	private void requestDataWithType() {
		LoadingHint.show(this);
		if (isDispose) {
			requestExitNode();
		}
		else {
			requestNodes("");
		}
	}

	@SuppressLint("ClickableViewAccessibility") @Override
	public void bindListener() {
		nodeDisposeLayout.setOnClickListener(v -> {
			new FEMaterialDialog.Builder(FormSendToDisposeActivity.this)
					.setTitle(getResources().getString(R.string.form_senddo_next))
					.setItemAdapter(mSpinnerAdapter)
					.setItems(true, null, (dialog, view, position) -> {
						dialog.dismiss();
						currentItem(position);
					})
					.build()
					.show();
		});
		/*-----多选-------*/
		mMultichoice.setOnCheckedChangeListener((buttonView, isChecked) -> isMultichoice = isChecked);
		mMultichoice.setOnClickListener(view -> chooseSubNodeObjec.clearCheckedNodes());
		/*----人员列表item点击-----*/
		mSubNodeList.setOnItemClickListener((parent, view, position, id) -> chooseSubNodeObjec.chooseSubNode(position));
		/*----节点列表item点击-----*/
		mNodeList.setOnItemClickListener((parent, view, position, id) -> {
			if (currentNodeIndex != position && !TextUtils.isEmpty(getEditText())) {
				mEditText.setText("");
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				if (imm.isActive(mEditText)) mEditText.setFocusable(false);
			}
			currentNodeIndex = position;
			mNodeAdapter.setSelectedPosition(position);
			isJsutSendPost();  // 增加默认岗位判断
		});
		/*----“人员与岗位”的切换-----*/
		nodeChangeRG.setOnCheckedChangeListener((group, checkedId) -> {
			if (!TextUtils.isEmpty(getEditText())) {
				mEditText.setText("");
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				if (imm.isActive(mEditText)) mEditText.setFocusable(false);
			}
			isPerson = checkedId == R.id.form_dispose_radio_persion;// 判读是否是人员节点
			requestSubNodes();
		});
		/*----发送按钮-----*/
		mToolBar.setRightText(R.string.form_submit);
		mToolBar.setRightTextClickListener(v -> submit());

		errorLayout.setOnClickListener(v -> requestDataWithType());

		mIvCelar.setOnClickListener(v -> mEditText.setText(""));
		mEditText.setOnFocusChangeListener((v, hasFocus) -> onEditHasFouce(hasFocus));
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String input = s.toString().trim();
				if (TextUtils.isEmpty(input)) {
					subNodeAdapter.refreshDatas(getSubNodes(), null);
					mIvCelar.setVisibility(View.GONE);
				}
				else {
					mIvCelar.setVisibility(View.VISIBLE);
					List<FormSubNodeInfo> subNodeInfoList = getSubNodes();
					if (CommonUtil.isEmptyList(subNodeInfoList)) return;
					subNodeAdapter.refreshDatas(subNodeInfoList, input);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mSubNodeList.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				if (imm.isActive(mEditText)) mEditText.setFocusable(false);
			}
			return false;
		});
	}

	private void currentItem(int position) {
		clearAllNodeNoCheckState();
		currentExitNodeIndex = position;
		currentNodeIndex = 0;/* 每次点击出口，初始化节点index为0 */
		mNodeAdapter.setSelectedPosition(currentNodeIndex);
		mMultichoice.setChecked(false);
		subNodeAdapter.refreshDatas(null, null);/* 每次点击出口，情况人员列表 */
		mSpinnerAdapter.setSelectedItemPosition(position);
		chooseSubNodeUtil.clearCheckSubNodes();
		switchTypeClearSelectedData();
		subNodeAdapter.setAllNodeNoCheckState(getSubNodes());
		isJsutSendPost();
		String name = mSpinnerAdapter.getItem(position);
		nodeDisposeTv.setText(name);
		if (isDispose) {
			requestNodes(mDataProvider.getExitItems().get(position).getKey());
		}
		else {
			requestSubNodes();
			final FormNodeToSubNode nodeToSubNode = (FormNodeToSubNode) mSpinnerAdapter.getDataItem(position);
			final FormNodeItem nodeItem = nodeToSubNode.getFormNodeItem();
			mNodeAdapter.refreshSendBackDatas(nodeItem);
			Message mes = handler.obtainMessage();
			mes.what = 10010;
			mes.obj = nodeItem;
			handler.sendMessage(mes);
		}
		if (getSubNodes() != null) {
			addDefaultNodes();
		}
	}

	//请求出口节点
	private void requestExitNode() {
		mDataProvider.requestExport(mExitRequestType);
	}

	/**
	 * 请求出口到节点<br>
	 * RequiredData，目的是将用户在js上做的操作保存到服务器，以便和pc端的一样 <br>
	 * 修改于 2014 - 11 - 09
	 */
	private void requestNodes(String exitID) {
		mNodeAdapter.refreshDatas(null);
		if (!isDispose || !isHadLoadNode()) {
			LoadingHint.show(this);
			mDataProvider.requsetNode(mRequestType, exitID, mRequiredData);
		}
		else {
			final FormExitToNodeItem exitToNodeItems = formNodeDatas.get(currentExitNodeIndex);
			final ArrayList<FormNodeToSubNode> nodeToSubNodes = exitToNodeItems.getNodeItems();
			mNodeAdapter.refreshDatas(nodeToSubNodes);
			Message mes = handler.obtainMessage();
			mes.what = 10011;
			mes.obj = nodeToSubNodes;
			handler.sendMessage(mes);
			requestSubNodes();
		}
	}

	//请求节点到人员
	private void requestSubNodes() {
		subNodeAdapter.refreshDatas(null, null);
		final FormNodeToSubNode nodToSubNode = getNodeToSubNode();// 获取“节点到人员”数据
		changMultipleView(nodToSubNode);// 判断是否隐藏“多选”choosebox
		/*--- 如果是人员就更加人员节点来判读，否则就根据岗位节点来判断---*/
		if (isPerson ? isHadLoadPersonNode() : isHadLoadPostionNode()) {// 如果数据已经加载过了，就不再加载数据
			if (nodToSubNode != null) {
				/* 更加人员节点类型来刷新列表 */
				subNodeAdapter.refreshDatas(isPerson
						? nodToSubNode.getPersonSubNodes()
						: nodToSubNode.getPositionSubNodes(), mEditText.getText().toString().trim());
			}
		}
		else {// 请求人员节点数据
			if (nodToSubNode != null) {
				LoadingHint.show(this);
				final int addressBookType = isPerson ? AddressBookType.Staff : AddressBookType.Position;
				final FormNodeItem nodeItem = nodToSubNode.getFormNodeItem();
				final String nodeId = nodeItem.getId();
				mDataProvider.requestSubnode(mRequestType, addressBookType, nodeId, mEditText.getText().toString().trim());
			}
		}
	}

	//判断是否已经加载相应的节点数据
	private boolean isHadLoadNode() {
		final ArrayList<FormNodeToSubNode> nodeToSubNodes = getCurrentNodes();
		return nodeToSubNodes != null;
	}

	//从总数据中获取“节点”的数据
	private FormNodeToSubNode getNodeToSubNode() {
		final ArrayList<FormNodeToSubNode> nodeToSubNodes = formNodeDatas.get(getExitIdex()).getNodeItems();
		if (nodeToSubNodes == null) return null;
		itemNums = nodeToSubNodes.size();
		return nodeToSubNodes.get(getNodeIdex());
	}

	//从节点中获取“人员节点”的数据
	private List<FormSubNodeInfo> getSubNodes() {
		final FormNodeToSubNode nodeToSubNode = getNodeToSubNode();
		if (nodeToSubNode == null) return null;
		return isPerson ? nodeToSubNode.getPersonSubNodes() : nodeToSubNode.getPositionSubNodes();
	}

	//当选择不同出口时，还原默认值 遍历节点，为每个节点添加默认办理人
	private void addDefaultNodes() {
		try {
			final FormNodeItem nodeItem = getNodeToSubNode().getFormNodeItem();
			final String figureID = nodeItem.getFigureID();
			if (!TextUtils.isEmpty(figureID)) {
				boolean hasMoreDefault = figureID.contains(",");
				if (hasMoreDefault && mMultichoiceLayout.getVisibility() == View.GONE) {
					return;
				}
				mMultichoice.setChecked(hasMoreDefault);
				List<FormSubNodeInfo> defaultNodes = createDefaultNode(nodeItem);
				if (!CommonUtil.isEmptyList(defaultNodes)) {
					clearAllNodeNoCheckState();
					for (FormSubNodeInfo defaultNode : defaultNodes) {
						defaultNode.setNeedAddState(false);
						if (defaultNode.getReferenceItem() != null)
							setDefaultItemChecked(nodeItem.getFigureType(), defaultNode.getReferenceItem().getKey());
						final int index = chooseSubNodeUtil.getContainsIndex(getNodeIdex(), defaultNode);
						if (index == -1) {// 判断已选的节点中是否已经包含了默认办理人，如果不包含就添加默认办理人
							/*---默认选择默认办理人---*/
							chooseSubNodeUtil.chooseNode(getNodeIdex(), defaultNode, isMultichoice);
						}
						mNodeAdapter.addNodesString(chooseSubNodeUtil.getNodesString(getNodeIdex()), currentNodeIndex);
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void setDefaultItemChecked(String type, String figureID) {
		if (formNodeDatas == null || formNodeDatas.size() <= currentExitNodeIndex || formNodeDatas.get(currentExitNodeIndex) == null)
			return;
		final ArrayList<FormNodeToSubNode> list = formNodeDatas.get(currentExitNodeIndex).getNodeItems();
		if (CommonUtil.isEmptyList(list)) return;
		for (int i = 0; i < list.size(); i++) {
			final FormNodeToSubNode subNode = list.get(i);
			setDefaultDisponeNodes(TextUtils.equals(type, "1") ? subNode.getPersonSubNodes()
					: subNode.getPositionSubNodes(), figureID);
		}
	}

	private void setDefaultDisponeNodes(List<FormSubNodeInfo> subNodes, String figureID) {
		if (subNodes == null) return;
		for (final FormSubNodeInfo subNodeInfo : subNodes) {
			if (subNodeInfo == null || subNodeInfo.getReferenceItem() == null) continue;
			setDefaultFormSubNodeInfo(subNodeInfo, figureID);
		}
	}

	private void setDefaultFormSubNodeInfo(final FormSubNodeInfo subNodeInfo, String figureID) {
		if (TextUtils.equals(subNodeInfo.getReferenceItem().getKey(), figureID)) {
			subNodeInfo.setNeedAddState(false);
		}
	}

	//当节点请求完毕后立即为每个节点设置默认的办理人或岗位
	private void setDefalutNodeValue() {
		final ArrayList<FormNodeToSubNode> nodeList = formNodeDatas.get(getExitIdex()).getNodeItems();
		final List<FormNodeToSubNode> finalNodeList = new ArrayList<>();
		for (FormNodeToSubNode subNode : nodeList) {
			final FormNodeItem node = subNode.getFormNodeItem();
			if (TextUtils.isEmpty(node.getFigureName()) || TextUtils.isEmpty(node.getFigureID())) {
				continue;
			}
			else {
				finalNodeList.add(subNode);
			}
		}
		for (FormNodeToSubNode node : finalNodeList) {
			FormNodeItem nodeInfo = node.getFormNodeItem();
			List<FormSubNodeInfo> subNodeInfoList = createDefaultNode(nodeInfo);
			if (!CommonUtil.isEmptyList(subNodeInfoList)) {
				int nodeIndex = nodeList.indexOf(node);
				isMultichoice = subNodeInfoList.size() > 1;
				for (FormSubNodeInfo defaultNode : subNodeInfoList) {
					chooseSubNodeUtil.chooseNode(nodeIndex, defaultNode, isMultichoice);
					mNodeAdapter.addNodesString(chooseSubNodeUtil.getNodesString(nodeIndex), nodeIndex);
				}
			}
		}
	}

	/**
	 * 当人员节点中不包含"默认办理人"时，利用"默认办理人"创建一个FormSubNodeInfo类型的人员节点
	 * @param nodeItem 出口到节点的节点数据 默认办理人的id
	 */
	private List<FormSubNodeInfo> createDefaultNode(FormNodeItem nodeItem) {
		if (defaultDisponeNode.containsKey(nodeItem)) {
			return defaultDisponeNode.get(nodeItem);
		}
		List<FormSubNodeInfo> defaultSubNodes = new ArrayList<>();
		if (TextUtils.isEmpty(nodeItem.getFigureID())) return null;
		if (nodeItem.getFigureID().contains(",")) {
			String[] defaultUserIDs = nodeItem.getFigureID().split(",");
			String[] positionNames = nodeItem.getFigureName().split(",");
			for (int i = 0; i < defaultUserIDs.length; i++) {
				defaultSubNodes.add(getFormSubNodeInfos(nodeItem, defaultUserIDs[i]
						, getPositionVauleName(nodeItem.getFigureName(), i, positionNames), positionNames[i]));
			}
		}
		else {
			defaultSubNodes.add(getFormSubNodeInfo(nodeItem, nodeItem.getFigureID(), nodeItem.getFigureName()));
		}
		defaultDisponeNode.put(nodeItem, defaultSubNodes);
		return defaultSubNodes;
	}

	private FormSubNodeInfo getFormSubNodeInfo(FormNodeItem nodeItem, String figureID, String figureName) {
		FormSubNodeInfo subNodeInfo = new FormSubNodeInfo();
		ReferenceItem item = new ReferenceItem();
		item.setKey(figureID);
		if (TextUtils.equals("1", nodeItem.getFigureType()))
			item.setValue(getPersonVauleName(figureID, figureName));
		else item.setValue(nodeItem.getFigureName());
		subNodeInfo.setNodeType("1".equals(nodeItem.getFigureType()) ? AddressBookType.Staff
				: AddressBookType.Position);
		subNodeInfo.setNodeItem(nodeItem);
		subNodeInfo.setReferenceItem(item);
		return subNodeInfo;
	}

	private FormSubNodeInfo getFormSubNodeInfos(FormNodeItem nodeItem, String figureID, String positionName, String figureName) {
		ReferenceItem item = new ReferenceItem();
		item.setKey(figureID);
		if (TextUtils.equals("1", nodeItem.getFigureType())) item.setValue(getPersonVauleName(figureID, figureName));
		else item.setValue(positionName);
		FormSubNodeInfo subNodeInfo = new FormSubNodeInfo();
		subNodeInfo.setNodeType("1".equals(nodeItem.getFigureType()) ? AddressBookType.Staff : AddressBookType.Position);
		subNodeInfo.setNodeItem(nodeItem);
		subNodeInfo.setReferenceItem(item);
		return subNodeInfo;
	}

	private String getPositionVauleName(String figureName, int index, String[] positionNames) {
		if (positionNames == null || positionNames.length <= index) return figureName;
		return positionNames[index];
	}

	private String getPersonVauleName(String figureID, String figureName) {
		if (!TextUtils.isEmpty(figureName)) {
			return figureName;
		}
		AddressBook addressBook = CoreZygote.getAddressBookServices().queryUserInfo(figureID);
		if (addressBook != null) return addressBook.name;
		else return figureName;
	}


	/**
	 * 判断是否已经加载相应的人员节点数据
	 */
	private boolean isHadLoadPersonNode() {
		if (isHadLoadNode()) {
			final FormNodeToSubNode nodeToSubNode = getCurrentNodes().get(getNodeIdex());
			if (nodeToSubNode != null && CommonUtil.nonEmptyList(nodeToSubNode.getPersonSubNodes())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否已经加载相应的岗位节点数据
	 */
	private boolean isHadLoadPostionNode() {
		if (isHadLoadNode()) {
			final FormNodeToSubNode nodeToSubNode = getCurrentNodes().get(getNodeIdex());
			if (nodeToSubNode != null && CommonUtil.nonEmptyList(nodeToSubNode.getPositionSubNodes())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取当前出口节点数据
	 */
	private ArrayList<FormNodeToSubNode> getCurrentNodes() {
		final FormExitToNodeItem nodeItem = formNodeDatas.get(getExitIdex());
		if (nodeItem == null) return null;
		return nodeItem.getNodeItems();
	}

	/**
	 * 根据处理方式来获取不同的出口索引位置
	 */
	private int getExitIdex() {
		return isDispose ? currentExitNodeIndex : 0;
	}

	/**
	 * 根据处理方式来获取不同的节点索引位置
	 */
	private int getNodeIdex() {
		int index = isDispose ? currentNodeIndex : currentExitNodeIndex;
		try {
			final ArrayList<FormNodeToSubNode> nodeToSubNodes = formNodeDatas.get(getExitIdex()).getNodeItems();
			final FormNodeItem nodeItem = nodeToSubNodes.get(index).getFormNodeItem();
			final FromNodeType nodeType = nodeItem.getType();
			if (nodeType == FromNodeType.FromNodeTypeLogic && nodeToSubNodes.size() - 1 > index) {// 判断是否是逻辑节点
				index = index + 1;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return index;
	}

	/**
	 * 表单退回时，遍历判断哪一个才是默认办理节点,并返回索引位置
	 * @param nodeItems 人员节点数据集合
	 */
	private int getDefaultNodeIndex(ArrayList<FormNodeToSubNode> nodeItems) {
		int defaultNodeCount = 0;
		if (nodeItems == null) return defaultNodeCount;
		for (int i = 0; i < nodeItems.size(); i++) {
			final FormNodeToSubNode nodeToSubNode = nodeItems.get(i);
			if (nodeToSubNode != null) {
				final FormNodeItem item = nodeToSubNode.getFormNodeItem();
				if (item.isDefaultNode()) defaultNodeCount = i;
			}
		}
		return defaultNodeCount;
	}

	/**
	 * 响应服务器返回来的数据的handler
	 */
	@SuppressLint("HandlerLeak") private final Handler mGetDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				formNodeDatas = mDataProvider.getFormExitToNodeItems();
				setErrorVisible(true);
				switch (msg.what) {
					case FormDataProvider.EXIT_HANDLER_WHAT:// 出口的响应
						mSpinnerAdapter.refreshData(mDataProvider.getExitItems());
						currentItem(0);
						break;
					case FormDataProvider.NODE_HANDLER_WHAT:// 出口到节点的响应
						final ArrayList<FormNodeToSubNode> nodeItems = formNodeDatas.get(getExitIdex()).getNodeItems();
						if (isDispose) {
							mNodeAdapter.refreshDatas(nodeItems);
							Message mes = handler.obtainMessage();
							mes.what = 10011;
							mes.obj = nodeItems;
							handler.sendMessage(mes);
							setDefalutNodeValue();
							/*---加载人员数据或者刷新人员列表---*/
							// 增加默认岗位判断
							isJsutSendPost();
							if (isSendTodo(nodeItems)) submit();
						}
						else {
							mSpinnerAdapter.refreshData(nodeItems);
							final FormNodeItem nodeItem = nodeItems.get(currentExitNodeIndex).getFormNodeItem();
							mNodeAdapter.refreshSendBackDatas(nodeItem);
							Message mes = handler.obtainMessage();
							mes.what = 10010;
							mes.obj = nodeItem;
							handler.sendMessage(mes);
							currentItem(getDefaultNodeIndex(nodeItems));
						}
						break;
					case FormDataProvider.PERSON_HANDLER_WHAT:// 节点到人员节点的响应
						addDefaultNodes();
						subNodeAdapter.refreshDatas(getSubNodes(), getEditText());
						break;
					default:
						break;
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	};

	private boolean isSendTodo(ArrayList<FormNodeToSubNode> nodeItems) {
		if (CommonUtil.isEmptyList(nodeItems)) return false;
		FormNodeToSubNode item = nodeItems.get(getNodeIdex());
		if (item == null) return false;
		FormNodeItem nodeItem = item.getFormNodeItem();
		return nodeItem != null && !TextUtils.isEmpty(nodeItem.getFigureID())
				&& !TextUtils.isEmpty(nodeItem.getFigureName()) && nodeItem.isTodo();
	}

	private String getEditText() {
		return mEditText != null ? mEditText.getText().toString().trim() : "";
	}

	/**
	 * 设置加载出错页面的显示与隐藏
	 */
	private void setErrorVisible(boolean isShowTig) {
		if (formNodeDatas == null || formNodeDatas.size() == 0) {
			errorLayout.setVisibility(View.VISIBLE);
			mToolBar.getRightTextView().setVisibility(View.GONE);
		}
		else {
			errorLayout.setVisibility(View.GONE);
			mToolBar.getRightTextView().setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 判断是否是多审批节点 并做相关操作 :如果是多审批节点就让“多选”choosebox出现，否则隐藏
	 * @param nodToSubNode 节点数据
	 */
	private void changMultipleView(FormNodeToSubNode nodToSubNode) {
		if (nodToSubNode != null) {
			final FormNodeItem nodeItem = nodToSubNode.getFormNodeItem();
			if (nodeItem != null) {
				isMultichoice = false;
				final FromNodeType nodeType = nodeItem.getType();
				if (!isDispose) {
					mMultichoiceLayout.setVisibility(View.GONE);
					mPersonPostLayout.setVisibility(View.GONE);
				}
				else if (nodeType == FromNodeType.FromNodeTypeUnion) {
					mMultichoiceLayout.setVisibility(View.GONE);
					mPersonPostLayout.setVisibility(View.GONE);
				}
				else if (nodeType == FromNodeType.FromNodeTypeMultiNode) {// 判断是否是多审批节点
					if (FunctionManager.hasPatch(Patches.PATCH_FORM_MORE_PERSON) || itemNums == 1) {
						mMultichoiceLayout.setVisibility(View.VISIBLE);
					}
					mPersonPostLayout.setVisibility(View.VISIBLE);
					if (mMultichoice != null) {
						isMultichoice = mMultichoice.isChecked();
					}
				}
				else if (nodeType == FromNodeType.FromNodeTypeEnd) {
					mPersonPostLayout.setVisibility(View.GONE);
					mMultichoiceLayout.setVisibility(View.GONE);
				}
				else {
					mMultichoiceLayout.setVisibility(View.GONE);
					mPersonPostLayout.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	//提交
	private void submit() {
		if (!mDataProvider.isAllowSend) {
			FEToast.showMessage(resources.getString(R.string.send_loader_hint));
			return;
		}
		mDataProvider.isAllowSend = false;
		try {
			int nodeCount = 0;// 计算当前节点对应的选中的子节点的数量
			final FormNodeResponse formNodeResponse = formNodeDatas.get(getExitIdex()).getFormNodeResponse();
			final List<FormNodeItem> nodeItems = formNodeResponse.getNodes();
			for (int i = 0; i < nodeItems.size(); i++) {
				final FormNodeItem nodeItem = nodeItems.get(i);
				final ArrayList<FormSubNodeInfo> checkedNodes = chooseSubNodeUtil.getCheckedSubNodes().get(i);
				final String value = getNodeValue(checkedNodes, nodeItem);
				if (value.length() != 0 && isNodeNameNoNull(checkedNodes, nodeItem)) {
					nodeItem.setValue(value);
					nodeCount++;
				}
			}
			final FromNodeType nodeType = nodeItems.get(getNodeIdex()).getType();
			if (nodeCount == 0 && (nodeType != FromNodeType.FromNodeTypeUnion && nodeType != FromNodeType.FromNodeTypeEnd
					&& nodeType != FromNodeType.FromNodeTypeOrion)) {
				// 如果当前没有选择人员并且节点类型不为“并节点”或“结束节点”，就提示用户选择人员再提交
				FEToast.showMessage(resources.getString(R.string.form_dispose_no_node));
				mDataProvider.isAllowSend = true;
				return;
			}
			mDataProvider.submit(getFormRequest(nodeItems), disposeData == null ? null : disposeData.attachemnts);
		} catch (final Exception e) {
			e.printStackTrace();
			mDataProvider.isAllowSend = true;
			if (LoadingHint.isLoading()) LoadingHint.hide();
		}
	}

	private FormSendDoRequest getFormRequest(final List<FormNodeItem> nodeItems) {
		final FormSendDoRequest request = new FormSendDoRequest();
		request.setRequestType(mRequestType);
		request.setId(mId);
		request.setDealType(FormNode.Normal);
		request.setSuggestion(mSuggestion);
		request.setTrace(isTracing);
		request.setWait(isWaiting);
		request.setRequiredData(mRequiredData);
		request.setReturnCurrentNode(isReturnTheNode);
		request.setNodes(nodeItems);
		if (mSuggestion != null && mSuggestion.startsWith("FEHandwrittenGUID=")) { // 意见为手写
			final String attachmentGUID = mSuggestion.replace("FEHandwrittenGUID=", "");
			request.setAttachmentGUID(attachmentGUID);
			request.setSuggestion("");
		}
		return request;
	}

	// 增加默认岗位判断
	private void isJsutSendPost() {
		if (formNodeDatas == null || formNodeDatas.size() <= currentExitNodeIndex || formNodeDatas.get(currentExitNodeIndex) == null)
			return;
		try {
			final FormExitToNodeItem exitToNodeItems = formNodeDatas.get(currentExitNodeIndex);
			final ArrayList<FormNodeToSubNode> subnodelist = exitToNodeItems.getNodeItems();
			if (currentNodeIndex >= 0 && subnodelist != null && subnodelist.size() >= 0) {
				final FormNodeToSubNode subNode = subnodelist.get(currentNodeIndex);
				if (subNode != null && subNode.getFormNodeItem() != null && subNode.getFormNodeItem().isSendPost()) {
					isPerson = false;
					nodeChangeRG.check(R.id.form_dispose_radio_position);
					radioButton.setVisibility(View.GONE);
				}
				else {
					radioButton.setVisibility(View.VISIBLE);
				}
			}
			requestSubNodes();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void clearAllNodeNoCheckState() {
		if (formNodeDatas == null || formNodeDatas.size() <= currentExitNodeIndex || formNodeDatas.get(currentExitNodeIndex) == null)
			return;
		try {
			final ArrayList<FormNodeToSubNode> list = formNodeDatas.get(currentExitNodeIndex).getNodeItems();
			if (list == null) return;
			for (int i = 0; i < list.size(); i++) {
				final FormNodeToSubNode subNode = list.get(i);
				setNoCheckState(subNode.getPersonSubNodes());
				setNoCheckState(subNode.getPositionSubNodes());
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	//设置所有的节点状态为未选状态
	private void setNoCheckState(List<FormSubNodeInfo> subNodeInfos) {
		if (subNodeInfos != null) {
			for (final FormSubNodeInfo subNodeInfo : subNodeInfos) {
				if (!subNodeInfo.isNeedAddState()) subNodeInfo.setNeedAddState(true);
			}
		}
	}

	/**
	 * 获取当前节点（nodeItem）的所选择的所有子节点拼凑成的value，格式如X531,554或者Y598,Y589
	 * @param checkedNodes 已选中的子节点数据
	 * @param nodeItem 当前节点数据
	 */
	private String getNodeValue(ArrayList<FormSubNodeInfo> checkedNodes, FormNodeItem nodeItem) {
		final StringBuilder value = new StringBuilder();
		if (checkedNodes != null) {
			final int size = checkedNodes.size();
			for (int i = 0; i < size; i++) {
				final FormSubNodeInfo info = checkedNodes.get(i);
				if (nodeItem != null && nodeItem.equals(info.getNodeItem())) {
					final ReferenceItem item = info.getReferenceItem();
					final String v = (info.getNodeType() == AddressBookType.Staff ? ("X" + item.getKey())
							: ("Y" + item.getKey()));
					value.append(v).append(i == (size - 1) ? "" : ",");
				}
			}
		}
		return value.toString();
	}

	private boolean isNodeNameNoNull(ArrayList<FormSubNodeInfo> checkedNodes, FormNodeItem nodeItem) {
		if (checkedNodes == null) return false;
		final int size = checkedNodes.size();
		for (int i = 0; i < size; i++) {
			final FormSubNodeInfo info = checkedNodes.get(i);
			if (nodeItem != null && nodeItem.equals(info.getNodeItem())) {
				final ReferenceItem item = info.getReferenceItem();
				if (!TextUtils.isEmpty(item.getValue())) return true;
			}
		}
		return false;
	}

	/**
	 * 选中人员节点类：用于其他类比如适配器调用某方法选中节点
	 */
	public class ChooseSubNodeObjec {

		void chooseSubNode(int position) {   //选择人员节点时调用此方法
			if (!isMultichoice) {
				switchTypeClearSelectedData();
				subNodeAdapter.setAllNodeNoCheckState(getSubNodes());
			}
			final FormSubNodeInfo subNodeInfo = subNodeAdapter.getItem(position);
			final ReferenceItem referenceItem = subNodeInfo.getReferenceItem();
			if (referenceItem == null || referenceItem.getKey() == null)
				return;
			final boolean isChecked = chooseSubNodeUtil.chooseNode(getNodeIdex(), subNodeInfo, isMultichoice);
			refreshNodeAdapter();
			subNodeAdapter.changeState(position, isChecked);
		}

		void clearCheckedNodes() {//清空当前节点下的所有选中的人员或岗位
			chooseSubNodeUtil.clearCheckSubNodesWithIndex(getNodeIdex());
			refreshNodeAdapter();
			switchTypeClearSelectedData();
			subNodeAdapter.setAllNodeNoCheckState(getSubNodes());
		}

		private void refreshNodeAdapter() { //刷新Adapter
			final String chooseNodeName = chooseSubNodeUtil.getNodesString(getNodeIdex());
			mNodeAdapter.addNodesString(chooseNodeName, currentNodeIndex);
		}

	}

	private void switchTypeClearSelectedData() {
		final FormNodeToSubNode nodeToSubNode = getNodeToSubNode();
		if (nodeToSubNode == null) return;
		List<FormSubNodeInfo> persons = nodeToSubNode.getPersonSubNodes();
		if (!CommonUtil.isEmptyList(persons)) {
			for (final FormSubNodeInfo subNodeInfo : persons) {
				if (!subNodeInfo.isNeedAddState()) subNodeInfo.setNeedAddState(true);
			}
		}
		List<FormSubNodeInfo> positons = nodeToSubNode.getPositionSubNodes();
		if (!CommonUtil.isEmptyList(positons)) {
			for (final FormSubNodeInfo subNodeInfo : positons) {
				if (!subNodeInfo.isNeedAddState()) subNodeInfo.setNeedAddState(true);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mSpinnerAdapter != null) mSpinnerAdapter = null;
		if (mNodeAdapter != null) mNodeAdapter = null;
		if (subNodeAdapter != null) subNodeAdapter = null;
		if (defaultDisponeNode != null) defaultDisponeNode = null;
		if (formNodeDatas != null) formNodeDatas = null;
		if (chooseSubNodeObjec != null) chooseSubNodeObjec = null;
		if (chooseSubNodeUtil != null) chooseSubNodeUtil = null;
		if (resources != null) resources = null;
		if (mDataProvider != null) {
			mDataProvider.onDestory();
			mDataProvider = null;
		}
	}


	private void onEditHasFouce(boolean hasFouce) {
		if (!hasFouce) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
			mGetDataHandler.postDelayed(() -> mNodeLayout.setVisibility(View.VISIBLE), 300);
			mEditText.setFocusable(true);
			mEditText.setFocusableInTouchMode(true);
		}
		else {
			mNodeLayout.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.FormSendToDispose);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.FormSendToDispose);
	}
}

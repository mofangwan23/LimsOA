/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-7
 */
package cn.flyrise.feep.collaboration.view.workflow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.AnimationSimple;
import cn.flyrise.android.library.utility.interpolator.BackInterpolator;
import cn.flyrise.android.library.view.BubbleWindow;
import cn.flyrise.android.library.view.OmnidirectionalScrollView;
import cn.flyrise.android.protocol.model.Flow;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.view.Avatar;
import cn.flyrise.feep.collaboration.view.workflow.WorkFlowNode.NodeType;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.watermark.WMStamp;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>类功能描述：</b><div style="margin-left:40px;margin-top:-10px"> 工作流程图显示与操作控件 </div>
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class WorkFlowView extends OmnidirectionalScrollView {

	private Flow flow;                                                          // 流程图数据模型
	static WorkFlowNode rootNode;                                                      // 转换后节点数据模型
	final RelativeLayout content_Rl;                                                    // 内部所有控件容器
	ImageView nodeSelected_Iv;                                               // 选中节点提示框
	ImageView nodeDelete_Iv;                                                 // 删除节点提示框
	View spaceRight;                                                    // 左右两侧占位控件...
	RelativeLayout.LayoutParams spaceRightLp, spaceLeftLp;
	static List<WorkFlowNode> allNode = new ArrayList<>();         // 所有节点用list保存返回,尽可能少递归遍历
	public static int nodeCount;
	public static WorkFlowNode currentNode;                                                   // 当前提示框选中的节点
	private WorkFlowNode addingNode;                                                    // 新添加的节点
	private static int cellWidth;
	private static int cellHeight;                                        // 单元格的宽与高，默认根据Icon大小1:1而定
	private boolean touchable = true;
	private static int paddingTop;
	private static int paddingBottom;
	private static int paddingLeft;
	private static int paddingRight;         // 上下左右边距间隔
	private final int hintHeight, hintWidth;                                        // 提示框的宽高
	private boolean changing = false;                                  // 能否改变提示框位置
	private final AccelerateDecelerateInterpolator i = new AccelerateDecelerateInterpolator(); // 移动动画插值器
	private final Animation aa = AnimationSimple.disappear(null, 10);
	private final ScaleAnimation sa = AnimationSimple.scale(null, 300, 0, 1);
	boolean seleted = true;                                   // 流程图是否可选择
	boolean lock = false;                                  // 是否锁定已存在节点,用于加签
	private static Flow lastProcessItem;

	private BubbleWindow mBubbleWindow;
	private TextView mTvBubbleText;
	private ImageView mIvBubbleAvatar;
	private TextView mTvBubbleSubText;

	private boolean notScroll;                                                     // 拖动头像就消耗触屏事件不让左边的列表能够滚动
	private boolean doingMovingAnimation = false;                                  // 动画移动头像中,不接受触屏事件
	private Avatar addingAvatar;                                                  // 紧贴手指滑动的头像,新的实现采用View.layout(),去除LayoutParams,更流畅了
	private boolean doOnce;                                                        // 设置左边距,设置一次就好
	private FrameLayout.LayoutParams addingAvatarLP;
	private final int[] from = new int[2];                             // 记录手指的头像位置
	private final int[] to = new int[2];                             // 记录新加节点的头像位置
	private int locationLeft, locationTop, locationRight, locationBottom;      // 记录自己在屏幕上的位置
	private final Context mContext;

	public WorkFlowView(Context context) {
		this(context, null);
	}

	public WorkFlowView(Flow flow, String currentUserGUID, Context context) {
		this(context, null);
		setInitData(flow, currentUserGUID);
	}

	public WorkFlowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		/*--初始化部分参数--*/
		mContext = context;
		paddingTop = paddingBottom = (int) getResources().getDimension(R.dimen.mdp_25);
		paddingLeft = paddingRight = (int) getResources().getDimension(R.dimen.mdp_10);
		cellWidth = (int) getResources().getDimension(R.dimen.mdp_45);
		cellHeight = (int) getResources().getDimension(R.dimen.mdp_45);
		hintHeight = getResources().getDrawable(R.drawable.focus_head_fe).getIntrinsicHeight();
		hintWidth = getResources().getDrawable(R.drawable.focus_head_fe).getIntrinsicWidth();
		sa.setInterpolator(new BackInterpolator(1.5f));
		/*--End--*/
		content_Rl = new RelativeLayout(context);
		addView(content_Rl);
		initBubbleWindow();
		content_Rl.setWillNotCacheDrawing(true);
		content_Rl.setDrawingCacheEnabled(false);
		doOnce = true;
	}

	private final int[] location = new int[2]; // 保存自己在屏幕的位置

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (hasWindowFocus && doOnce) {
			content_Rl.setMinimumHeight(getHeight());
			content_Rl.setMinimumWidth(getWidth());
			getLocationOnScreen(location);
			doOnce = false;
		}
		super.onWindowFocusChanged(hasWindowFocus);
	}

	private void initBubbleWindow() {
		View view = View.inflate(getContext(), R.layout.view_popup_flow, null);
		mTvBubbleText = (TextView) view.findViewById(R.id.tvFlowName);
		mIvBubbleAvatar = (ImageView) view.findViewById(R.id.ivFlowAvatar);
		mTvBubbleSubText = (TextView) view.findViewById(R.id.tvFlowState);

		mBubbleWindow = new BubbleWindow(view);
		mBubbleWindow.setDismissWithoutAnima(true);
		mBubbleWindow.setContentViewFocusable(false);
	}

	/**
	 * 获取当前显示流程图结果数据
	 */
	public Flow getResult() {
		WorkFlowTranslater.translateNode2ProcessItem(rootNode, flow);
		lastProcessItem = flow;
		return flow;
	}

	/**
	 * 设置流程图数据
	 */
	public void setInitData(Flow flow, String currentFlowNodeGUID) {
		this.flow = flow;
		nodeSelected_Iv = new ImageView(getContext());
		nodeDelete_Iv = new ImageView(getContext());
		/*--数据模型转换--*/
		if (lastProcessItem == null || lastProcessItem != flow) {
			rootNode = new WorkFlowNode();
			currentNode = rootNode;
			allNode = WorkFlowTranslater.translateProcessItem2Node(flow, rootNode, currentFlowNodeGUID);
			nodeCount = allNode.size();
		}
		/*--End--*/
		/*--初始化相关控件--*/
		nodeSelected_Iv.setImageResource(R.drawable.focus_head_fe);
		nodeDelete_Iv.setImageResource(R.drawable.icon_wrong);
		int padding = (int) getResources().getDimension(R.dimen.mdp_5);
		nodeDelete_Iv.setPadding(padding, padding, padding, padding);
		nodeDelete_Iv.setVisibility(View.GONE);
		nodeDelete_Iv.setOnClickListener(v -> {
			if (lock && (currentNode.getNodeType() == NodeType.locked || currentNode.getNodeType() == NodeType.existed
					|| currentNode.getNodeType() == NodeType.user)) {
				FEToast.showMessage(getResources().getString(R.string.flow_nodeunmodify));
				nodeDelete_Iv.setVisibility(View.GONE);
				return;
			}
			deleteNode(currentNode);
			nodeDelete_Iv.setVisibility(View.GONE);
		});
		spaceRight = new View(getContext());
		spaceRight.setLayoutParams(new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.mdp_40), padding));
		spaceRightLp = (RelativeLayout.LayoutParams) spaceRight.getLayoutParams();
		addingAvatar = new Avatar(Avatar.BUBBLE_NAME_BOTTOM, getContext());
		addingAvatar.setNeedInterceptName();
		addingAvatar.setVisibility(View.INVISIBLE);
		addingAvatar.setAvatarFace(AddressBookType.Staff, currentNode.getNodeId(), currentNode.getNodeName(),
				currentNode.getImageHref());
		final FrameLayout topest = (FrameLayout) ((Activity) getContext()).getWindow().getDecorView();
		topest.addView(addingAvatar);
		addingAvatarLP = (FrameLayout.LayoutParams) addingAvatar.getLayoutParams();
		addingAvatarLP.gravity = Gravity.LEFT | Gravity.TOP;
		handler.postDelayed(waitForDraw, 20);
	}

	/**
	 * 设置流程图节点是否可选
	 */
	@Override
	public void setSelected(boolean b) {
		seleted = b;
		if (b) {
			nodeSelected_Iv.setVisibility(View.VISIBLE);
		}
		else {
			nodeSelected_Iv.setVisibility(View.GONE);
		}
	}

	/*--节点提示框移动--*/
	void changeHint(final int toX, final int toY) {
		nodeSelected_Iv.clearAnimation();
		final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) nodeSelected_Iv.getLayoutParams();
		if (changing) {
			changing = false;
			lp.setMargins(toX - hintWidth / 2, toY - hintHeight / 2, 0, 0);
			nodeSelected_Iv.setLayoutParams(lp);
		}
		else {
			AnimationSimple.move(nodeSelected_Iv, AnimationSimple.DURATIONTIME_300, toX - lp.leftMargin - hintWidth / 2,
					toY - lp.topMargin - hintHeight / 2, new DecelerateInterpolator(1.7f));
			changing = true;
			handler.postDelayed(() -> {
				nodeSelected_Iv.clearAnimation();
				if (changing) {
					changing = false;
					lp.setMargins(toX - hintWidth / 2, toY - hintHeight / 2, 0, 0);
					nodeSelected_Iv.setLayoutParams(lp);
				}
			}, AnimationSimple.DURATIONTIME_300);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		/*--计算有效的坐标--*/
		int x = (int) (ev.getRawX() - addingAvatar.getWidth() * 1.5);
		int y = (int) (ev.getRawY() - addingAvatar.getHeight() * 1.5);
		if (x < locationLeft) {
			x = locationLeft;
		}
		if (x + addingAvatar.getWidth() > locationRight) {
			x = locationRight - addingAvatar.getWidth();
		}
		if (y < locationTop) {
			y = locationTop;
		}
		if (y + addingAvatar.getHeight() > locationBottom) {
			y = locationBottom - addingAvatar.getHeight();
		}
		if (ev.getAction() == MotionEvent.ACTION_DOWN && !doingMovingAnimation) {
			addingAvatarLP.leftMargin = x;
			addingAvatarLP.topMargin = y;
			addingAvatar.setLayoutParams(addingAvatarLP);
		}
		if (!doingMovingAnimation && addingAvatar.getVisibility() == View.VISIBLE) {
			addingAvatar.layout(x, y, x + addingAvatar.getWidth(), y + addingAvatar.getHeight());
		}
		if (notScroll || doingMovingAnimation) {
			return false;
		}
		if (ev.getAction() == MotionEvent.ACTION_DOWN && nodeDelete_Iv.getVisibility() == View.VISIBLE) {
			hideDelete();
		}
		return !touchable || super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP && !doingMovingAnimation) {
			if (addingAvatar.getVisibility() == View.VISIBLE) {
				if (ev.getX() < WorkFlowDrawer.spaceLeft) {
					doDisappear(false);
				}
				else {
					addNode(addingNode);
					addingAnimation();
				}
			}
		}
		return super.onTouchEvent(ev);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if ((locationLeft + locationTop + locationRight + locationBottom) == 0) {// 其实也就是判断有无初始化
			getLocationInWindow(from);// 临时借用下int[2]数组
			locationLeft = l + from[0];
			locationTop = t + from[1];
			locationRight = r + from[0];
			locationBottom = b + from[1];
		}
		super.onLayout(changed, l, t, r, b);
	}

	/**
	 * 外部隐藏删除按钮
	 */
	private void hideDelete() {
		handler.postDelayed(() -> nodeDelete_Iv.setVisibility(View.GONE), AnimationSimple.DURATIONTIME_100);
	}

	/*--节点操作の删除--*/
	private void deleteNode(WorkFlowNode deleted) {
		if (deleted.getParent() == null) {// 根节点
			for (final WorkFlowNode node : deleted.getChildNodes()) {
				node.setParent(null);
				deleteChileNode(node);
			}
			deleted.getChildNodes().clear();
		}
		else {
			currentNode = deleted.getParent();
			deleted.getParent().getChildNodes().remove(deleted);
			deleted.setParent(null);
			deleteChileNode(deleted);
		}
		WorkFlowDrawer.drawTree(this);
	}

	/*--节点操作の删除--*/
	private void deleteChileNode(WorkFlowNode deleted) {
		if (deleted.getChildNodes() != null || deleted.getChildNodes().size() != 0) {
			for (final WorkFlowNode node : deleted.getChildNodes()) {
				deleteChileNode(node);
			}
		}
		deleted.setParent(null);
	}

	/**
	 * 显示完成名称的气泡框
	 */
	void showBubbleWindow(View avatar) {
		String bwName = currentNode.getNodeName();
		String id = currentNode.getNodeId();
		int type = currentNode.getType();
		if (type == AddressBookType.Staff || type == AddressBookType.Group) {
			CoreZygote.getAddressBookServices().queryUserDetail(id)
					.subscribe(it -> {
						if (it != null) {
							FEImageLoader.load(mContext, mIvBubbleAvatar
									, CoreZygote.getLoginUserServices().getServerAddress() + it.imageHref, it.userId, it.name);
						}
						else {
							mIvBubbleAvatar.setImageResource(R.drawable.administrator_icon);
						}
					}, error -> {
						mIvBubbleAvatar.setImageResource(R.drawable.administrator_icon);
					});
		}
		else if (type == AddressBookType.Position) {
			mIvBubbleAvatar.setImageDrawable(getContext().getResources().getDrawable(R.drawable.head_post_fe));
		}
		else if (type == AddressBookType.Company) {
			mIvBubbleAvatar.setImageDrawable(getContext().getResources().getDrawable(R.drawable.head_corporation_fe));
		}
		else if (type == AddressBookType.Department) {
			mIvBubbleAvatar.setImageDrawable(getContext().getResources().getDrawable(R.drawable.head_department_fe));
		}
		mTvBubbleText.setText(bwName);
		mBubbleWindow.setNeedInitLocation(true);

		String wflag = currentNode.getWflag();
		if (!TextUtils.isEmpty(wflag) && !TextUtils.equals(currentNode.getGgId(), rootNode.getGgId())) {
			String flag = "";
			switch (wflag) {
				case "0":
					flag = getContext().getString(R.string.workflow_done);
					break;
				case "1":
					flag = getContext().getString(R.string.workflow_received);
					break;
				case "2":
					flag = getContext().getString(R.string.workflow_unreceived);
					break;
				case "3":
					flag = getContext().getString(R.string.workflow_stop);
					break;
				case "4":
					flag = getContext().getString(R.string.workflow_freeze);
					break;
				case "8":
					flag = getContext().getString(R.string.workflow_return);
					break;
			}
			mTvBubbleSubText.setVisibility(View.VISIBLE);
			mTvBubbleSubText.setText(flag);
		}
		else {
			mTvBubbleSubText.setVisibility(View.GONE);
			mTvBubbleSubText.setText("");
		}

		mBubbleWindow.show(avatar);
	}

	/*--节点操作の增加--*/
	public void addNode(WorkFlowNode added) {
		if (!hasNode(added)) {
			setNode(added);
		}
	}


	private void setNode(WorkFlowNode added) {
		currentNode.getChildNodes().add(added);
		added.setParent(currentNode);
		if (currentNode.getNodeType() == NodeType.user) {
			added.setEndorse(true);
			added.setEndorseby(currentNode.getNodeId());
			added.setNewnode(true);
		}
		allNode.add(added);
		WorkFlowDrawer.drawTree(this);
	}

	/**
	 * 锁定已传入的所有节点
	 */
	public void setLockExist(boolean b) {
		lock = b;
	}

	/**
	 * 流程图是否被用户修改过
	 */
	public boolean hasModify() {
		int t = 0;
		for (final WorkFlowNode node : allNode) {
			if (node.getParent() != null || node == rootNode) {
				t++;
			}
		}
		return nodeCount != t;
	}

	/**
	 * 设置选中的节点
	 */
	private void seleteUser() {
		for (final WorkFlowNode node : allNode) {
			if (node.getNodeType() == NodeType.user) {
				changing = true;
				currentNode = node;
				changeHint(node.getNodeX(), node.getNodeY());
				return;
			}
		}
	}

	public void setTouchable() {
		touchable = true;
	}

	/**
	 * 获取气泡框
	 */
	public BubbleWindow getBubbleWindow() {
		return mBubbleWindow;
	}

	/**
	 * 加一个新的节点,动画已封装入内部
	 */
	public void addNewNode(WorkFlowNode wfn) {
		if ((currentNode.getNodeType() == NodeType.existed || currentNode.getNodeType() == NodeType.locked) && lock) {
			FEToast.showMessage(getResources().getString(R.string.flow_nodeunmodify));
			return;
		}
		if (hasNode(wfn)) {
			return;
		}
		if (doingMovingAnimation) {
			// 防止操作太快动画还没结束又新加一个
			return;
		}
		addingNode = wfn;
		addingAvatar.startAnimation(sa);
		addingAvatar.setName(addingNode.getNodeName());
		addingAvatar.setAvatarFace(addingNode.getType(), addingNode.getNodeId(), addingNode.getNodeName(), addingNode.getImageHref());
		addingAvatar.setVisibility(View.VISIBLE);
	}

	private boolean hasNode(WorkFlowNode wfn) {
		for (WorkFlowNode node : currentNode.getChildNodes()) {
			if (node.getNodeId().equals(wfn.getNodeId())) {
				FEToast.showMessage(getResources().getString(R.string.flow_alreadyadded));
				return true;
			}
		}
		return false;
	}

	/**
	 * 设置流程图左边边距,用于不覆盖通讯录
	 */
	public void setSpaceLeft(int spaceLeft) {
		handler.removeCallbacks(waitForDraw);
		WorkFlowDrawer.spaceLeft = spaceLeft;
		waitForDraw.run();
	}

	/**
	 * 获取流程图左边边距
	 */
	public int getSpaceLeft() {
		return WorkFlowDrawer.spaceLeft;
	}

	/**
	 * 手指松开头像移到应该去的位置的那个动画
	 */
	private void addingAnimation() {
		doingMovingAnimation = true;
		addingNode.getAvatar().setVisibility(View.INVISIBLE);
		addingAvatarLP.leftMargin = addingAvatar.getLeft();
		addingAvatarLP.topMargin = addingAvatar.getTop();
		handler.postDelayed(() -> {
			addingNode.getAvatar().getImageView().getLocationOnScreen(to);// 不在需要复杂的计算了,获取头像空间中的ImageView位置差即可
			addingAvatar.getImageView().getLocationOnScreen(from);
			final TranslateAnimation ta = AnimationSimple.move(null, 500, to[0] - from[0], to[1] - from[1], i);
			addingAvatar.startAnimation(ta);
			ta.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					addingAvatar.clearAnimation();
					addingNode.getAvatar().setVisibility(View.VISIBLE);
					final int finalX = addingAvatar.getLeft() + to[0] - from[0];// 计算动画移动后的的坐标
					final int finalY = addingAvatar.getTop() + to[1] - from[1];
					addingAvatar.layout(finalX, finalY, finalX + addingAvatar.getWidth(), finalY + addingAvatar.getHeight());
					doDisappear(true);
				}
			});
		}, 20);
		handler.postDelayed(() -> doingMovingAnimation = false, 820);
	}

	private void doDisappear(boolean confirmedAdd) {
		addingAvatar.startAnimation(aa);
		addingAvatar.setVisibility(View.INVISIBLE);
		if (confirmedAdd) {
			addingNode.getAvatar().setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 设置是否能滑动流程图
	 */
	public void setNotScroll(boolean value) {
		notScroll = value;
	}

	private final Runnable waitForDraw = () -> {
		try {
			WorkFlowDrawer.drawTree(WorkFlowView.this);
			seleteUser();
		} catch (final Exception e) {
			((Activity) WorkFlowView.this.getContext()).finish();
			e.printStackTrace();
		}
	};               // 等待一定时间设置左边距参数，等不到就把他先画了吧
	private static final Handler handler = new Handler(); // 用来做延时操作的

	private Paint mWaterMarkPaint;
	private Rect mWaterMarkRect;
	private String mWaterMark;

	{
		mWaterMark = WMStamp.getInstance().getWaterMarkText();
		if (!TextUtils.isEmpty(mWaterMark)) {
			mWaterMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mWaterMarkPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
			mWaterMarkPaint.setColor(Color.parseColor("#0F666666"));
			mWaterMarkRect = new Rect();
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (TextUtils.isEmpty(mWaterMark)) {
			return;
		}

		View childAt = getChildAt(0);
		int mWidth = childAt.getWidth();
		int mHeight = childAt.getHeight();
		mWaterMarkPaint.getTextBounds(mWaterMark, 0, mWaterMark.length(), mWaterMarkRect);

		int widthCount, heightCount;
		int singleWidth = (int) (mWaterMarkRect.width() * 1.5);
		if (singleWidth < mWidth / 4) {
			singleWidth = mWidth / 4;
			widthCount = 2;
		}
		else {
			widthCount = mWidth / singleWidth + 2;
		}

		int singleHeight = mWaterMarkRect.height() * 6;
		if (singleHeight > mHeight) {
			singleHeight = mHeight;
			heightCount = 2;
		}
		else {
			heightCount = mHeight / singleHeight + 2;
			heightCount = heightCount < 0 ? 2 : heightCount;
		}

		int startX, startY;
		for (int i = 0; i < heightCount; i++) {            // 每一行
			for (int j = 0; j < widthCount; j++) {         // 每一列
				startX = i % 2 == 0 ? (j - 1) * singleWidth : (j - 1) * singleWidth + singleWidth / 2;
				startY = (i + 1) * singleHeight;
				canvas.save();
				canvas.rotate(342, startX, startY);
				canvas.drawText(mWaterMark, startX, startY, mWaterMarkPaint);
				canvas.restore();
			}
		}
	}
}
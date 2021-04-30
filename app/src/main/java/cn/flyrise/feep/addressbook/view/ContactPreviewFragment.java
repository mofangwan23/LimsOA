package cn.flyrise.feep.addressbook.view;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.ContactPreviewAdapter;
import cn.flyrise.feep.core.services.model.AddressBook;

/**
 * @author ZYP
 * @since 2016-12-13 10:16
 */
public class ContactPreviewFragment extends DialogFragment {

	private RecyclerView mRecyclerView;
	private List<AddressBook> mSelectedContacts;
	private DialogInterface.OnDismissListener mDismissLisntener;
	private DialogInterface.OnClickListener mClickListener;
	private int mMaxHeight;
	private int mMarginTop;

	private TextView tvEmpty;

	public static ContactPreviewFragment newInstance(int maxHeight, int marginTop) {
		ContactPreviewFragment instance = new ContactPreviewFragment();
		instance.mMaxHeight = maxHeight;
		instance.mMarginTop = marginTop;
		return instance;
	}

	public void setSeletedContacts(List<AddressBook> seletedContacts) {
		this.mSelectedContacts = seletedContacts;
	}

	public void setOnDismissLisntener(DialogInterface.OnDismissListener dismissLisntener) {
		this.mDismissLisntener = dismissLisntener;
	}

	public void setOnClickListener(DialogInterface.OnClickListener clickListener) {
		this.mClickListener = clickListener;
	}

	@Override public void onStart() {
		super.onStart();
		getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		WindowManager.LayoutParams attributes = getDialog().getWindow().getAttributes();
		attributes.gravity = Gravity.TOP;
		attributes.height = mMaxHeight;
		attributes.dimAmount = 0.0F;
		attributes.y = mMarginTop;
		getDialog().getWindow().setAttributes(attributes);
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		View view = inflater.inflate(R.layout.fragment_contact_preview, container, false);
		view.setOnClickListener(v -> dismiss());
		this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
		this.tvEmpty = (TextView) view.findViewById(R.id.tvEmpty);
		this.initialize();
		return view;
	}

	private void initialize() {
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());

		final ContactPreviewAdapter adapter = new ContactPreviewAdapter(getActivity());
		adapter.setSelectedContacts(mSelectedContacts);
		mRecyclerView.setAdapter(adapter);
		tvEmpty.setVisibility(CommonUtil.isEmptyList(mSelectedContacts) ? View.VISIBLE : View.GONE);

		adapter.setOnContactItemClickListener((addressBook, position) -> {
			mSelectedContacts.remove(addressBook);
			adapter.notifyDataSetChanged();
			if (mClickListener != null) mClickListener.onClick(getDialog(), position);
			tvEmpty.setVisibility(CommonUtil.isEmptyList(mSelectedContacts) ? View.VISIBLE : View.GONE);
		});
	}

	@Override public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mDismissLisntener != null) mDismissLisntener.onDismiss(dialog);
	}
}

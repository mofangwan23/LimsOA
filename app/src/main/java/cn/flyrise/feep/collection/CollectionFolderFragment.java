package cn.flyrise.feep.collection;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collection.adapter.CollectionFolderAdapter;
import cn.flyrise.feep.collection.bean.FavoriteFolder;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog.OnClickListener;
import cn.flyrise.feep.core.dialog.FEMaterialEditTextDialog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-21 10:07
 */
public class CollectionFolderFragment extends Fragment {

	private static final String[] EDIT_MENU = {"修改", "删除"};

	public static final int MODE_DISPLAY = 0;   // 个人-收藏：显示
	public static final int MODE_SELECT = 1;    // 收藏夹选择界面

	private int mMode;
	private FavoriteRepository mRepository;

	private ListView mCollectionFolderListView;
	private CollectionFolderAdapter mAdapter;
	private View mEmptyView;

	public static CollectionFolderFragment newInstance(int mode) {
		CollectionFolderFragment instance = new CollectionFolderFragment();
		instance.mRepository = new FavoriteRepository();
		instance.mMode = mode;
		return instance;
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.fragment_collection_list, container, false);
		bindView(contentView);
		return contentView;
	}

	private void bindView(View contentView) {
		contentView.findViewById(R.id.layoutAddCollectionFolder).setOnClickListener(this::createFavoriteFolder);
		mEmptyView = contentView.findViewById(R.id.layoutEmptyView);
		mCollectionFolderListView = contentView.findViewById(R.id.listView);
		mCollectionFolderListView.setAdapter(mAdapter = new CollectionFolderAdapter(mMode));

		mCollectionFolderListView.setOnItemClickListener((parent, view, position, id) -> {
			FavoriteFolder folder = (FavoriteFolder) mAdapter.getItem(position);
			if (mMode == MODE_DISPLAY) {
				Intent intent = new Intent(getActivity(), CollectionListActivity.class);
				intent.putExtra("favoriteId", folder.favoriteId);
				intent.putExtra("favoriteName", folder.favoriteName);
				getActivity().startActivity(intent);
			}
			else {
				mAdapter.setSelectedFolder(folder);
			}
		});

		if (mMode == MODE_DISPLAY) {
			mCollectionFolderListView.setOnItemLongClickListener((parent, view, position, id) -> {
				FavoriteFolder folder = (FavoriteFolder) mAdapter.getItem(position);
				if (!folder.isEdit) return false;       // 不可编辑

				new FEMaterialDialog.Builder(getActivity())
						.setWithoutTitle(true)
						.setCancelable(true)
						.setItems(EDIT_MENU, (son, of, bitch) -> {
							if (bitch == 0) updateFavoriteFolder(folder);
							else removeFavoriteFolder(folder);
							son.dismiss();
						})
						.build()
						.show();
				return true;
			});
		}

		refreshFavoriteFolder();
	}

	public FavoriteFolder getSelectedFolder() {
		return mAdapter.getSelectedFolder();
	}

	// 创建收藏夹
	private void createFavoriteFolder(View clickView) {
		new FEMaterialEditTextDialog.Builder(getActivity())
				.setCancelable(false)
				.setHint("请输入收藏夹名称")
				.setTitle("新建收藏夹")
				.setNegativeButton(null, null)
				.setPositiveButton(null, (dialog, input, isChecked) -> {
					input = input.trim();
					if (TextUtils.isEmpty(input)) {
						FEToast.showMessage("请输入收藏夹名称");
						return;
					}
					if (input.length()>10) {
						FEToast.showMessage("最多可输入10个字");
						return;
					}

					LoadingHint.show(getActivity());
					mRepository.createFolder(input)
							.subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(result -> {
								LoadingHint.hide();
								if (result.errorCode == 0) {
									FEToast.showMessage("添加成功");
									refreshFavoriteFolder();
									return;
								}
								FEToast.showMessage(result.errorMessage);
							}, exception -> {
								LoadingHint.hide();
								FEToast.showMessage(CommonUtil.getString(R.string.lbl_retry_operator));
							});
				})
				.build()
				.show();
	}

	// 修改收藏夹
	private void updateFavoriteFolder(FavoriteFolder folder) {
		new FEMaterialEditTextDialog.Builder(getActivity())
				.setCancelable(false)
				.setTitle("修改收藏夹")
				.setHint(folder.favoriteName)
				.setNegativeButton(null, null)
				.setPositiveButton(null, (dialog, input, isChecked) -> {
					input = input.trim();
					if (TextUtils.isEmpty(input)) {
						FEToast.showMessage("请输入收藏夹名称");
						return;
					}

					if (TextUtils.equals(input, folder.favoriteName)) {
						FEToast.showMessage("与原项名称相同");
						return;
					}
					if (input.length()>10) {
						FEToast.showMessage("最多可输入10个字");
						return;
					}

					LoadingHint.show(getActivity());
					mRepository.updateFolder(folder.favoriteId, input)
							.subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(result -> {
								LoadingHint.hide();
								if (result.errorCode == 0) {
									FEToast.showMessage("修改成功");
									refreshFavoriteFolder();
									dialog.dismiss();
									return;
								}

								FEToast.showMessage(result.errorMessage);
							}, exception -> {
								LoadingHint.hide();
								FEToast.showMessage(CommonUtil.getString(R.string.lbl_retry_operator));
							});

				})
				.build()
				.show();
	}

	// 删除收藏夹
	private void removeFavoriteFolder(FavoriteFolder folder) {
		LoadingHint.show(getActivity());
		new FEMaterialDialog.Builder(getActivity())
				.setTitle("删除收藏夹")
				.setMessage("收藏夹内的所有收藏都将取消收藏")
				.setCancelable(true)
				.setPositiveButton("删除", dialog -> {
					mRepository.deleteFolder(folder.favoriteId)
							.subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe(result -> {
								LoadingHint.hide();
								if (result.errorCode == 0) {
									FEToast.showMessage("删除成功");
									refreshFavoriteFolder();
									return;
								}
								FEToast.showMessage(result.errorMessage);
							}, exception -> {
								LoadingHint.hide();
								FEToast.showMessage(CommonUtil.getString(R.string.lbl_retry_operator));
							});
				})
				.setNegativeButton("取消", dialog -> {LoadingHint.hide();})
				.build()
				.show();

	}

	// 刷新数据
	private void refreshFavoriteFolder() {
		LoadingHint.show(getActivity());
		mRepository.queryAllCollectionFolders()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(folders -> {
					LoadingHint.hide();
					mEmptyView.setVisibility(CommonUtil.isEmptyList(folders) ? View.VISIBLE : View.GONE);
					mAdapter.setFavoriteFolders(folders);
				}, exception -> {
					LoadingHint.hide();
					FEToast.showMessage("数据刷新失败,请稍后重试！");
				});
	}
}

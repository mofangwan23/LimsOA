package cn.flyrise.feep.media.images;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import cn.flyrise.feep.core.base.views.adapter.DividerItemDecoration;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.images.adapter.AlbumSelectionAdapter;
import cn.flyrise.feep.media.images.adapter.AlbumSelectionAdapter.OnAlbumItemClickListener;
import cn.flyrise.feep.media.images.bean.Album;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-18 14:40
 */
public class AlbumSelectionWindow {

	private String mDefaultAlbumId = "-1";
	private final Context mContext;
	private final List<Album> mImageAlbums;

	private PopupWindow mPopupWindow;
	private AlbumSelectionAdapter mAlbumAdapter;
	private OnImageAlbumOperatedListener mAlbumOperatedListener;

	public AlbumSelectionWindow(Context context, List<Album> imageAlbums) {
		this.mContext = context;
		this.mImageAlbums = imageAlbums;
	}

	public void setOnImageAlbumOperatedListener(OnImageAlbumOperatedListener listener) {
		this.mAlbumOperatedListener = listener;
	}

	public void showImageAlbums(View archView) {
		if (mPopupWindow == null) {
			View contentView = LayoutInflater.from(mContext).inflate(R.layout.ms_view_album_selection, null);
			RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.msRecyclerView);
			recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
			recyclerView.setItemAnimator(new DefaultItemAnimator());

			Drawable drawable = mContext.getResources().getDrawable(R.drawable.ms_divider_album_item);
			DividerItemDecoration dividerDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
			dividerDecoration.setDrawable(drawable);
			recyclerView.addItemDecoration(dividerDecoration);

			mAlbumAdapter = new AlbumSelectionAdapter(mImageAlbums);
			mAlbumAdapter.setOnAlbumItemClickListener(new OnAlbumItemClickListener() {
				@Override public void onAlbumItemClick(Album album, int position) {
					mDefaultAlbumId = album.id;
					if (mAlbumOperatedListener != null) {
						mAlbumOperatedListener.onAlbumClick(album);
					}
					if (mPopupWindow.isShowing()) {
						mPopupWindow.dismiss();
					}
				}
			});
			recyclerView.setAdapter(mAlbumAdapter);

			final int maxHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.7);
			mPopupWindow = new PopupWindow(contentView, MATCH_PARENT, maxHeight);
			mPopupWindow.setContentView(contentView);
			mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setFocusable(true);
			mPopupWindow.setOnDismissListener(new OnDismissListener() {
				@Override public void onDismiss() {
					if (mAlbumOperatedListener != null) {
						mAlbumOperatedListener.onAlbumDismiss();
					}
				}
			});
		}

		if (mAlbumOperatedListener != null) {
			mAlbumOperatedListener.onAlbumPrepareDisplay();
		}
		mAlbumAdapter.setDefaultAlbumId(mDefaultAlbumId);
		mPopupWindow.showAsDropDown(archView, 0, 0);
	}

	public interface OnImageAlbumOperatedListener {

		void onAlbumPrepareDisplay();

		void onAlbumDismiss();

		void onAlbumClick(Album album);
	}

}

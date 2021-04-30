package cn.flyrise.feep.media.images;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.ImageView;

import cn.flyrise.feep.media.images.bean.ImageItem;
import cn.flyrise.feep.media.images.repository.ImageDataSource;
import cn.flyrise.feep.media.images.repository.ImageSelectionSpec;
import java.util.ArrayList;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-10-17 14:22
 */
public class ImageSelectionPresenter {

	private ImageDataSource mDataSource;
	private ImageSelectionView mPickerView;

	public ImageSelectionPresenter(ImageSelectionView imageSelectionView, Intent intent) {
		this.mPickerView = imageSelectionView;
		this.mDataSource = new ImageDataSource(new ImageSelectionSpec(intent));
	}

	public void start() {
		mDataSource.loadAllImages((Context) mPickerView)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mPickerView::onImageLoad, exception -> mPickerView.onImageLoad(null));
	}

	public void loadImageAlbums() {
		mDataSource.loadImageAlbums((Context) mPickerView)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mPickerView::onImageAlbumLoad);
	}

	public void loadImages(String folderId) {
		(TextUtils.equals(folderId, "-1")
				? mDataSource.loadAllImages((Context) mPickerView)
				: mDataSource.loadImageByFolderId((Context) mPickerView, folderId))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mPickerView::onImageLoad, exception -> mPickerView.onImageLoad(null));
	}

	/**
	 * 未添加的会执行添加操作，已添加的执行移除操作.
	 */
	public int executeImageCheckChange(ImageItem imageItem) {
		return mDataSource.executeImageCheckedChange(imageItem);
	}

	/**
	 * 获取选择的图片
	 */
	public List<ImageItem> getSelectedImages() {
		return mDataSource.getSelectedImages();
	}

	/**
	 * 设置选中的图片
	 * @param imageItemList
	 */
	public void setSelectedImages(List<ImageItem> imageItemList){
		mDataSource.setmSelectedImages(imageItemList);
	}

	public List<String> getSelectedImagePath() {
		List<ImageItem> selectedImages = getSelectedImages();
		List<String> imagePath = new ArrayList<>(selectedImages.size());
		for (ImageItem item : selectedImages) {
			imagePath.add(item.path);
		}
		return imagePath;
	}

	/**
	 * 将所有选中的图片收集起来
	 * @param allImageList
	 * @return
	 */
    public List<ImageItem> getSelectedImageList(List<ImageItem> allImageList) {
        List<ImageItem> selectedImageList = new ArrayList<>();
        for (ImageItem item : allImageList) {
            if(item.isHasSelected()){
                selectedImageList.add(item);
            }
        }
        return selectedImageList;
    }

	/**
	 *
	 * @param allImageItems
	 * @param position
	 * @param checkState
	 * @return  记录所有图片的选中状态
	 */
	public List<ImageItem> getAllImagesInCludeSelected(List<ImageItem> allImageItems,int position, int checkState){
	    ImageItem imageItem = allImageItems.get(position);
	    if(checkState == 1){
            imageItem.setHasSelected(true);
            allImageItems.set(position,imageItem);
        }else {
            imageItem.setHasSelected(false);
            allImageItems.set(position,imageItem);
        }
		return allImageItems;
	}

	public boolean isSingleChoice() {
		return mDataSource.isSingleChoice();
	}

	public void onDestroy() {

	}

}

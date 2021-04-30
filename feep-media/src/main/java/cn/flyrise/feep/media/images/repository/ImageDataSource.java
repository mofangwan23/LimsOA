package cn.flyrise.feep.media.images.repository;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.text.TextUtils;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.media.images.bean.Album;
import cn.flyrise.feep.media.images.bean.ImageFolder;
import cn.flyrise.feep.media.images.bean.ImageItem;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * @author ZYP
 * @since 2017-10-17 14:22
 */
public class ImageDataSource {

	// 数据源，角色是 model, Activity 不应该知晓我的存在...

	private static final String[] STORE_IMAGES = {
			MediaStore.Images.Media._ID,                    // Image id.
			MediaStore.Images.Media.DATA,                   // Image absolute path.
			MediaStore.Images.Media.DISPLAY_NAME,           // Image name.
			MediaStore.Images.Media.DATE_ADDED,             // The time to be added to the library.
			MediaStore.Images.Media.BUCKET_ID,              // Folder id.
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME,    // Folder name.
			MediaStore.Images.Media.SIZE                    // Image size.
	};

	private List<ImageFolder> mImageFolders;    // 这里面级联着很多的操作啊...
	private ImageSelectionSpec mSelectionSpec;
	private List<ImageItem> mSelectedImages;    // 已选择的图片，在首次加载图片的时候，根据传入的已选择路径创建

	public ImageDataSource(ImageSelectionSpec spec) {
		this.mSelectionSpec = spec;
		this.mSelectedImages = new ArrayList<>();
	}

	public void setmSelectedImages(List<ImageItem> mSelectedImages){
		this.mSelectedImages = mSelectedImages;
	}

	/**
	 * 加载设备上的所有图片
	 */
	public Observable<List<ImageFolder>> loadDeviceImages(final Context context) {
		if (mImageFolders != null) {
			return Observable.just(mImageFolders);
		}
		return Observable
				.create(new OnSubscribe<List<ImageFolder>>() {
					@Override public void call(Subscriber<? super List<ImageFolder>> subscriber) {
						mImageFolders = new ArrayList<>();
						Cursor cursor = null;
						try {
							cursor = MediaStore.Images.Media.query(context.getContentResolver(),
									MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
									STORE_IMAGES, null, null, MediaStore.Images.Media.DATE_ADDED + " desc");
							while (cursor.moveToNext()) {
								String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
								if (isBadImage(path)) {
									continue;
								}

								String id = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
								long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
								String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
								long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
								String folderId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
								String folderName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

								ImageItem imageItem = new ImageItem(id, name, path, size, date);
								if (!mSelectionSpec.isExpectImage(imageItem)) {
									continue;
								}
								if (mSelectionSpec.isImageSelected(imageItem)) {
									executeImageCheckedChange(imageItem);
								}

								ImageFolder imageFolder = findImageFolder(mImageFolders, folderId, folderName);
								if (imageFolder == null) {
									imageFolder = new ImageFolder(folderId, folderName);
									imageFolder.addImage(imageItem);
									mImageFolders.add(imageFolder);
								}
								else {
									imageFolder.addImage(imageItem);
								}
							}
							subscriber.onNext(mImageFolders);
						} catch (Exception exp) {
							exp.printStackTrace();
							subscriber.onError(exp);
						} finally {
							if (cursor != null) {
								cursor.close();
							}
							subscriber.onCompleted();
						}
					}
				});
	}

	/**
	 * 加载所有图片
	 */
	public Observable<List<ImageItem>> loadAllImages(Context context) {
		return loadDeviceImages(context)
				.flatMap(new Func1<List<ImageFolder>, Observable<ImageFolder>>() {
					@Override public Observable<ImageFolder> call(List<ImageFolder> imageFolders) {
						return Observable.from(imageFolders);
					}
				})
				.flatMap(new Func1<ImageFolder, Observable<ImageItem>>() {
					@Override public Observable<ImageItem> call(ImageFolder imageFolder) {
						return Observable.from(imageFolder.getImages());
					}
				})
				.toSortedList(new Func2<ImageItem, ImageItem, Integer>() {
					@Override public Integer call(ImageItem imageItem, ImageItem imageItem2) {
						if (imageItem.date > imageItem2.date) return -1;
						else if (imageItem.date == imageItem2.date) return 0;
						return 1;
					}
				});
	}

	/**
	 * 加载指定文件下的图片
	 */
	public Observable<List<ImageItem>> loadImageByFolderId(Context context, final String folderId) {
		return loadDeviceImages(context)
				.flatMap(new Func1<List<ImageFolder>, Observable<ImageFolder>>() {
					@Override public Observable<ImageFolder> call(List<ImageFolder> imageFolders) {
						return Observable.from(imageFolders);
					}
				})
				.filter(new Func1<ImageFolder, Boolean>() {
					@Override public Boolean call(ImageFolder imageFolder) {
						return TextUtils.equals(imageFolder.id, folderId);
					}
				})
				.flatMap(new Func1<ImageFolder, Observable<ImageItem>>() {
					@Override public Observable<ImageItem> call(ImageFolder imageFolder) {
						return Observable.from(imageFolder.getImages());
					}
				})
				.toSortedList(new Func2<ImageItem, ImageItem, Integer>() {
					@Override public Integer call(ImageItem imageItem, ImageItem imageItem2) {
						if (imageItem.date > imageItem2.date) return -1;
						else if (imageItem.date == imageItem2.date) return 0;
						return 1;
					}
				});
	}

	/**
	 * 加载相册集
	 */
	public Observable<List<Album>> loadImageAlbums(Context context) {
		return loadDeviceImages(context)
				.flatMap(new Func1<List<ImageFolder>, Observable<ImageFolder>>() {
					@Override public Observable<ImageFolder> call(List<ImageFolder> imageFolders) {
						return Observable.from(imageFolders);
					}
				})
				.map(new Func1<ImageFolder, Album>() {
					@Override public Album call(ImageFolder folder) {
						return new Album(folder.id, folder.name, folder.getAlbumCover(), folder.getImageSize());
					}
				})
				.toList()
				.doOnNext(new Action1<List<Album>>() {
					@Override public void call(List<Album> imageAlbums) {
						if (imageAlbums == null || imageAlbums.size() == 0) return;
						int totalCount = 0;
						for (Album imageAlbum : imageAlbums) {
							totalCount += imageAlbum.count;
						}
						imageAlbums.add(0, new Album("-1", "全部图片", imageAlbums.get(0).cover, totalCount));
					}
				});
	}

	/**
	 * 执行图片操作，添加 or 移除，返回对象的状态码
	 * return 1：添加成功; -1: 移除成功; 0: 无法添加或已达到最大上限
	 */
	public int executeImageCheckedChange(ImageItem imageItem) {
		if (mSelectedImages.contains(imageItem)) {
			mSelectedImages.remove(imageItem);
			return -1;
		}
		if (mSelectedImages.size() >= mSelectionSpec.getMaxSelectCount()) {
			return 0;
		}
		mSelectedImages.add(imageItem);
		return 1;
	}


	public List<ImageItem> getSelectedImages() {
		return mSelectedImages;
	}

	private ImageFolder findImageFolder(List<ImageFolder> imageFolders, String folderId, String folderName) {
		if (imageFolders == null || imageFolders.size() == 0) return null;
		for (ImageFolder folder : imageFolders) {
			if (TextUtils.equals(folder.id, folderId) || TextUtils.equals(folder.name, folderName)) {
				return folder;
			}
		}
		return null;
	}

	private boolean isBadImage(String imagePath) {
		boolean isBadImage = false;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imagePath, options);   //filePath代表图片路径
			if (options.mCancel || options.outWidth == -1 || options.outHeight == -1) {
				return true;
			}
		} catch (Exception exp) {
			isBadImage = true;
		}
		return isBadImage;
	}


	public boolean isSingleChoice() {
		return mSelectionSpec.isSingleChoice();
	}
}

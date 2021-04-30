package cn.flyrise.feep.media.images;


import cn.flyrise.feep.media.images.bean.Album;
import cn.flyrise.feep.media.images.bean.ImageItem;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-17 16:15
 */
public interface ImageSelectionView {

	/**
	 * 加载设备上或指定目录的图片，如果加载失败，则 imageItems 为空
	 * @param imageItems 图片
	 */
	void onImageLoad(List<ImageItem> imageItems);

	/**
	 * 加载文件夹列表，如果加载失败，则 imageAlbums 为空
	 */
	void onImageAlbumLoad(List<Album> imageAlbums);

}

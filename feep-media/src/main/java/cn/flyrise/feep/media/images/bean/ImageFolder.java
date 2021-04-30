package cn.flyrise.feep.media.images.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-17 14:14
 */
public class ImageFolder {

	public String id;
	public String name;
	private List<ImageItem> images;

	public ImageFolder() {
		this.images = new ArrayList<>();
	}

	public ImageFolder(String folderId, String folderName) {
		this.id = folderId;
		this.name = folderName;
		this.images = new ArrayList<>();
	}

	public int getImageSize() {
		return images == null ? 0 : images.size();
	}

	public void addImage(ImageItem imageItem) {
		if (!images.contains(imageItem)) {
			images.add(imageItem);
		}
	}

	public List<ImageItem> getImages() {
		return images;
	}

	public String getAlbumCover() {
		return (images == null || images.size() == 0) ? null : images.get(0).path;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ImageFolder folder = (ImageFolder) o;

		return name.equals(folder.name);

	}

	@Override public int hashCode() {
		return name.hashCode();
	}
}

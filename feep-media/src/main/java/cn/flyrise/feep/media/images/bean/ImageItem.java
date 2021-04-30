package cn.flyrise.feep.media.images.bean;

import java.io.Serializable;

/**
 * @author ZYP
 * @since 2017-10-17 14:14
 */
public class ImageItem implements Serializable {

	public String id;
	public String name;
	public String path;
	public long size;
	public long date;
	private boolean hasSelected;

	public void setHasSelected(boolean hasSelected){
		this.hasSelected = hasSelected;
	}

	public boolean isHasSelected(){
		return hasSelected;
	}

	public ImageItem() {
	}

	public ImageItem(String id, String name, String path, long size, long date) {
		this.id = id;
		this.name = name;
		this.path = path;
		this.size = size;
		this.date = date;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ImageItem imageItem = (ImageItem) o;

		if (size != imageItem.size) return false;
		if (date != imageItem.date) return false;
		if (!id.equals(imageItem.id)) return false;
		if (!name.equals(imageItem.name)) return false;
		return path.equals(imageItem.path);

	}

	@Override public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + path.hashCode();
		result = 31 * result + (int) (size ^ (size >>> 32));
		result = 31 * result + (int) (date ^ (date >>> 32));
		return result;
	}
}
